package com.ottd.libs.framework.network

import android.content.Context
import android.util.Log
import com.ottd.libs.cache.CacheManager
import com.ottd.libs.framework.model.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp4k.Http
import okhttp4k.OkhttpBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


/**
 * Created by enzowei on 2018/2/5.
 */
const val TIME_OUT_READ = 5000L
const val TIME_OUT_CONNECTION = 5000L
const val TIME_OUT_WRITE = 5000L

const val CACHE_SIZE_DISK = (1024 * 1024 * 5).toLong()
const val CACHE_SIZE_MEMORY = 1024 * 1024 * 5

const val BASE_URL = "https://api.readhub.me/"
const val BASE_UPDATE_URL = "http://readhub.bihe0832.com/"

object ReadHubApi {
	lateinit var apiService: ApiService
	lateinit var aboutService: AboutService

	fun init(context: Context, debug: Boolean = false) {
		val client = OkhttpBuilder().build {
			connectTimeout = TIME_OUT_CONNECTION
			readTimeout = TIME_OUT_READ
			writeTimeout = TIME_OUT_WRITE
			cache {
				CacheManager.getInstance().init(context, CACHE_SIZE_DISK, CACHE_SIZE_MEMORY, File(context.applicationContext.cacheDir, "response").absolutePath)
				val httpCacheDirectory = File(context.applicationContext.cacheDir, "responses")
				Cache(httpCacheDirectory, CACHE_SIZE_DISK)
			}
			interceptor {
				HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
					//打印retrofit日志
					Log.d("RetrofitLog", "retrofitMsg = " + message)
				}).apply {
					level = if (debug) {
						HttpLoggingInterceptor.Level.BODY
					} else {
						HttpLoggingInterceptor.Level.BASIC
					}
				}
			}
		}

		apiService = Retrofit.Builder()
				.client(client)
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(BASE_URL)
				.build()
				.create(ApiService::class.java)

		aboutService = Retrofit.Builder()
				.client(client)
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(BASE_UPDATE_URL)
				.build()
				.create(AboutService::class.java)
	}

}

interface AboutService {
	@POST("cgi/readhub.php")
	fun checkUpdate(@Body body: RequestBody): Call<ApplicationInfo>
}
interface ApiService {
	@GET("topic")
	fun topic(@Query("lastCursor") lastCursor: String,
						@Query("pageSize") pageSize: Int): Call<TopicList>

	@GET("news")
	fun news(@Query("lastCursor") lastCursor: Long = System.currentTimeMillis(),
					 @Query("pageSize") pageSize: Int = 10): Call<NewsList>

	@GET("techNews")
	fun techNews(@Query("lastCursor") lastCursor: Long = System.currentTimeMillis(),
				 @Query("pageSize") pageSize: Int = 10): Call<NewsList>

	@GET("blockchain")
	fun blockchain(@Query("lastCursor") lastCursor: Long = System.currentTimeMillis(),
				   @Query("pageSize") pageSize: Int = 10): Call<NewsList>

	@GET("topic/{id}")
	fun topicDetail(@Path("id")id:String):Call<TopicDetail>
}