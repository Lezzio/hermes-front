package fr.insalyon.hermes

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import fr.insalyon.hermes.model.AccessChat
import fr.insalyon.hermes.model.LogChat
import fr.insalyon.hermes.model.TextMessage

data class AppState(
    val messages: SnapshotStateList<TextMessage> = mutableStateListOf(),
    val chats: SnapshotStateList<LogChat> = mutableStateListOf(),
    val usersConnected: MutableState<Map<String, Boolean>> = mutableStateOf(mapOf()),
    val currentChat: MutableState<AccessChat?> = mutableStateOf(null)
)