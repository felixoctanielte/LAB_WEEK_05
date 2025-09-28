package com.example.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_week_05.api.CatApiService
import com.example.lab_week_05.model.ImageData
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MainActivity : AppCompatActivity() {

    // Retrofit instance
    private val retrofit by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory()) // ✅ support Kotlin data class
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Service instance
    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    // UI references
    private val apiResponseView: TextView by lazy {
        findViewById(R.id.api_response)
    }

    private val imageResultView: ImageView by lazy {
        findViewById(R.id.image_result)
    }

    // Image loader (pakai Glide)
    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getCatImageResponse()
    }

    // Function untuk request API
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
                    val imageList = response.body()

                    // 🔑 Cari image pertama yang punya URL valid
                    val firstImage = imageList?.firstOrNull { !it.imageUrl.isNullOrBlank() }

                    val imageUrl = firstImage?.imageUrl.orEmpty()
                    val breedName = firstImage?.breeds?.firstOrNull()?.name ?: "Unknown"

                    if (imageUrl.isNotBlank()) {
                        imageLoader.loadImage(imageUrl, imageResultView)
                        Log.d(MAIN_ACTIVITY, "Loaded image: $imageUrl")
                    } else {
                        Log.w(MAIN_ACTIVITY, "No valid image URL found")
                    }

                    // Update TextView dengan nama breed
                    apiResponseView.text =
                        getString(R.string.breed_placeholder, breedName)

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
