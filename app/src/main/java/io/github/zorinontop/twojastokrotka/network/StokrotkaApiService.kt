package io.github.zorinontop.twojastokrotka.network

import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class UserResponse(val data: UserData?, val status: Int)
data class UserData(val customerId: Int, val loyaltyCardNumber: String, val firstName: String, val phoneNumber: String, val isAdult: Boolean, val totalSavings: Double, val properties: Map<String, String>?)

data class MessageListResponse(val data: List<MessageData>?, val status: Int)
data class MessageData(val id: String, val title: String, val body: String, val isOpen: Boolean, val sysdat: String)

data class PrizeListResponse(val data: PrizeListData?, val status: Int)
data class PrizeListData(val walletDetails: WalletDetails?, val offers: List<Offer>?)
data class WalletDetails(val balance: Int)
data class Offer(
    val offerId: Int,
    val prize: Prize,
    val pointCost: Int,
    val isFavorite: Boolean = false,
    val category: String? = null,
    val activatedCount: Int = 0
)
data class Prize(val id: Int, val banner: PrizeBanner)
data class PrizeBanner(val properties: BannerProperties)
data class BannerProperties(
    val title: String,
    val additional1: String?,
    val additional2: String?, // Used for "Limit 1 szt."
    val prizeCard: String?,
    val imageUrl: String?,
    val imageUrl_L: String?,
    val imageUrl_M: String?,
    val imageUrl_S: String?,
    val linkUrl: String?,
    val textHtml: String?
)

data class StoreResponse(val data: StoreData?, val status: Int)
data class StoreData(val storeId: String, val name: String, val address: StoreAddress)
data class StoreAddress(val street: String, val postCode: String, val city: String)

data class BannerListResponse(val data: List<StokrotkaBanner>?, val status: Int)
data class StokrotkaBanner(val id: Int, val properties: BannerProperties, val displayOrder: Int)

data class MagazineResponse(val data: MagazineData?, val status: Int)
data class MagazineListResponse(val data: List<MagazineData>?, val status: Int)
data class MagazineData(val id: Int, val name: String, val dateFrom: String?, val dateTo: String?, val linkUrl: String?, val pages: List<MagazinePage>?, val banner: PrizeBanner?, val isUserStoreMagazine: Boolean = false)
data class MagazinePage(val page: Int, val imageUrl: String, val products: List<String>)

data class UserPrizesResponse(val data: List<UserPrizeData>?, val status: Int)
data class UserPrizeData(val prize: UserPrize)
data class UserPrize(val id: Int, val activeFrom: String, val activeTo: String, val banner: PrizeBanner, val isFavorite: Boolean? = false)
data class GenericPrizeResponse(val data: List<UserPrize>?, val status: Int)

data class SingleOfferResponse(
    val data: Offer?,
    val status: Int
)

data class TransferHistoryRequest(
    val skip: Int,
    val take: Int
)

data class WalletResponse(val data: WalletData?, val status: Int)
data class WalletData(val balance: Double)

data class WalletHistoryResponse(val data: List<HistoryEntry>?, val status: Int)
data class HistoryEntry(
    val type: HistoryType,
    val name: String,
    val points: Int,
    val operationDate: String
)
data class HistoryType(val id: Int, val name: String)

data class TransferHistoryResponse(val data: TransferHistoryData?, val status: Int)
data class TransferHistoryData(val elements: List<TransferElement>?, val total: Int)
data class TransferElement(
    val transferId: Int,
    val phoneNumber: String,
    val comment: String?,
    val transferDate: String,
    val points: Int,
    val status: String,
    val walletId: Int
)

data class TransactionListResponse(val data: TransactionListData?, val status: Int)
data class TransactionListData(val savings: Double, val transactionList: List<TransactionEntry>?)
data class TransactionEntry(
    val receiptId: Long,
    val transactionDateTime: String,
    val receiptNo: String,
    val grossValue: Double,
    val storeName: String,
    val savings: Double
)

data class TransactionDetailResponse(val data: TransactionDetailData?, val status: Int)
data class TransactionDetailData(
    val store: StoreData?,
    val lines: List<ReceiptLine>?,
    val total: Double,
    val receiptNo: String,
    val transactionDate: String
)
data class ReceiptLine(
    val productName: String,
    val originalPrice: Double,
    val quantity: Double,
    val grossValue: Double
)

interface StokrotkaApiService {
    @GET("api/v1/User/me")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserResponse

    @GET("api/v1/Message/list")
    suspend fun getMessages(@Header("Authorization") token: String): MessageListResponse

    @GET("api/v1/wallet/1/prizelist")
    suspend fun getPrizes(@Header("Authorization") token: String, @Query("includeBanners") includeBanners: Boolean = true, @Query("includeWallet") includeWallet: Boolean = true): PrizeListResponse

    @GET("api/v1/Store/{id}")
    suspend fun getStore(@Header("Authorization") token: String, @Path("id") storeId: String): StoreResponse

