package dev.igorxp5.applada.data.source.remote

import dev.igorxp5.applada.data.Match
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

    companion object {
        const val API_BASE_URL = "https://crudcrud.com/api/8d261fd5d9e040cba9f655ed431dd6f7/"
    }
}