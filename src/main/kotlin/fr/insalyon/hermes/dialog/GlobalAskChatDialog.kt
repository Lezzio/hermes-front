package fr.insalyon.hermes.dialog

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import fr.insalyon.hermes.AppState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun globalAskChatDialog(appState: AppState, askChatName: MutableState<Boolean>) {

    var chatNameInput by rememberSaveable { mutableStateOf("") }

    if (askChatName.value) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(text = "New conversation")
            },
            text = {
                TextField(
                    value = chatNameInput,
                    onValueChange = {
                        chatNameInput = it
                    },
                    label = { Text("Conversation's name") },
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        appState.hermesClient.value?.createChat(chatNameInput)
                        chatNameInput = ""
                        askChatName.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        chatNameInput = ""
                        askChatName.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}