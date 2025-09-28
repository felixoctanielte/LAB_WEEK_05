package com.example.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_week_05.api.CatApiService
import com.example.lab_week_05.model.ImageData
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    // 1. Retrofit instance pakai Moshi
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    // 2. Service instance
    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    // 3. Reference ke TextView
    private val apiResponseView: TextView by lazy {
        findViewById(R.id.api_response)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 4. Panggil API
        getCatImageResponse()
    }

    // 5. Function untuk request API
    private fun getCatImageResponse() {
        val call = catApiService.searchImages(1, "full")

        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "Failed to get response", t)
            }

            override fun onResponse(
                call: Call<List<ImageData>>,
                response: Response<List<ImageData>>
            ) {
                if (response.isSuccessful) {
                    val images = response.body()
                    val firstImageUrl = images?.firstOrNull()?.imageUrl ?: "No URL Found"
                    apiResponseView.text =
                        getString(R.string.image_placeholder, firstImageUrl)
                } else {
                    Log.e(
                        MAIN_ACTIVITY,
                        "Failed to get response\n" +
                                response.errorBody()?.string().orEmpty()
                    )
                }
            }
        })
    }

    companion object {
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
    }
}
