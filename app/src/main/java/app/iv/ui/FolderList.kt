package app.iv.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFramePercent

import app.iv.data.MediaStoreQuery
import app.iv.data.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderList(toFolderContent: (Long, String) -> Unit) {
    val context = LocalContext.current
    var folders by remember { mutableStateOf(emptyList<Folder>()) }

    LaunchedEffect(Unit) {
        folders = MediaStoreQuery(context).queryMediaFolders()
    }

    Column {
        TopAppBar(
            title = { Text("Folders") },
            actions = {
                
            }
        )
        LazyColumn() {
            items(folders) { folder ->
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth().clickable { toFolderContent(folder.id, folder.name) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                        	.data(folder.thumbnailUri)
                        	.videoFramePercent(0.5)
                        	.build(),
                        modifier = Modifier.size(64.dp),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                    	Text(
                    		text = folder.name,
                    		style = MaterialTheme.typography.titleMedium
                    	)
                    	Text(
                    		text = folder.count.toString(),
                    		style = MaterialTheme.typography.bodyMedium
                    	)
                    }
                }
            }
        }
    }
}