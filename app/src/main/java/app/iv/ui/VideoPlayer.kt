package app.iv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.*

import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.CaptionStyleCompat
import android.graphics.Color
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

@Composable
fun VideoPlayer(
    uri: Uri,
    currentPage: Boolean,
    isUserPlaying: Boolean,
    seekToPosition: Long?,
    onProgressChange: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var wasPlaying by remember { mutableStateOf(isUserPlaying) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    wasPlaying = exoPlayer.isPlaying
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isUserPlaying && wasPlaying) {
                        exoPlayer.play()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            exoPlayer.release()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uri) {
        val item = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(item)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.prepare()
    }

    LaunchedEffect(exoPlayer) {
        while(true) {
            if (exoPlayer.isPlaying) {
                onProgressChange(exoPlayer.currentPosition, exoPlayer.duration)
            }
            delay(1000)
        }
    }

    LaunchedEffect(seekToPosition) {
        seekToPosition?.let {
            exoPlayer.seekTo(it)
        }
    }

    LaunchedEffect(currentPage, isUserPlaying) {
        if (currentPage && isUserPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }
    
    LaunchedEffect(currentPage) {
        if(currentPage) {
            exoPlayer.seekTo(0)
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                player = exoPlayer
                
                val subtitleView = this.subtitleView
	            subtitleView?.apply {
	                val style = CaptionStyleCompat(
	                    Color.WHITE, // Text color
	                    Color.TRANSPARENT, // Text background color
	                    Color.TRANSPARENT, // Window color (the box behind the text block)
	                    CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, // Add a shadow for legibility
	                    Color.BLACK, // Edge color
	                    null // Default typeface
	                )
	                setStyle(style)
	                setFractionalTextSize(0.05f) // Set the text size relative to the screen height
	            }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
