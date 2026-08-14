package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HighDensityBadge
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.DocumentWorkspaceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.LSDocsTheme
import com.example.ui.theme.PurpleDark
import com.example.ui.theme.PurpleLightBg
import com.example.ui.viewmodel.MainViewModel

import androidx.compose.ui.text.style.TextOverflow

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            LSDocsTheme(darkTheme = settings.isDarkTheme) {
                MainAppLayout(viewModel)
            }
        }
    }
}

enum class NavItem(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Dashboard),
    BROWSE("Browse", Icons.Default.Folder),
    WORKSPACE("Viewer", Icons.Default.Description),
    TOOLS("Tools", Icons.Default.Build),
    LIBRARY("Library", Icons.Default.CollectionsBookmark),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainAppLayout(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    var isUnlocked by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(NavItem.HOME) }
    var selectedToolSubtab by remember { mutableStateOf("ocr") }

    if (settings.appLockEnabled && !isUnlocked) {
        AppLockScreen(
            expectedPin = settings.pinHash,
            biometricsEnabled = settings.biometricsEnabled,
            onUnlocked = { isUnlocked = true }
        )
    } else {
        Scaffold(
        topBar = {
            HighDensityTopHeader(
                title = when (selectedTab) {
                    NavItem.HOME -> "LS Docs"
                    NavItem.BROWSE -> "File Browser"
                    NavItem.WORKSPACE -> "Workspace"
                    NavItem.TOOLS -> "Document Tools"
                    NavItem.LIBRARY -> "Library & Vault"
                    NavItem.SETTINGS -> "Settings"
                }
            )
        },
        bottomBar = {
            HighDensityBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                NavItem.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToWorkspace = { selectedTab = NavItem.WORKSPACE },
                    onNavigateToTools = { tool ->
                        selectedToolSubtab = tool
                        selectedTab = NavItem.TOOLS
                    }
                )
                NavItem.BROWSE -> BrowseScreen(
                    viewModel = viewModel,
                    onNavigateToWorkspace = { selectedTab = NavItem.WORKSPACE }
                )
                NavItem.WORKSPACE -> DocumentWorkspaceScreen(
                    viewModel = viewModel
                )
                NavItem.TOOLS -> ToolsScreen(
                    viewModel = viewModel,
                    initialTab = selectedToolSubtab
                )
                NavItem.LIBRARY -> LibraryScreen(
                    viewModel = viewModel,
                    onNavigateToWorkspace = { selectedTab = NavItem.WORKSPACE }
                )
                NavItem.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
}

@Composable
fun HighDensityTopHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PurpleLightBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Offline Vault",
                        tint = PurpleDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Offline • On-Device Inspection",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                HighDensityBadge(
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "100% PRIVATE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HighDensityBottomNavigation(
    selectedTab: NavItem,
    onTabSelected: (NavItem) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavItem.values().forEach { item ->
            val isSelected = selectedTab == item
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item) },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
