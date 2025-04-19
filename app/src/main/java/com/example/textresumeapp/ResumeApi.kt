// ResumeApi.kt

import retrofit2.http.GET
import retrofit2.http.Query

interface ResumeApi {
    @GET("resume")
    suspend fun getResume(@Query("name") name: String): Resume
}
