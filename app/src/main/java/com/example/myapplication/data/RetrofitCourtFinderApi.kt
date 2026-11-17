package com.example.myapplication.data

import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response

private interface CourtFinderService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleSignInRequest): AuthResponse

    @GET("api/courts/search")
    suspend fun searchCourts(@Query("query") query: String): List<Court>

    @POST("api/bookings")
    suspend fun bookCourt(
        @Header("Authorization") authorization: String,
        @Body request: BookingRequest
    ): BookingConfirmation

    @GET("api/auth/me")
    suspend fun getCurrentUser(@Header("Authorization") authorization: String): User

    @DELETE("api/account")
    suspend fun deleteAccount(@Header("Authorization") authorization: String): Response<Unit>
}

private data class GoogleSignInRequest(val idToken: String)

class RetrofitCourtFinderApi(
    baseUrl: String,
    private val sessionStore: SessionStore
) : CourtFinderApi {
    private val service: CourtFinderService
    @Volatile
    private var authToken: String? = sessionStore.readToken()

    init {
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        service = Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CourtFinderService::class.java)
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return apiCall {
            service.login(request).also { response ->
                if (response.success) authToken = response.token
                if (response.success) response.token?.let(sessionStore::saveToken)
            }
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return apiCall {
            service.register(request).also { response ->
                if (response.success) authToken = response.token
                if (response.success) response.token?.let(sessionStore::saveToken)
            }
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return apiCall {
            service.loginWithGoogle(GoogleSignInRequest(idToken)).also { response ->
                if (response.success) authToken = response.token
                if (response.success) response.token?.let(sessionStore::saveToken)
            }
        }
    }

    override suspend fun searchCourts(query: String): Result<List<Court>> {
        return apiCall { service.searchCourts(query) }
    }

    override suspend fun bookCourt(courtId: String, timeSlot: String): Result<BookingConfirmation> {
        val token = authToken ?: return Result.failure(IllegalStateException("Sign in before booking a court."))
        return apiCall {
            service.bookCourt("Bearer $token", BookingRequest(courtId, timeSlot))
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        val token = authToken ?: return Result.success(null)
        return apiCall { service.getCurrentUser("Bearer $token") }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val token = authToken ?: return Result.failure(IllegalStateException("Sign in before deleting your account."))
        return apiCall {
            val response = service.deleteAccount("Bearer $token")
            if (!response.isSuccessful) throw HttpException(response)
            clearSession()
        }
    }

    override fun clearSession() {
        authToken = null
        sessionStore.clear()
    }

    private suspend fun <T> apiCall(request: suspend () -> T): Result<T> {
        return try {
            Result.success(request())
        } catch (error: HttpException) {
            Result.failure(IllegalStateException("Server request failed (${error.code()})."))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}

class UnavailableCourtFinderApi : CourtFinderApi {
    private fun <T> unavailable(): Result<T> =
        Result.failure(IllegalStateException("Set API_BASE_URL in local.properties to connect to the CourtFinder service."))

    override suspend fun login(request: LoginRequest) = unavailable<AuthResponse>()
    override suspend fun register(request: RegisterRequest) = unavailable<AuthResponse>()
    override suspend fun loginWithGoogle(idToken: String) = unavailable<AuthResponse>()
    override suspend fun searchCourts(query: String) = unavailable<List<Court>>()
    override suspend fun bookCourt(courtId: String, timeSlot: String) = unavailable<BookingConfirmation>()
    override suspend fun deleteAccount() = unavailable<Unit>()
}
