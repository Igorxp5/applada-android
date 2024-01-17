package dev.igorxp5.applada.data.source.remote

import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.Subscription
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AppLadaApi {
    @GET("matches/{id}")
    suspend fun getMatchById(@Path("id") matchId: Int): Match

    @GET("matches")
    suspend fun getNearMatches(@Query("latitude") latitude: Double,
                       @Query("longitude") longitude: Double,
                       @Query("radius") radius: Double): List<Match>

    @POST("matches")
    suspend fun createMatch(@Body match: Match): Match

    @GET("subscriptions")
    suspend fun getUserSubscriptions(): List<Subscription>

    @POST("subscriptions")
    suspend fun createUserSubscription(@Body subscription: Subscription): Subscription

    @DELETE("subscriptions/{id}")
    suspend fun deleteUserSubscription(@Path("id") subscriptionId: String)

    companion object {
        const val API_BASE_URL = "https://crudcrud.com/api/af534c5941f94ac4be0d3bdf6634c3ef/"
    }
}