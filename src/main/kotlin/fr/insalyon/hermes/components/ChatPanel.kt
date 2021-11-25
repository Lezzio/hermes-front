package fr.insalyon.hermes.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.insalyon.hermes.AppState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun chatPanel(appState: AppState, askChatName: MutableState<Boolean>) {
    Column(
        Modifier.width(250.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .background(Color(245, 245, 245))
    ) {

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                askChatName.value = true
            },
            border = BorderStroke(1.dp, Color.Black),
            modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Create a conversation", color = Color.Blue)
        }
        Spacer(modifier = Modifier.height(10.dp))

        val chats = appState.chats.toMutableList()
        chats.sortByDescending { it.message.time }
        chats.forEach {
            ConversationRow(
                activeChat = appState.currentChat.value?.chatName == it.name,
                logChat = it,
                modifier = Modifier.align(Alignment.Start)
                    .clickable {
                        println("Access chat")
                        appState.hermesClient.value?.accessChat(it.name)
                    }
            )
        }
    }
}