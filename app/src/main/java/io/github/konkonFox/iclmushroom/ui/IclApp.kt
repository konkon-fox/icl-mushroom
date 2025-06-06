package io.github.konkonFox.iclmushroom.ui

import android.net.Uri
import android.util.Log
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.konkonFox.iclmushroom.DialogOptions
import io.github.konkonFox.iclmushroom.IclViewModel
import io.github.konkonFox.iclmushroom.R
import io.github.konkonFox.iclmushroom.data.ImgurAccountData
import io.github.konkonFox.iclmushroom.ui.screen.HistoriesScreen
import io.github.konkonFox.iclmushroom.ui.screen.HomeScreen
import io.github.konkonFox.iclmushroom.ui.screen.SettingsScreen
import io.github.konkonFox.iclmushroom.ui.screen.UploadScreen
import io.github.konkonFox.iclmushroom.ui.theme.ICLMushroomTheme


enum class IclScreen {
    Home,
    Upload,
    Histories,
    Settings
}

@Composable
fun IclApp(
    isMushroom: Boolean = false,
    isShared: Boolean = false,
    isImgurCallback: Boolean = false,
    imgurAccountData: ImgurAccountData = ImgurAccountData(),
    sharedUris: List<Uri> = emptyList(),
    iclViewModel: IclViewModel = viewModel(
        factory = IclViewModel.Factory
    ),
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        iclViewModel.setIsMushroom(isMushroom)
        iclViewModel.setIsShared(isShared)
        //
        if (isShared && sharedUris.isNotEmpty()) {
            iclViewModel.onImagesSelected(
                uris = sharedUris,
                context = context,
                navController = navController
            )
            return@LaunchedEffect
        }
        if (isImgurCallback && imgurAccountData.accessToken !== null) {
            Log.d("IclApp", imgurAccountData.toString())
            iclViewModel.updateImgurAccountData(imgurAccountData)
            iclViewModel.openDialog(
                DialogOptions(
                    isOpen = true,
                    title = R.string.dialog_title_imgur_login,
                    body = R.string.dialog_body_imgur_login,
                    dynamicBody = null
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = IclScreen.Home.name,
        ) {
            composable(
                route = IclScreen.Home.name,
                enterTransition = { slideInHorizontally { -it } },
                exitTransition = { slideOutHorizontally { -it } }
            ) {
                HomeScreen(
                    uiState = iclViewModel.uiState.collectAsState().value,
                    viewModel = iclViewModel,
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(
                route = IclScreen.Upload.name,
                exitTransition = { slideOutHorizontally { it } }
            ) {
                UploadScreen(
                    uiState = iclViewModel.uiState.collectAsState().value,
                    viewModel = iclViewModel,
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(
                route = IclScreen.Histories.name,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } }
            ) {
                HistoriesScreen(
                    uiState = iclViewModel.uiState.collectAsState().value,
                    viewModel = iclViewModel,
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(
                route = IclScreen.Settings.name,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } }
            ) {
                SettingsScreen(
                    uiState = iclViewModel.uiState.collectAsState().value,
                    viewModel = iclViewModel,
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun IclPreview() {
    ICLMushroomTheme {
        IclApp()
    }
}