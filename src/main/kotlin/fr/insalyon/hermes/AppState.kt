package fr.insalyon.hermes

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import fr.insalyon.hermes.client.HermesClient
import fr.insalyon.hermes.model.AccessChat
import fr.insalyon.hermes.model.LogChat
import fr.insalyon.hermes.model.Message
import fr.insalyon.hermes.model.TextMessage

data class AppState(
    val username: MutableState<String> = mutableStateOf("aguigal"),
    val hermesClient: MutableState<HermesClient?> = mutableStateOf(null),
    val chats: SnapshotStateList<LogChat> = mutableStateListOf(),
    val currentChat: MutableState<AccessChat?> = mutableStateOf(null),
    val messages: SnapshotStateList<TextMessage> = mutableStateListOf(),
    val usersConnected: MutableState<Map<String, Boolean>> = mutableStateOf(mapOf()),
    val usersAddable: SnapshotStateList<String> = mutableStateListOf()
)