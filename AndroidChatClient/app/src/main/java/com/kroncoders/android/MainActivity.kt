package com.kroncoders.android

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.navigation.navOptions
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.kroncoders.android.networking.call.CallService
import com.kroncoders.android.ui.ChatNavHost
import com.kroncoders.android.ui.navigation.DirectionBack
import com.kroncoders.android.ui.navigation.NavigationManager
import com.kroncoders.android.ui.navigation.navigate
import com.kroncoders.android.ui.theme.AndroidChatClientTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationManager: NavigationManager

    //we need to inject it here to force it to be created
    @Inject
    lateinit var callService: CallService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)

        setContent {
            AndroidChatClientTheme {
                // A surface container using the 'background' color from the theme
                val navController = rememberAnimatedNavController()

                LaunchedEffect(true) {
                    navigationManager.commands.collect { command ->
                        when (command) {
                            DirectionBack -> navController.popBackStack()
                            else -> navController.navigate(
                                route = command::class.toString(),
                                args = command.arguments,
                                navOptions = navOptions(command.navBuilder)
                            )
                        }
                    }
                }

                ChatNavHost(navController = navController)
            }
        }
    }
}