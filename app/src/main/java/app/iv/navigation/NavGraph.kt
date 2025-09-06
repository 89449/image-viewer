package app.iv.navigation

import androidx.compose.runtime.Composable
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import app.iv.ui.FolderList
import app.iv.ui.FolderContent
import app.iv.ui.MediaView
import app.iv.data.MediaType

@Composable
fun NavGraph() {
	val navController = rememberNavController()
	NavHost(
	    navController = navController,
	    startDestination = "folder_list",
	    enterTransition = { EnterTransition.None },
		exitTransition = { ExitTransition.None },
	) {
		composable(
		    route = "folder_list"
		) {
			FolderList(
			    toFolderContent = { folderId, folderName, mediaType ->
			    	navController.navigate("folder_content/$folderId/$folderName/$mediaType")
			    }
			)
		}
		composable(
		    route = "folder_content/{folderId}/{folderName}/{mediaType}",
		    arguments = listOf(
		        navArgument("folderId") { type = NavType.LongType },
		        navArgument("folderName") { type = NavType.StringType },
		        navArgument("mediaType") { type = NavType.StringType }
		    )
		) { backStackEntry ->
			val folderId = backStackEntry.arguments!!.getLong("folderId")
			val folderName = backStackEntry.arguments!!.getString("folderName")!!
			val mediaTypeString = backStackEntry.arguments!!.getString("mediaType")!!
			val mediaType = MediaType.valueOf(mediaTypeString)
			
			FolderContent(
			    folderId = folderId,
			    folderName = folderName,
			    toMediaView = { index ->
			    	navController.navigate("media_view/$index/$folderId")
			    },
			    mediaType = mediaType
			)
		}
		composable(
		    route = "media_view/{index}/{folderId}",
		    arguments = listOf(
		        navArgument("index") { type = NavType.IntType },
		        navArgument("folderId") { type = NavType.LongType }
		    )
		) { backStackEntry ->
			val index = backStackEntry.arguments!!.getInt("index")
			val folderId = backStackEntry.arguments!!.getLong("folderId")
			MediaView(
			    index = index,
			    folderId = folderId
			)
		}
	}
}