    @GET("api/v1/banner")
    suspend fun getBanners(@Header("Authorization") token: String, @Query("path") path: String = "/application_screens/home/banery", @Query("pageSize") pageSize: Int = 50): BannerListResponse

    @GET("api/v1/Magazine/{id}")
    suspend fun getMagazine(@Header("Authorization") token: String, @Path("id") magazineId: String): MagazineResponse

    @GET("api/v1/Magazine")
    suspend fun getAllMagazines(@Header("Authorization") token: String): MagazineListResponse

    @GET("api/v1/prize/userprizes")
    suspend fun getUserPrizes(@Header("Authorization") token: String, @Query("pageSize") pageSize: Int = 15, @Query("includeBanner") includeBanner: Boolean = true, @Query("statusFilters") statusFilters: List<Int> = listOf(1, 2), @Query("path") path: String = "/application_screens/home/kupony"): UserPrizesResponse

    @GET("api/v1/prize")
    suspend fun getPrizesByPath(@Header("Authorization") token: String, @Query("path") path: String, @Query("includeBanner") includeBanner: Boolean = true, @Query("pageSize") pageSize: Int = 30): GenericPrizeResponse

    @GET("api/v1/Wallet/offer/{id}")
    suspend fun getOfferDetails(
        @Header("Authorization") token: String,
        @Path("id") offerId: Int,
        @Query("includeBanner") includeBanner: Boolean = true
    ): SingleOfferResponse

    @POST("api/v1/Wallet/offer/{id}")
    suspend fun activateOffer(
        @Header("Authorization") token: String,
        @Path("id") offerId: Int,
        @Query("count") count: Int
    ): retrofit2.Response<Unit>

    @DELETE("api/v1/Wallet/offer/{id}")
    suspend fun deactivateOffer(
        @Header("Authorization") token: String,
        @Path("id") offerId: Int,
        @Query("count") count: Int
    ): retrofit2.Response<Unit>

    @retrofit2.http.PUT("api/v1/Prize/{id}/set-favorite")
    suspend fun setFavorite(
        @Header("Authorization") token: String,
        @Path("id") prizeId: Int
    ): retrofit2.Response<Unit>

    @DELETE("api/v1/Prize/{id}/delete-favorite")
    suspend fun deleteFavorite(
        @Header("Authorization") token: String,
        @Path("id") prizeId: Int
    ): retrofit2.Response<Unit>

    @GET("api/v1/Wallet/1")
    suspend fun getWalletDetails(@Header("Authorization") token: String): WalletResponse

    @GET("api/v1/Wallet/1/history")
    suspend fun getWalletHistory(@Header("Authorization") token: String): WalletHistoryResponse

    @POST("api/v1/Wallet/transfer")
    suspend fun transferPetals(
        @Header("Authorization") token: String,
        @Query("Comment") comment: String,
        @Query("Mobile") mobile: String,
        @Query("Value") value: Int,
        @Query("WalletId") walletId: Int = 1
    ): retrofit2.Response<Unit>

    @POST("api/v1/Wallet/transfer-history")
    suspend fun getTransferHistory(
        @Header("Authorization") token: String,
        @retrofit2.http.Body request: TransferHistoryRequest = TransferHistoryRequest(0, 20),
        @Header("Content-Type") contentType: String = "application/json"
    ): TransferHistoryResponse

    @POST("api/v1/Wallet/transfer-cancel")
    suspend fun cancelTransfer(
        @Header("Authorization") token: String,
        @Query("transferId") transferId: Int
    ): retrofit2.Response<Unit>

    @GET("api/v1/Transaction/list")
    suspend fun getTransactionList(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 25
    ): TransactionListResponse

    @GET("api/v1/Transaction/{id}")
    suspend fun getTransactionDetail(
        @Header("Authorization") token: String,
        @Path("id") receiptId: Long
    ): TransactionDetailResponse

    companion object {
        private const val BASE_URL = "https://mobile.api.stk.loyaltydrive.pl/"

        fun create(sessionManager: SessionManager, onAuthError: () -> Unit = {}): StokrotkaApiService {
            val client = OkHttpClient.Builder()
                .authenticator(TokenAuthenticator(sessionManager, onAuthError))
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(StokrotkaApiService::class.java)
        }
    }
}

class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val onAuthError: () -> Unit
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = runBlocking { sessionManager.refreshToken.first() } ?: return null

        val loginService = LoginApiService.create()
        val tokenResponse = runBlocking {
            try {
                loginService.refreshToken(refreshToken = refreshToken)
            } catch (e: Exception) {
                null
            }
        }

        if (tokenResponse?.isSuccessful == true) {
            val newTokens = tokenResponse.body()
            if (newTokens != null) {
                runBlocking {
                    sessionManager.saveSession(newTokens.access_token, newTokens.refresh_token)
                }
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.access_token}")
                    .build()
            }
        }

        runBlocking { sessionManager.clearSession() }
        onAuthError()
        return null
    }
}
