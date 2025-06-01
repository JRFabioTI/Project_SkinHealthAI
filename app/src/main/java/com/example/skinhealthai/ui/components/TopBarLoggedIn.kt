package com.example.skinhealthai.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarLoggedIn(userName: String, onLogout: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text("SkinHealthAI", fontWeight = FontWeight.Bold)
        },
        actions = {
            IconButton(onClick = {  }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificações")
            }
            Box {
                TextButton(onClick = { showMenu = !showMenu }) {
                    Text(
                        text = "Olá, $userName",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Icon(Icons.Filled.MoreVert, contentDescription = "Menu de opções", tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            showMenu = false
                            onLogout()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}