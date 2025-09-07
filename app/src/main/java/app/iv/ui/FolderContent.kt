package app.iv.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFramePercent

import app.iv.utils.MediaInfoFormatter
import app.iv.data.MediaStoreQuery
import app.iv.data.MediaItem
import app.iv.data.MediaType

@OptIn(ExperimentalMaterial3Api::class,ExperimentalFoundationApi::class)
@Composable
fun FolderContent(folderId: Long, folderName: String, toMediaView: (Int, MediaType) -> Unit, mediaType: MediaType, onBack: () -> Unit) {
    val context = LocalContext.current
    var mediaItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
    val coroutineScope = rememberCoroutineScope()
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isSelectionMode = false
            selectedItemIds = emptySet()
            coroutineScope.launch {
                mediaItems = MediaStoreQuery(context).queryMediaItemsInFolder(folderId, mediaType)
            }
        }
    }
    
    BackHandler(enabled = isSelectionMode) {
        isSelectionMode = false
        selectedItemIds = emptySet()
    }
    
    LaunchedEffect(folderId, mediaType) {
        isLoading = true
        mediaItems = MediaStoreQuery(context).queryMediaItemsInFolder(folderId, mediaType)
        isLoading = false
    }
    
    LaunchedEffect(selectedItemIds) {
        if (selectedItemIds.isEmpty() && isSelectionMode) {
            isSelectionMode = false
        }
    }

    Column {
    	TopAppBar(
    		title = {
    		    if(isSelectionMode) {
    		        Text("${selectedItemIds.size}/${mediaItems.size} selected")
    		    } else {
    		        Text(folderName)
    		    }
    		},
    		navigationIcon = {
    			IconButton(onClick = { onBack() }) {
    				Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
    			}
    		},
    		actions = {
    		    if(isSelectionMode) {
    		    	val allSelected = selectedItemIds.size == mediaItems.size
				    FilledIconToggleButton(
				        checked = allSelected,
				        onCheckedChange = { isChecked ->
				            selectedItemIds = if (isChecked) {
				                mediaItems.map { it.id }.toSet()
				            } else {
				                emptySet()
				            }
				        }
				    ) {
				        Icon(
				            imageVector = Icons.Default.SelectAll,
				            contentDescription = "Toggle select all"
				        )
				    }
    		        IconButton(onClick = {
    		            val selectedUris = mediaItems.filter { it.id in selectedItemIds }.map { it.uri }
                        if (selectedUris.isNotEmpty()) {
                            val intentSender = MediaStoreQuery(context).deleteMediaItems(selectedUris)
                            deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        }
    		        }) {
    		            Icon(
    		                Icons.Default.Delete,
    		                contentDescription = null,
    		                tint = MaterialTheme.colorScheme.error
    		            )
    		        }
    		    }
    		}
    	)
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp)
            ) {
                itemsIndexed(mediaItems) {index, item ->
                val isSelected = selectedItemIds.contains(item.id)
                
                    Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(1.dp)
                        .combinedClickable(
                            onLongClick = {
                                isSelectionMode = true
                                selectedItemIds += item.id
                            },
                            onClick = {
                                if(isSelectionMode) {
                                    if(isSelected) {
                                        selectedItemIds -= item.id
                                    } else {
                                        selectedItemIds += item.id
                                    }
                                } else {
                                    toMediaView(index, mediaType)
                                }
                            }
                        )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                            	.data(item.uri)
                            	.videoFramePercent(0.5)
                            	.build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        if(item.mimeType.startsWith("video/")) {
                            Box(
	                            modifier = Modifier.fillMaxSize(),
	                            contentAlignment = Alignment.BottomEnd
                            ) {
                            	Text(
                            	    text = MediaInfoFormatter.formatDuration(item.duration),
                            	    fontSize = 12.sp,
                            	    fontWeight = FontWeight.Bold,
                            	    modifier = Modifier.padding(end = 8.dp)
                            	)
                            }
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
