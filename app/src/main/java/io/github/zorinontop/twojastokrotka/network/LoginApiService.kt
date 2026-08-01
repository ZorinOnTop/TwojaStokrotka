package io.github.zorinontop.twojastokrotka.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

data class SmsRequest(
    val applicationKey: String = "wczBi+C1LD9",
    val phoneNumber: String
)

data class SmsResponse(
    val success: Boolean?,
    val message: String?
)

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val id_token: String?,
    val refresh_token: String?
)

data class TokenErrorResponse(
    val error: String,
    val error_description: String?,
    val error_uri: String?
)

interface LoginApiService {
    @POST("web/v1/SignInMobile/sendSms")
    suspend fun sendSms(@Body request: SmsRequest): retrofit2.Response<SmsResponse>

    @FormUrlEncoded
    @POST("connect/token")
    suspend fun verifyOtp(
        @Field("client_id") clientId: String = "stokrotka_mobile_app",
        @Field("grant_type") grantType: String = "otp_code",
        @Field("otp_code") otpCode: String,
        @Field("phone_number") phoneNumber: String, // Should include +48
        @Field("scope") scope: String = "openid profile phone stokrotka_mobile_api offline_access"
    ): retrofit2.Response<TokenResponse>

    @FormUrlEncoded
    @POST("connect/token")
    suspend fun refreshToken(
        @Field("client_id") clientId: String = "stokrotka_mobile_app",
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("scope") scope: String = "openid profile phone stokrotka_mobile_api offline_access"
    ): retrofit2.Response<TokenResponse>

    companion object {
        private const val BASE_URL = "https://login.stk.loyaltydrive.pl/"

        fun create(): LoginApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LoginApiService::class.java)
        }
    }
}
