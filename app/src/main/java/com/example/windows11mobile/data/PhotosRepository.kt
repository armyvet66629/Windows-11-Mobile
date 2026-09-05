package com.example.windows11mobile.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotosRepository(private val context: Context) {

    suspend fun getRecentPhotos(limit: Int = 40): List<Uri> = withContext(Dispatchers.IO) {
        android.util.Log.d("PhotosRepository", "Searching for photos in People/Pets albums...")
        val photos = mutableListOf<Uri>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC"

        // Inclusive search for likely personal photo albums
        val selection = "LOWER(${MediaStore.Images.Media.BUCKET_DISPLAY_NAME}) LIKE ? OR " +
                        "LOWER(${MediaStore.Images.Media.BUCKET_DISPLAY_NAME}) LIKE ? OR " +
                        "LOWER(${MediaStore.Images.Media.BUCKET_DISPLAY_NAME}) LIKE ? OR " +
                        "LOWER(${MediaStore.Images.Media.BUCKET_DISPLAY_NAME}) LIKE ? OR " +
                        "LOWER(${MediaStore.Images.Media.BUCKET_DISPLAY_NAME}) LIKE ?"
        val selectionArgs = arrayOf("%people%", "%pets%", "%family%", "%camera%", "%dcim%")

        try {
            context.contentResolver.query(
                contentUri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                android.util.Log.d("PhotosRepository", "Query found ${cursor.count} matching items")
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                
                while (cursor.moveToNext() && photos.size < limit) {
                    val id = cursor.getLong(idColumn)
                    photos.add(ContentUris.withAppendedId(contentUri, id))
                }
            }

            // Fallback: If filtered search found nothing, get any 20 recent photos
            if (photos.isEmpty()) {
                android.util.Log.d("PhotosRepository", "No specific albums found, falling back to all images")
                context.contentResolver.query(
                    contentUri,
                    projection,
                    null,
                    null,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    while (cursor.moveToNext() && photos.size < limit) {
                        val id = cursor.getLong(idColumn)
                        photos.add(ContentUris.withAppendedId(contentUri, id))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PhotosRepository", "Query Error", e)
        }
        
        android.util.Log.d("PhotosRepository", "Search finished. Found ${photos.size} photos.")
        photos
    }
}
