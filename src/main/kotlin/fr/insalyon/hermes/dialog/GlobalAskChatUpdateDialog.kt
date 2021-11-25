package fr.insalyon.hermes.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.insalyon.hermes.AppState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun globalAskChatUpdateDialog(
    appState: AppState,
    currentChatName: String?,
    currentChatAdmin: String?,
    askChatUpdate: MutableState<Boolean>
) {

    println("Rendering chat update")
    var chatNameInput by mutableStateOf(currentChatName ?: "")
    val selectedAdmin = mutableStateOf(currentChatAdmin ?: "all")

    if (askChatUpdate.value) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(text = "Update chat")
            },
            text = {
                Column {
                    TextField(
                        value = chatNameInput,
                        onValueChange = {
                            chatNameInput = it
                        },
                        label = { Text("Choose your chat's name") },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Choose an admin", color = Color.Black, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    //Everyone's admin option
                    selectableAdminOption(
                        adminName = "all",
                        selectedAdmin = selectedAdmin
                    )
                    //Individual admin option
                    appState.usersConnected.value.keys.forEach {
                        selectableAdminOption(
                            adminName = it,
                            selectedAdmin = selectedAdmin
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        println("Selected admin = ${selectedAdmin.value}")
                        appState.hermesClient.value?.updateChat(currentChatName, chatNameInput, selectedAdmin.value)
                        askChatUpdate.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        askChatUpdate.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Composable
fun selectableAdminOption(adminName: String, selectedAdmin: MutableState<String>) {
    Spacer(Modifier.height(5.dp))
    Row {
        Text(
            text = adminName,
            color = if (selectedAdmin.value == adminName) Color.Green else Color.Black,
            modifier = Modifier.clickable {
                selectedAdmin.value = adminName
            },
            fontSize = 15.sp
        )
    }
    Spacer(Modifier.height(5.dp))
}
