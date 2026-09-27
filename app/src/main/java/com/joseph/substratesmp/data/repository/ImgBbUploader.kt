package com.joseph.substratesmp.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object ImgBbUploader {
  // Get your free API key at: https://api.imgbb.com/ (Takes 30 seconds, no credit card)
  // Replace this with your actual key:
  const val IMGBB_API_KEY = "YOUR_IMGBB_API_KEY_HERE"

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  /**
   * Uploads an uncompressed, high-res image directly to ImgBB 
   * and returns the public direct HTTPS link.
   */
  suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
    try {
      val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
        ?: return@withContext Result.failure(Exception("Cannot open image stream"))

      val buffer = ByteArrayOutputStream()
      inputStream.use { input ->
        val temp = ByteArray(8192)
        var read: Int
        while (input.read(temp).also { read = it } != -1) {
          buffer.write(temp, 0, read)
        }
      }
      val imageBytes = buffer.toByteArray()

      val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
          "image",
          "upload_${System.currentTimeMillis()}.jpg",
          imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
        )
        .build()

      val request = Request.Builder()
        .url("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        return@withContext Result.failure(Exception("ImgBB upload failed: ${response.code}"))
      }

      val json = JSONObject(responseString)
      if (json.getBoolean("success")) {
        val data = json.getJSONObject("data")
        // Get the direct full-resolution image URL
        val directUrl = data.getString("url")
        Result.success(directUrl)
      } else {
        Result.failure(Exception("ImgBB response error: $responseString"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Uploads byte array directly (e.g. from camera or stickers)
   */
  suspend fun uploadBytes(imageBytes: ByteArray): Result<String> = withContext(Dispatchers.IO) {
    try {
      val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
          "image",
          "upload_${System.currentTimeMillis()}.png",
          imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
        )
        .build()

      val request = Request.Builder()
        .url("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string() ?: ""

      val json = JSONObject(responseString)
      if (json.getBoolean("success")) {
        val directUrl = json.getJSONObject("data").getString("url")
        Result.success(directUrl)
      } else {
        Result.failure(Exception("ImgBB error: $responseString"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
