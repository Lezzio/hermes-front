package fr.insalyon.hermes

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import fr.insalyon.hermes.model.AccessChat
import fr.insalyon.hermes.model.LogChat
import fr.insalyon.hermes.model.Message

class AppState {
    val messages: SnapshotStateList<Message> = mutableStateListOf()
    val chats: SnapshotStateList<LogChat> = mutableStateListOf()
    val currentChat: MutableState<AccessChat?> = mutableStateOf(null)
}