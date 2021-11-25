package fr.insalyon.hermes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.insalyon.hermes.components.ConversationUserRow

@Composable
fun chatManagePanel(appState: AppState, askAddMember: MutableState<Boolean>, askChatUpdate: MutableState<Boolean>) {
    if (appState.currentChat.value != null) {
        Column(
            Modifier.width(250.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .background(Color(245, 245, 245))
        ) {
            appState.usersConnected.value.entries.forEach {
                ConversationUserRow(
                    appState = appState,
                    username = it.key,
                    connected = it.value,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            val admin = appState.currentChat.value?.admin

            if ((admin.equals(appState.username.value) || admin.equals("all"))) {
                OutlinedButton(
                    onClick = {
                        appState.hermesClient.value?.getAddable()
                        askAddMember.value = true
                    },
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Add users", color = Color.Blue)
                }
                OutlinedButton(
                    onClick = {
                        println("Clicking on update chat")
                        appState.hermesClient.value?.getAddable()
                        askChatUpdate.value = true
                    },
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Update chat", color = Color.Blue)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    appState.hermesClient.value?.leaveChat(appState.currentChat.value?.chatName)
                },
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Leave chat", color = Color.Blue)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}