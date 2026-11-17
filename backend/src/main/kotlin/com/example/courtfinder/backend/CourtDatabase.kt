package com.example.courtfinder.backend

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import java.util.Base64
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Properties
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class CourtDatabase(
    databasePath: String,
    private val seedDemoCourts: Boolean = true,
    private val remoteDatabaseUrl: String? = null
) {
    private val databaseFile = Path.of(databasePath).toAbsolutePath()
    private val databaseUrl: String
    private val usesPostgres = remoteDatabaseUrl != null

    init {
        if (remoteDatabaseUrl == null) {
            databaseFile.parent?.let { Files.createDirectories(it) }
            databaseUrl = "jdbc:sqlite:$databaseFile"
        } else {
            databaseUrl = toJdbcUrl(remoteDatabaseUrl)
        }
        initialize()
    }

    private fun toJdbcUrl(value: String): String {
        val uri = URI(value)
        require(uri.scheme.equals("postgres", true) || uri.scheme.equals("postgresql", true)) {
            "DATABASE_URL must use the postgres:// or postgresql:// scheme."
        }
        require(!uri.host.isNullOrBlank() && !uri.rawPath.isNullOrBlank()) {
            "DATABASE_URL must include a database host and database name."
        }
        val query = uri.rawQuery.orEmpty().split('&').filter(String::isNotBlank).toMutableList()
        if (query.none { it.substringBefore('=').equals("sslmode", true) }) query += "sslmode=require"
        return "jdbc:postgresql://${uri.rawAuthority.substringAfter('@', uri.rawAuthority)}${uri.rawPath}?${query.joinToString("&")}"
    }

    private fun postgresProperties(): Properties {
        val uri = URI(requireNotNull(remoteDatabaseUrl))
        val credentials = uri.rawUserInfo.orEmpty().split(':', limit = 2)
        return Properties().apply {
            if (credentials.isNotEmpty() && credentials[0].isNotBlank()) {
                setProperty("user", URLDecoder.decode(credentials[0], StandardCharsets.UTF_8))
            }
            if (credentials.size > 1) {
                setProperty("password", URLDecoder.decode(credentials[1], StandardCharsets.UTF_8))
            }
        }
    }

    suspend fun register(request: RegisterRequestDto): UserDto = withContext(Dispatchers.IO) {
        val name = request.fullName.trim()
        val email = request.email.trim().lowercase(Locale.ROOT)
        val phone = request.phone.trim()
        if (name.isBlank()) throw ApiException(HttpStatusCode.BadRequest, "Full name is required.")
        if (!email.contains('@')) throw ApiException(HttpStatusCode.BadRequest, "Enter a valid email address.")
        if (request.password.length < 10) {
            throw ApiException(HttpStatusCode.BadRequest, "Password must be at least 10 characters.")
        }

        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val passwordHash = hashPassword(request.password, salt)
        val user = UserDto(UUID.randomUUID().toString(), name, email)
        try {
            connection().use { database ->
                database.prepareStatement(
                    "INSERT INTO users (id, full_name, email, phone, password_salt, password_hash) VALUES (?, ?, ?, ?, ?, ?)"
                ).use { statement ->
                    statement.setString(1, user.id)
                    statement.setString(2, name)
                    statement.setString(3, email)
                    statement.setString(4, phone)
                    statement.setBytes(5, salt)
                    statement.setBytes(6, passwordHash)
                    statement.executeUpdate()
                }
            }
        } catch (_: SQLException) {
            throw ApiException(HttpStatusCode.Conflict, "An account with that email already exists.")
        }
        user
    }

    suspend fun login(request: LoginRequestDto): UserDto = withContext(Dispatchers.IO) {
        val identifier = request.emailOrPhone.trim().lowercase(Locale.ROOT)
        val userRecord = connection().use { database ->
            database.prepareStatement(
                "SELECT id, full_name, email, password_salt, password_hash FROM users WHERE lower(email) = ? OR phone = ? LIMIT 1"
            ).use { statement ->
                statement.setString(1, identifier)
                statement.setString(2, request.emailOrPhone.trim())
                statement.executeQuery().use { result ->
                    if (!result.next()) null else UserPasswordRecord(
                        id = result.getString("id"),
                        name = result.getString("full_name"),
                        email = result.getString("email"),
                        salt = result.getBytes("password_salt"),
                        passwordHash = result.getBytes("password_hash")
                    )
                }
            }
        } ?: throw ApiException(HttpStatusCode.Unauthorized, "Email/phone or password is incorrect.")

        val submittedHash = hashPassword(request.password, userRecord.salt)
        if (!MessageDigest.isEqual(submittedHash, userRecord.passwordHash)) {
            throw ApiException(HttpStatusCode.Unauthorized, "Email/phone or password is incorrect.")
        }
        UserDto(userRecord.id, userRecord.name, userRecord.email)
    }

    suspend fun getUser(userId: String): UserDto = withContext(Dispatchers.IO) {
        connection().use { database ->
            database.prepareStatement("SELECT id, full_name, email FROM users WHERE id = ?").use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { result ->
                    if (!result.next()) throw ApiException(HttpStatusCode.Unauthorized, "Account is unavailable.")
                    UserDto(result.getString("id"), result.getString("full_name"), result.getString("email"))
                }
            }
        }
    }

    suspend fun loginWithGoogle(subject: String, email: String, name: String?): UserDto = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        if (subject.isBlank() || !normalizedEmail.contains('@')) {
            throw ApiException(HttpStatusCode.Unauthorized, "Google account details are invalid.")
        }
        connection().use { database ->
            database.prepareStatement(
                "SELECT users.id, users.full_name, users.email FROM users JOIN google_accounts ON users.id = google_accounts.user_id WHERE google_accounts.subject = ?"
            ).use { statement ->
                statement.setString(1, subject)
                statement.executeQuery().use { result ->
                    if (result.next()) return@withContext UserDto(
                        result.getString("id"), result.getString("full_name"), result.getString("email")
                    )
                }
            }

            val fullName = name?.trim()?.takeIf(String::isNotEmpty) ?: normalizedEmail.substringBefore('@')
            val user = UserDto(UUID.randomUUID().toString(), fullName, normalizedEmail)
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val randomPassword = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(ByteArray(48).also { SecureRandom().nextBytes(it) })
            val passwordHash = hashPassword(randomPassword, salt)
            database.autoCommit = false
            try {
                database.prepareStatement(
                    "INSERT INTO users (id, full_name, email, phone, password_salt, password_hash) VALUES (?, ?, ?, '', ?, ?)"
                ).use { statement ->
                    statement.setString(1, user.id)
                    statement.setString(2, user.name)
                    statement.setString(3, user.email)
                    statement.setBytes(4, salt)
                    statement.setBytes(5, passwordHash)
                    statement.executeUpdate()
                }
                database.prepareStatement("INSERT INTO google_accounts (subject, user_id) VALUES (?, ?)").use { statement ->
                    statement.setString(1, subject)
                    statement.setString(2, user.id)
                    statement.executeUpdate()
                }
                database.commit()
            } catch (_: SQLException) {
                database.rollback()
                throw ApiException(HttpStatusCode.Conflict, "An account with this email already exists. Sign in with that account first.")
            } finally {
                database.autoCommit = true
            }
            user
        }
    }

    /** Maps the verified Firebase UID to the existing CourtFinder user, preserving any legacy Google subject link. */
    suspend fun loginWithFirebaseGoogle(
        firebaseUid: String,
        googleSubject: String,
        email: String,
        name: String?
    ): UserDto = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        if (firebaseUid.isBlank() || googleSubject.isBlank() || !normalizedEmail.contains('@')) {
            throw ApiException(HttpStatusCode.Unauthorized, "Firebase Google account details are invalid.")
        }

        connection().use { database ->
            val existingFirebaseUser = database.prepareStatement(
                "SELECT users.id, users.full_name, users.email FROM users JOIN firebase_accounts ON users.id = firebase_accounts.user_id WHERE firebase_accounts.firebase_uid = ?"
            ).use { statement ->
                statement.setString(1, firebaseUid)
                statement.executeQuery().use { result ->
                    if (result.next()) UserDto(
                        result.getString("id"), result.getString("full_name"), result.getString("email")
                    ) else null
                }
            }
            if (existingFirebaseUser != null) return@withContext existingFirebaseUser

            val existingGoogleUser = database.prepareStatement(
                "SELECT users.id, users.full_name, users.email FROM users JOIN google_accounts ON users.id = google_accounts.user_id WHERE google_accounts.subject = ?"
            ).use { statement ->
                statement.setString(1, googleSubject)
                statement.executeQuery().use { result ->
                    if (result.next()) UserDto(
                        result.getString("id"), result.getString("full_name"), result.getString("email")
                    ) else null
                }
            }

            val user = existingGoogleUser ?: run {
                val fullName = name?.trim()?.takeIf(String::isNotEmpty)
                    ?: normalizedEmail.substringBefore('@')
                UserDto(UUID.randomUUID().toString(), fullName, normalizedEmail)
            }
            database.autoCommit = false
            try {
                if (existingGoogleUser == null) {
                    val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
                    val randomPassword = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(ByteArray(48).also { SecureRandom().nextBytes(it) })
                    val passwordHash = hashPassword(randomPassword, salt)
                    database.prepareStatement(
                        "INSERT INTO users (id, full_name, email, phone, password_salt, password_hash) VALUES (?, ?, ?, '', ?, ?)"
                    ).use { statement ->
                        statement.setString(1, user.id)
                        statement.setString(2, user.name)
                        statement.setString(3, user.email)
                        statement.setBytes(4, salt)
                        statement.setBytes(5, passwordHash)
                        statement.executeUpdate()
                    }
                    database.prepareStatement("INSERT INTO google_accounts (subject, user_id) VALUES (?, ?)").use { statement ->
                        statement.setString(1, googleSubject)
                        statement.setString(2, user.id)
                        statement.executeUpdate()
                    }
                }
                database.prepareStatement(
                    "INSERT INTO firebase_accounts (firebase_uid, user_id) VALUES (?, ?)"
                ).use { statement ->
                    statement.setString(1, firebaseUid)
                    statement.setString(2, user.id)
                    statement.executeUpdate()
                }
                database.commit()
            } catch (_: SQLException) {
                database.rollback()
                throw ApiException(HttpStatusCode.Conflict, "This Firebase account is already linked to another CourtFinder account.")
            } finally {
                database.autoCommit = true
            }
            user
        }
    }

    suspend fun deleteAccount(userId: String) = withContext(Dispatchers.IO) {
        connection().use { database ->
            database.autoCommit = false
            try {
                database.prepareStatement("DELETE FROM bookings WHERE user_id = ?").use { statement ->
                    statement.setString(1, userId)
                    statement.executeUpdate()
                }
                database.prepareStatement("DELETE FROM google_accounts WHERE user_id = ?").use { statement ->
                    statement.setString(1, userId)
                    statement.executeUpdate()
                }
                database.prepareStatement("DELETE FROM firebase_accounts WHERE user_id = ?").use { statement ->
                    statement.setString(1, userId)
                    statement.executeUpdate()
                }
                val deleted = database.prepareStatement("DELETE FROM users WHERE id = ?").use { statement ->
                    statement.setString(1, userId)
                    statement.executeUpdate()
                }
                if (deleted == 0) throw ApiException(HttpStatusCode.NotFound, "Account was not found.")
                database.commit()
            } catch (error: Exception) {
                database.rollback()
                throw error
            } finally {
                database.autoCommit = true
            }
        }
    }

    suspend fun searchCourts(query: String): List<CourtDto> = withContext(Dispatchers.IO) {
        val allCourts = connection().use { database ->
            database.prepareStatement("SELECT * FROM courts ORDER BY rating DESC").use { statement ->
                statement.executeQuery().use { result ->
                    buildList {
                        while (result.next()) {
                            add(
                                CourtDto(
                                    id = result.getString("id"),
                                    name = result.getString("name"),
                                    sportType = result.getString("sport_type"),
                                    location = result.getString("location"),
                                    distanceKm = result.getDouble("distance_km"),
                                    latitude = result.getDouble("latitude"),
                                    longitude = result.getDouble("longitude"),
                                    pricePerHour = result.getString("price_per_hour"),
                                    rating = result.getDouble("rating"),
                                    availableTimeSlots = Json.decodeFromString(result.getString("time_slots")),
                                    aiMatchScore = result.getInt("ai_match_score"),
                                    amenities = Json.decodeFromString(result.getString("amenities"))
                                )
                            )
                        }
                    }
                }
            }
        }
        val sport = listOf("Tennis", "Badminton", "Basketball", "Padel", "Pickleball")
            .firstOrNull { query.contains(it, ignoreCase = true) }
        if (sport == null) allCourts else allCourts.filter { it.sportType.equals(sport, ignoreCase = true) }
    }

    suspend fun book(userId: String, request: BookingRequestDto): BookingConfirmationDto = withContext(Dispatchers.IO) {
        connection().use { database ->
            val courtName = database.prepareStatement("SELECT name, time_slots FROM courts WHERE id = ?").use { statement ->
                statement.setString(1, request.courtId)
                statement.executeQuery().use { result ->
                    if (!result.next()) throw ApiException(HttpStatusCode.NotFound, "Court was not found.")
                    val slots = Json.decodeFromString<List<String>>(result.getString("time_slots"))
                    if (request.timeSlot !in slots) {
                        throw ApiException(HttpStatusCode.BadRequest, "That time slot is not available for this court.")
                    }
                    result.getString("name")
                }
            }
            val date = LocalDate.now().toString()
            val bookingId = "BK-${UUID.randomUUID().toString().take(8).uppercase(Locale.ROOT)}"
            try {
                database.prepareStatement(
                    "INSERT INTO bookings (id, user_id, court_id, time_slot, booking_date) VALUES (?, ?, ?, ?, ?)"
                ).use { statement ->
                    statement.setString(1, bookingId)
                    statement.setString(2, userId)
                    statement.setString(3, request.courtId)
                    statement.setString(4, request.timeSlot)
                    statement.setString(5, date)
                    statement.executeUpdate()
                }
            } catch (_: SQLException) {
                throw ApiException(HttpStatusCode.Conflict, "That time slot has already been booked.")
            }
            BookingConfirmationDto(bookingId, courtName, request.timeSlot, date)
        }
    }

    private fun initialize() {
        connection().use { database ->
            database.createStatement().use { statement ->
                val binaryType = if (usesPostgres) "BYTEA" else "BLOB"
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id TEXT PRIMARY KEY,
                        full_name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        phone TEXT NOT NULL DEFAULT '',
                        password_salt $binaryType NOT NULL,
                        password_hash $binaryType NOT NULL
                    )
                """.trimIndent())
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS courts (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        sport_type TEXT NOT NULL,
                        location TEXT NOT NULL,
                        distance_km REAL NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        price_per_hour TEXT NOT NULL,
                        rating REAL NOT NULL,
                        time_slots TEXT NOT NULL,
                        ai_match_score INTEGER NOT NULL,
                        amenities TEXT NOT NULL
                    )
                """.trimIndent())
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS bookings (
                        id TEXT PRIMARY KEY,
                        user_id TEXT NOT NULL REFERENCES users(id),
                        court_id TEXT NOT NULL REFERENCES courts(id),
                        time_slot TEXT NOT NULL,
                        booking_date TEXT NOT NULL,
                        UNIQUE(court_id, time_slot, booking_date)
                    )
                """.trimIndent())
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS google_accounts (
                        subject TEXT PRIMARY KEY,
                        user_id TEXT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS firebase_accounts (
                        firebase_uid TEXT PRIMARY KEY,
                        user_id TEXT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())
            }
            if (seedDemoCourts) seedCourts(database)
        }
    }

    private fun seedCourts(database: Connection) {
        val courts = listOf(
            CourtDto("c1", "Apex Tennis Arena & Academy", "Tennis", "Downtown Sports Hub, Sector 4", 1.8, 37.7749, -122.4194, "$25/hr", 4.9, listOf("06:00 PM", "07:00 PM", "09:00 PM"), 98, amenities = listOf("LED Floodlights", "Free Parking", "Changing Rooms", "Racket Rental")),
            CourtDto("c2", "Smash Badminton Indoor Club", "Badminton", "Eastside Fitness Park", 3.2, 37.7849, -122.4094, "$18/hr", 4.7, listOf("05:30 PM", "08:00 PM"), 94, amenities = listOf("Air Conditioned", "Shoe Rental", "Hydration Station")),
            CourtDto("c3", "Urban Hoops Basketball Arena", "Basketball", "Central Stadium Complex", 4.5, 37.7649, -122.4294, "$30/hr", 4.8, listOf("07:00 PM", "08:30 PM"), 91, amenities = listOf("Sound System", "Night Lights", "Lockers & Showers")),
            CourtDto("c4", "Pro Padel Court & Lounge", "Padel", "Green Valley Sports Club", 5.0, 37.7549, -122.4394, "$35/hr", 4.9, listOf("06:00 PM", "07:30 PM"), 89, amenities = listOf("Cafe Lounge", "Glass Panoramic Courts", "Valet Parking")),
            CourtDto("c5", "Rally Pickleball Center", "Pickleball", "Westside Community Sports Park", 2.6, 37.7949, -122.3994, "$22/hr", 4.6, listOf("05:00 PM", "06:30 PM", "08:00 PM"), 92, amenities = listOf("Court Lighting", "Free Parking", "Water Station"))
        )
        database.prepareStatement(
            "INSERT INTO courts (id, name, sport_type, location, distance_km, latitude, longitude, price_per_hour, rating, time_slots, ai_match_score, amenities) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING"
        ).use { statement ->
            courts.forEach { court ->
                statement.setString(1, court.id)
                statement.setString(2, court.name)
                statement.setString(3, court.sportType)
                statement.setString(4, court.location)
                statement.setDouble(5, court.distanceKm)
                statement.setDouble(6, court.latitude)
                statement.setDouble(7, court.longitude)
                statement.setString(8, court.pricePerHour)
                statement.setDouble(9, court.rating)
                statement.setString(10, Json.encodeToString(court.availableTimeSlots))
                statement.setInt(11, court.aiMatchScore)
                statement.setString(12, Json.encodeToString(court.amenities))
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private fun connection(): Connection {
        return if (usesPostgres) {
            DriverManager.getConnection(databaseUrl, postgresProperties())
        } else {
            DriverManager.getConnection(databaseUrl).also { database ->
                database.createStatement().use { it.execute("PRAGMA foreign_keys = ON") }
            }
        }
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val passwordSpec = PBEKeySpec(password.toCharArray(), salt, 210_000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(passwordSpec).encoded
        } finally {
            passwordSpec.clearPassword()
        }
    }

    private data class UserPasswordRecord(
        val id: String,
        val name: String,
        val email: String,
        val salt: ByteArray,
        val passwordHash: ByteArray
    )
}
