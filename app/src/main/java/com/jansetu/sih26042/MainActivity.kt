package com.jansetu.sih26042

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jansetu.sih26042.ui.screens.HomeScreen
import com.jansetu.sih26042.ui.screens.MaterialsScreen
import com.jansetu.sih26042.ui.screens.OfflineScreen
import com.jansetu.sih26042.ui.screens.TextTranslationScreen
import com.jansetu.sih26042.ui.screens.VoiceTranslationScreen
import com.jansetu.sih26042.ui.theme.JanSetuTheme

private object Routes {
    const val HOME = "home"
    const val TEXT = "text"
    const val VOICE = "voice"
    const val MATERIALS = "materials"
    const val OFFLINE = "offline"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JanSetuTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = Routes.HOME) {
                        composable(Routes.HOME) {
                            HomeScreen(
                                onText = { nav.navigate(Routes.TEXT) },
                                onVoice = { nav.navigate(Routes.VOICE) },
                                onMaterials = { nav.navigate(Routes.MATERIALS) },
                                onOffline = { nav.navigate(Routes.OFFLINE) }
                            )
                        }
                        composable(Routes.TEXT) { TextTranslationScreen(onBack = { nav.popBackStack() }) }
                        composable(Routes.VOICE) { VoiceTranslationScreen(onBack = { nav.popBackStack() }) }
                        composable(Routes.MATERIALS) { MaterialsScreen(onBack = { nav.popBackStack() }) }
                        composable(Routes.OFFLINE) { OfflineScreen(onBack = { nav.popBackStack() }) }
                    }
                }
            }
        }
    }
}
