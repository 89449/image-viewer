package app.iv.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFramePercent

import app.iv.data.MediaStoreQuery
import app.iv.data.Folder
import app.iv.data.MediaType
import app.iv.utils.SharedPreferencesUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderList(toFolderContent: (Long, String, MediaType) -> Unit) {
    val context = LocalContext.current
    var folders by remember { mutableStateOf(emptyList<Folder>()) }

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Images & videos") }
    var filterType by remember { mutableStateOf(MediaType.ALL) } 

    // LaunchedEffect to load the saved filter on initial composition
    LaunchedEffect(Unit) {
        filterType = SharedPreferencesUtil.loadFilter(context)
    }

    // LaunchedEffect to re-query folders when filterType changes
    LaunchedEffect(filterType) {
        folders = MediaStoreQuery(context).queryMediaFolders(filterType)
        selectedFilter = when (filterType) {
            MediaType.ALL -> "Images & videos"
            MediaType.IMAGE -> "Images"
            MediaType.VIDEO -> "Videos"
        }
    }
    
    val checkIcon = @Composable {
	    Icon(
	        imageVector = Icons.Default.Check,
	        contentDescription = null
	    )
	}

    Column {
        TopAppBar(
            title = {
                Box(modifier = Modifier.clickable { expanded = true }) {
                    Text(selectedFilter)
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.padding(end = 24.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Default") },
                            onClick = {
                                SharedPreferencesUtil.saveFilter(context, MediaType.ALL)
                                filterType = MediaType.ALL
                                expanded = false
                            },
                            leadingIcon = {
                            	if(filterType == MediaType.ALL) checkIcon()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Images") },
                            onClick = {
                                SharedPreferencesUtil.saveFilter(context, MediaType.IMAGE)
                                filterType = MediaType.IMAGE
                                expanded = false
                            },
                            leadingIcon = {
                            	if(filterType == MediaType.IMAGE) checkIcon()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Videos") },
                            onClick = {
                                SharedPreferencesUtil.saveFilter(context, MediaType.VIDEO)
                                filterType = MediaType.VIDEO
                                expanded = false
                            },
                            leadingIcon = {
                            	if(filterType == MediaType.VIDEO) checkIcon()
                            }
                        )
                    }
                }
            },
            actions = {}
        )
        LazyColumn() {
            items(folders) { folder ->
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable { toFolderContent(folder.id, folder.name, filterType) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(folder.thumbnailUri)
                            .videoFramePercent(0.5)
                            .build(),
                        modifier = Modifier.size(width = 82.dp, height = 50.dp),
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
