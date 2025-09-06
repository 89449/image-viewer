package app.iv.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.IntentSender

data class Folder(
    val id: Long,
    val name: String,
    val thumbnailUri: Uri,
    val count: Int
)

data class MediaItem(
    val id: Long,
    val size: Long,
    val dateAdded: Long,
    val duration: Long,
    val name: String,
    val mimeType: String,
    val uri: Uri,
    val width: Int,
    val height: Int
)

class MediaStoreQuery(private val context: Context) {
	
    suspend fun queryMediaFolders(): List<Folder> = withContext(Dispatchers.IO) {
        val folders = mutableMapOf<Long, Folder>()
        val contentUri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )
        
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
        
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        context.contentResolver.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val bucketId = cursor.getLong(bucketIdColumn)
                val bucketName = cursor.getString(bucketNameColumn) ?: "Unknown"
                val mediaId = cursor.getLong(idColumn)
                val mediaType = cursor.getInt(mediaTypeColumn)

                val itemUri = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId)
                } else {
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaId)
                }
                
                folders.compute(bucketId) { _, existingFolder ->
                    existingFolder?.copy(count = existingFolder.count + 1)
                        ?: Folder(bucketId, bucketName, itemUri, 1)
                }
            }
        }
        return@withContext folders.values.toList()
    }
    
    suspend fun queryMediaItems(bucketId: Long? = null): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaItems = mutableListOf<MediaItem>()
        val contentUri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Video.Media.DURATION
        )

        val selectionClauses = mutableListOf("${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)")
        val selectionArgsList = mutableListOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        bucketId?.let {
            selectionClauses.add("${MediaStore.Files.FileColumns.BUCKET_ID} = ?")
            selectionArgsList.add(it.toString())
        }

        val selection = selectionClauses.joinToString(" AND ")
        val selectionArgs = selectionArgsList.toTypedArray()
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        context.contentResolver.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
            val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val mediaType = cursor.getInt(mediaTypeColumn)

                val uri = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                } else {
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                }
                
                val duration = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    cursor.getLong(durationColumn)
                } else {
                    0L
                }

                mediaItems.add(
                    MediaItem(
                        id = id,
                        size = cursor.getLong(sizeColumn),
                        dateAdded = cursor.getLong(dateAddedColumn),
                        duration = duration,
                        name = cursor.getString(nameColumn),
                        mimeType = cursor.getString(mimeTypeColumn),
                        uri = uri,
                        width = cursor.getInt(widthColumn),
                        height = cursor.getInt(heightColumn)
                    )
                )
            }
        }
        return@withContext mediaItems
    }

    suspend fun queryAllMediaItems(): List<MediaItem> = queryMediaItems(null)

    suspend fun queryMediaItemsInFolder(folderId: Long): List<MediaItem> = queryMediaItems(folderId)
    
    fun deleteMediaItems(uris: List<Uri>): IntentSender {
        return MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
    }
}