package com.example.courtfinder.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.algorithms.Algorithm
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.HttpStatusCode
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Date
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

@Serializable
data class LoginRequestDto(val emailOrPhone: String, val password: String)

@Serializable
data class RegisterRequestDto(
    val fullName: String,
    val email: String,
    val phone: String = "",
    val password: String
)

@Serializable
data class GoogleSignInRequestDto(val idToken: String)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val isGuest: Boolean = false
)

@Serializable
data class AuthResponseDto(
    val success: Boolean,
    val token: String? = null,
    val user: UserDto? = null,
    val errorMessage: String? = null
)

@Serializable
data class CourtDto(
    val id: String,
    val name: String,
    val sportType: String,
    val location: String,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double,
    val pricePerHour: String,
    val rating: Double,
    val availableTimeSlots: List<String>,
    val aiMatchScore: Int,
    val isFavorite: Boolean = false,
    val hasAlertSet: Boolean = false,
    val amenities: List<String>
)

@Serializable
data class BookingRequestDto(val courtId: String, val timeSlot: String)

@Serializable
data class BookingConfirmationDto(
    val bookingId: String,
    val courtName: String,
    val timeSlot: String,
    val date: String
)

@Serializable
data class ApiErrorDto(val errorMessage: String)

class ApiException(val status: HttpStatusCode, override val message: String) : RuntimeException(message)

fun main() {
    val port = System.getenv("COURTFINDER_PORT")?.toIntOrNull()
        ?: System.getenv("PORT")?.toIntOrNull()
        ?: 8080
    val databasePath = System.getenv("COURTFINDER_DB_PATH") ?: "data/courtfinder.db"
    val environment = System.getenv("COURTFINDER_ENV")?.lowercase() ?: "production"
    val databaseUrl = System.getenv("DATABASE_URL")?.takeIf { it.isNotBlank() }
    if (environment == "production" && databaseUrl == null) {
        error("DATABASE_URL must be configured in production. SQLite is for local development only.")
    }
    val configuredSecret = System.getenv("COURTFINDER_JWT_SECRET")
    val jwtSecret = when {
        configuredSecret != null -> configuredSecret
        environment == "development" -> "local-development-secret-change-this-before-deploying"
        else -> error("COURTFINDER_JWT_SECRET must be configured outside development.")
    }
    require(jwtSecret.length >= 32) { "COURTFINDER_JWT_SECRET must contain at least 32 characters." }
    val googleWebClientId = System.getenv("COURTFINDER_GOOGLE_WEB_CLIENT_ID").orEmpty()
    val firebaseProjectId = System.getenv("COURTFINDER_FIREBASE_PROJECT_ID")
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv("GOOGLE_CLOUD_PROJECT")?.takeIf { it.isNotBlank() }.orEmpty()
    val firebaseAuth = initializeFirebaseAuth(firebaseProjectId)
    if (firebaseProjectId.isNotBlank() && firebaseAuth == null) {
        System.err.println("Firebase Admin credentials are unavailable; Firebase Google sign-in will return 503.")
    }
    val database = CourtDatabase(
        databasePath = databasePath,
        seedDemoCourts = environment == "development",
        remoteDatabaseUrl = databaseUrl
    )

    embeddedServer(Netty, host = "0.0.0.0", port = port) {
        courtFinderModule(database, jwtSecret, googleWebClientId, firebaseProjectId, firebaseAuth)
    }.start(wait = true)
}

private fun initializeFirebaseAuth(projectId: String): FirebaseAuth? {
    if (projectId.isBlank()) return null
    return try {
        val app = FirebaseApp.getApps().firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
            ?: FirebaseApp.initializeApp(
                FirebaseOptions.builder()
                    .setCredentials(firebaseAdminCredentials())
                    .setProjectId(projectId)
                    .build()
            )
        FirebaseAuth.getInstance(app)
    } catch (_: Exception) {
        null
    }
}

private fun firebaseAdminCredentials(): GoogleCredentials {
    val serviceAccountJson = System.getenv("COURTFINDER_FIREBASE_SERVICE_ACCOUNT_JSON")
        ?.takeIf { it.isNotBlank() }
    return if (serviceAccountJson != null) {
        GoogleCredentials.fromStream(ByteArrayInputStream(serviceAccountJson.toByteArray(Charsets.UTF_8)))
    } else {
        GoogleCredentials.getApplicationDefault()
    }
}

