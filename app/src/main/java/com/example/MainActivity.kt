package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.LoveRepository
import com.example.ui.screens.HomeTab
import com.example.ui.screens.LettersTab
import com.example.ui.screens.MusicTab
import com.example.ui.screens.StoryTab
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.glassmorphicBorder
import com.example.ui.viewmodel.LoveViewModel
import com.example.ui.viewmodel.LoveViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Crash reporting handler
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val prefs = getSharedPreferences("crash_reports", MODE_PRIVATE)
                val sw = java.io.StringWriter()
                val pw = java.io.PrintWriter(sw)
                throwable.printStackTrace(pw)
                prefs.edit().putString("last_crash", sw.toString()).commit()
            } catch (e: Exception) {
                // Ignore
            }
            if (oldHandler != null) {
                oldHandler.uncaughtException(thread, throwable)
            } else {
                System.exit(1)
            }
        }

        super.onCreate(savedInstanceState)
        
        // 1. Initialize DB + Repository
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = LoveRepository(database.loveDao())

        // 2. Instantiate ViewModel
        val viewModel: LoveViewModel by viewModels {
            LoveViewModelFactory(repository)
        }

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableIntStateOf(0) }

                // Check for last crash
                val context = LocalContext.current
                val prefs = remember { context.getSharedPreferences("crash_reports", android.content.Context.MODE_PRIVATE) }
                var lastCrash by remember { mutableStateOf<String?>(prefs.getString("last_crash", null)) }

                if (lastCrash != null) {
                    AlertDialog(
                        onDismissRequest = {
                            prefs.edit().remove("last_crash").apply()
                            lastCrash = null
                        },
                        title = { Text("⚠️ Diagnóstico de Caída") },
                        text = {
                            Box(modifier = Modifier.sizeIn(maxHeight = 300.dp)) {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    Text("La aplicación se cerró por un error inesperado:")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = lastCrash ?: "",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(8.dp),
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    prefs.edit().remove("last_crash").apply()
                                    lastCrash = null
                                }
                            ) {
                                Text("Aceptar y Limpiar")
                            }
                        }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent, // Let the custom beautiful glows background show through
                    bottomBar = {
                        val navItems = listOf(
                            NavigationItem("Inicio", Icons.Filled.Home, Icons.Outlined.Home, "home_tab"),
                            NavigationItem("Cartas", Icons.Filled.Mail, Icons.Outlined.Mail, "letters_tab"),
                            NavigationItem("Historia", Icons.Filled.Timeline, Icons.Outlined.Timeline, "story_tab"),
                            NavigationItem("Música", Icons.Filled.MusicNote, Icons.Outlined.MusicNote, "music_tab")
                        )

                        NavigationBar(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                                .glassmorphicBorder(RoundedCornerShape(32.dp))
                                .clip(RoundedCornerShape(32.dp))
                                .testTag("app_navigation_bar"),
                            containerColor = Color(0x991C1B1F), // bg-[#1C1B1F]/60 (60% opacity dark glass)
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)
                    
                    when (selectedTab) {
                        0 -> HomeTab(viewModel = viewModel, modifier = screenModifier)
                        1 -> LettersTab(viewModel = viewModel, modifier = screenModifier)
                        2 -> StoryTab(viewModel = viewModel, modifier = screenModifier)
                        3 -> MusicTab(viewModel = viewModel, modifier = screenModifier)
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)
