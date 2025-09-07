package app.iv.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable


import app.iv.utils.CopiableText
import app.iv.utils.MediaInfoFormatter
import app.iv.data.MediaStoreQuery
import app.iv.data.MediaItem
import app.iv.data.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaView(index: Int, folderId: Long, mediaType: MediaType, onBack: () -> Unit) {
	val context = LocalContext.current
	val coroutineScope = rememberCoroutineScope()
    var mediaItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isToolbarVisible by remember { mutableStateOf(true) }
    var isUserPlaying by remember { mutableStateOf(true) }
    var showInfoDialog by remember { mutableStateOf(false) } 
    var showDropdownMenu by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0L) }
    var seekToPosition by remember { mutableStateOf<Long?>(null) }
    var keepScreenOn by remember { mutableStateOf(false) }
    
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch {
                mediaItems = MediaStoreQuery(context).queryMediaItemsInFolder(folderId, mediaType)
            }
        }
    }

    fun requestDeletion(item: MediaItem) {
        coroutineScope.launch {
            try {
                val intentSender = MediaStoreQuery(context).deleteMediaItems(listOf(item.uri))
                val request = IntentSenderRequest.Builder(intentSender).build()
                deleteLauncher.launch(request)
            } catch (e: Exception) {
            
            }
        }
    }
    
    LaunchedEffect(folderId, mediaType) {
        mediaItems = MediaStoreQuery(context).queryMediaItemsInFolder(folderId, mediaType)
    }
    
    LaunchedEffect(keepScreenOn) {
        val window = (context as? android.app.Activity)?.window ?: return@LaunchedEffect
	    if (keepScreenOn) {
	        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
	    } else {
	        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
	    }
    }
    
    LaunchedEffect(isToolbarVisible) {
        val window = (context as? android.app.Activity)?.window ?: return@LaunchedEffect
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (isToolbarVisible) {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
    
    val state = rememberPagerState(
	    initialPage = index,
	    pageCount = { mediaItems.size }
	)
	val currentMediaItem = mediaItems.getOrNull(state.currentPage)
	
	Box(
	    modifier = Modifier
	    .background(Color.Black)
	    .fillMaxSize()
	) {
	    HorizontalPager(state = state) { page ->
	        val item = mediaItems[page]
	        val zoomState = rememberZoomState()
	        if(item.mimeType.startsWith("video/")) {
	            VideoPlayer(
	                uri = item.uri,
	                currentPage = state.currentPage == page,
	                isUserPlaying = isUserPlaying,
	                seekToPosition = seekToPosition,
	                onProgressChange = { position, totalDuration ->
                        currentPosition = position
                        duration = totalDuration
                    },
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                isToolbarVisible = !isToolbarVisible
                            }
                        )
                    }
	            )
	        } else {
	            AsyncImage(
	                model = item.uri,
	                contentDescription = null,
	                modifier = Modifier
	                	.fillMaxSize()
	                	.zoomable(
    	                	zoomState = zoomState,
    	                	onTap = {
    	                	    isToolbarVisible = !isToolbarVisible
    	                	}
	                	),
	                contentScale = ContentScale.Fit
	            )
	        }
	    }
	    
	    if(isToolbarVisible) {
	    	
	        TopAppBar(
    	        title = {},
    	        navigationIcon = {
    	        	IconButton(onClick = { onBack() }) {
    	        		Icon(Icons.Default.Close, contentDescription = null)
    	        	}
    	        },
    	        actions = {
    	        	IconButton(
        	        	onClick = {
        	        	    
        	        	}
    	        	) {
    	        		Icon(Icons.Default.Edit, contentDescription = null)
    	        	}
    	            FilledIconToggleButton(
    	                checked = keepScreenOn,
    	                onCheckedChange = { checked -> keepScreenOn = checked },
    	                colors = IconButtonDefaults.filledIconToggleButtonColors(
    	                	containerColor = Color.Transparent,
    	                	checkedContentColor = MaterialTheme.colorScheme.onPrimary
    	                )
    	            ) {
    	                Icon(Icons.Default.Coffee, contentDescription = null)
    	            }
    	            IconButton(onClick = { showInfoDialog = true }) {
    	            	Icon(Icons.Default.Info, contentDescription = null)
    	            }
    	            Box{
    	                IconButton(onClick =  { showDropdownMenu = true } ) {
    	                    Icon(Icons.Default.MoreVert, contentDescription = null)
    	                }
    	                if(showDropdownMenu) {
                	        DropdownMenu(
                	            expanded = showDropdownMenu,
                	            onDismissRequest = { showDropdownMenu = false },
                	            shape = MaterialTheme.shapes.large
                	        ) {
                	            DropdownMenuItem(
                	                text = { Text(text = "Delete", color = MaterialTheme.colorScheme.error) },
                	                onClick = {
                	                    currentMediaItem?.let { item ->
                                            requestDeletion(item)
                                        }
                	                },
                	                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                	            )
                	        }
                	    }
    	            }
    	        },
    	        modifier = Modifier.align(Alignment.TopCenter)
    	    )
    	    
    	    if(currentMediaItem?.mimeType?.startsWith("video/") == true) {
    	        IconButton(
            	    onClick = { isUserPlaying = !isUserPlaying},
            	    modifier = Modifier
            	        .align(Alignment.Center)
            	        .size(64.dp)
            	        .clip(CircleShape)
            	        .background(MaterialTheme.colorScheme.primary)
        	    ) {
        	        Icon(
            	        if (isUserPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
            	        contentDescription = null,
            	        tint = MaterialTheme.colorScheme.onPrimary,
            	        
        	        )
        	    }
        	    
        	    Column (
        	        modifier = Modifier
        	            .align(Alignment.BottomCenter)
        	            .padding(horizontal = 28.dp)
        	    ) {
        	        Slider(
            	        value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
            	        onValueChange = { 
                	        currentPosition = (it * duration ).toLong() 
                	        sliderPosition = it
            	        },
            	        onValueChangeFinished = { seekToPosition = (sliderPosition * duration).toLong() },
            	        track = { sliderState ->
            	            SliderDefaults.Track(
            	                sliderState = sliderState,
            	                modifier = Modifier.height(2.dp)
            	            )
            	        }
            	    )
            	    Text(
            	        text = MediaInfoFormatter.formatDuration(currentPosition),
            	        fontSize = 12.sp,
            	        fontWeight = FontWeight.Bold
            	    )
        	    }

    	    }

	    }
	    if(showInfoDialog) {
	        AlertDialog(
	            onDismissRequest = { showInfoDialog = false},
	            title = { Text("File Details") },
	            text = {
	                Column(
	                    modifier = Modifier.verticalScroll(rememberScrollState())
	                ) {
	                    currentMediaItem?.let {
	                        CopiableText(label = "Filename/type: ", value = "${it.name}")
	                        CopiableText(label = "Resolution: ", value = "${it.width}x${it.height}")
	                        CopiableText(label = "File size: ", value = "${MediaInfoFormatter.formatSize(it.size)}")
	                        CopiableText(label = "Date added: ", value = "${MediaInfoFormatter.formatDate(it.dateAdded)}")
	                        if(it.mimeType.startsWith("video/")) {
	                            CopiableText(label = "Length: ", value = "${MediaInfoFormatter.formatDuration(it.duration)}")
	                        }
	                    }
	                }
	            },
	            confirmButton = {
	                TextButton(onClick = { showInfoDialog = false }) {
	                    Text("OK")
	                }
	            }
	        )
	    }
	    
	}
}
