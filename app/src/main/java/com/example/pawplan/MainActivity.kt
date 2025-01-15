package com.example.pawplan

import android.os.Bundle
import android.view.Window
import androidx.activity.SystemBarStyle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.example.pawplan.models.MainViewModel
import com.example.pawplan.ui.theme.LatoFontFamily
import com.example.pawplan.ui.theme.PawPlanTheme

val CustomTypography = Typography(
    bodyLarge = Typography().bodyLarge.copy(fontFamily = LatoFontFamily),
    titleLarge = Typography().titleLarge.copy(fontFamily = LatoFontFamily),
    labelSmall = Typography().labelSmall.copy(fontFamily = LatoFontFamily),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = LatoFontFamily),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = LatoFontFamily),
    titleMedium = Typography().titleMedium.copy(fontFamily = LatoFontFamily),
    bodySmall = Typography().bodySmall.copy(fontFamily = LatoFontFamily),
)

class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fetch user and pet details
        mainViewModel.fetchUserAndPetDetails()

        setContent {
            PawPlanTheme (
                typography = CustomTypography
            ) {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BarsWithScaffold()
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            MaterialTheme.colorScheme.primary.toArgb(),
                            MaterialTheme.colorScheme.primary.toArgb()
                        )
                    )
                }
            }
        }
    }
}