private fun Application.courtFinderModule(
    database: CourtDatabase,
    jwtSecret: String,
    googleWebClientId: String,
    firebaseProjectId: String,
    firebaseAuth: FirebaseAuth?
) {
    val issuer = "court-finder-api"
    val audience = "court-finder-android"
    val algorithm = Algorithm.HMAC256(jwtSecret)
    val verifier = JWT.require(algorithm).withIssuer(issuer).withAudience(audience).build()
    val googleVerifier = googleWebClientId.takeIf { it.isNotBlank() }?.let { clientId ->
        GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(clientId))
            .build()
    }
    val firebaseTokenVerifier = firebaseAuth?.let(::FirebaseGoogleTokenVerifier)

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiErrorDto("Request body is invalid."))
        }
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ApiErrorDto(cause.message))
        }
        exception<Throwable> { call, cause ->
            if (cause is CancellationException) throw cause
            call.respond(HttpStatusCode.InternalServerError, ApiErrorDto("The server could not complete the request."))
        }
    }
    install(Authentication) {
        jwt("court-finder-auth") {
            verifier(verifier)
            validate { credential ->
                if (credential.payload.subject.isNullOrBlank()) null else JWTPrincipal(credential.payload)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ApiErrorDto("Sign in to make a booking."))
            }
        }
    }

    routing {
        get("/delete-account") {
            val page = Thread.currentThread().contextClassLoader
                .getResourceAsStream("public/delete-account.html")
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: throw IllegalStateException("Account deletion page is missing.")
            val safeClientId = googleWebClientId
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
            call.respondText(page.replace("__GOOGLE_WEB_CLIENT_ID__", safeClientId), ContentType.Text.Html)
        }
        get("/api/health") {
            call.respond(mapOf("status" to "ok"))
        }
        post("/api/auth/register") {
            val request = call.receive<RegisterRequestDto>()
            val user = database.register(request)
            call.respond(createAuthResponse(user, algorithm, issuer, audience))
        }
        post("/api/auth/login") {
            val request = call.receive<LoginRequestDto>()
            val user = database.login(request)
            call.respond(createAuthResponse(user, algorithm, issuer, audience))
        }
        post("/api/auth/google") {
            val request = call.receive<GoogleSignInRequestDto>()
            val audiences = try {
                JWT.decode(request.idToken).audience.orEmpty()
            } catch (_: JWTDecodeException) {
                throw ApiException(HttpStatusCode.Unauthorized, "Google sign-in token is invalid or expired.")
            }
            val account = if (firebaseProjectId.isNotBlank() && firebaseProjectId in audiences) {
                val configuredVerifier = firebaseTokenVerifier
                    ?: throw ApiException(
                        HttpStatusCode.ServiceUnavailable,
                        "Firebase Google sign-in verification is unavailable. Configure Firebase Admin credentials on the service."
                    )
                withContext(Dispatchers.IO) { configuredVerifier.verify(request.idToken) }
                    ?: throw ApiException(
                        HttpStatusCode.Unauthorized,
                        "Firebase Google sign-in token is invalid, expired, or not linked to a verified Google account."
                    )
            } else {
                val configuredVerifier = googleVerifier
                    ?: throw ApiException(
                        HttpStatusCode.ServiceUnavailable,
                        "Google sign-in is not configured. Set COURTFINDER_GOOGLE_WEB_CLIENT_ID on the service."
                    )
                val googleToken = withContext(Dispatchers.IO) {
                    configuredVerifier.verify(request.idToken)
                } ?: throw ApiException(HttpStatusCode.Unauthorized, "Google sign-in token is invalid or expired.")
                val payload = googleToken.payload
                if (payload.emailVerified != true || payload.email.isNullOrBlank()) {
                    throw ApiException(HttpStatusCode.Unauthorized, "Google account must have a verified email address.")
                }
                VerifiedGoogleAccount(payload.subject, payload.email, payload["name"] as? String)
            }
            val user = if (firebaseProjectId.isNotBlank() && firebaseProjectId in audiences) {
                database.loginWithFirebaseGoogle(
                    account.firebaseUid ?: throw ApiException(
                        HttpStatusCode.Unauthorized,
                        "Firebase Google identity is incomplete."
                    ),
                    account.subject,
                    account.email,
                    account.name
                )
            } else {
                database.loginWithGoogle(account.subject, account.email, account.name)
            }
            call.respond(createAuthResponse(user, algorithm, issuer, audience))
        }
        get("/api/courts/search") {
            call.respond(database.searchCourts(call.request.queryParameters["query"].orEmpty()))
        }
        authenticate("court-finder-auth") {
            get("/api/auth/me") {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject
                    ?: throw ApiException(HttpStatusCode.Unauthorized, "Sign in to continue.")
                call.respond(database.getUser(userId))
            }
            delete("/api/account") {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject
                    ?: throw ApiException(HttpStatusCode.Unauthorized, "Sign in before deleting your account.")
                database.deleteAccount(userId)
                call.respond(HttpStatusCode.NoContent)
            }
            post("/api/bookings") {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject
                    ?: throw ApiException(HttpStatusCode.Unauthorized, "Sign in to make a booking.")
                val request = call.receive<BookingRequestDto>()
                call.respond(database.book(userId, request))
            }
        }
    }
}

private fun createAuthResponse(
    user: UserDto,
    algorithm: Algorithm,
    issuer: String,
    audience: String
): AuthResponseDto {
    val token = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withSubject(user.id)
        .withClaim("email", user.email)
        .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L))
        .sign(algorithm)
    return AuthResponseDto(success = true, token = token, user = user)
}
