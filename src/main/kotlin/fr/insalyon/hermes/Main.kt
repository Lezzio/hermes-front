// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.insalyon.hermes.AppState
import fr.insalyon.hermes.chatManagePanel
import fr.insalyon.hermes.client.HermesClient
import fr.insalyon.hermes.components.*
import fr.insalyon.hermes.dialog.*
import kotlin.system.exitProcess

var hermesClient: HermesClient? = null

fun main() = application {
    Window(
        onCloseRequest = {
            hermesClient?.closeClient()
            exitApplication()
            exitProcess(0)
        }
    ) {
        App()
    }
}

@Composable
@Preview
fun App() {
    DesktopMaterialTheme {

        val appState = rememberSaveable { AppState() }

        if (appState.username.value == null) {
            println("Asking user")
            Row {
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                    globalAskUsernameDialog(appState)
                }
            }
        } else {
            //Connecting user...
            appState.hermesClient.value = rememberSaveable { HermesClient(appState.username.value, appState) }
            if (appState.hermesClient.value?.isConnected == false) {
                try {
                    appState.hermesClient.value?.connect(appState.serverAddress.value, appState.serverPort.value)
                } catch (e: Exception) {
                    e.printStackTrace()
                    appState.notification.value = "Couldn't establish the connection, try restarting the application" to true
                }
                hermesClient = appState.hermesClient.value
                println("User connected as ${appState.username.value}")
            }

            if (appState.notification.value != null && appState.notification.value?.second == true) {
                globalNotification(appState)
            }

            val askChatName = remember { mutableStateOf(false) }
            val askAddMember = remember { mutableStateOf(false) }
            val askChatUpdate = remember { mutableStateOf(false) }

            globalAskChatDialog(
                appState = appState,
                askChatName = askChatName)
            globalAddMemberDialog(
                appState = appState,
                askAddMember = askAddMember
            )
            globalAskChatUpdateDialog(
                appState = appState,
                currentChatName = appState.currentChat.value?.chatName,
                currentChatAdmin = appState.currentChat.value?.admin,
                askChatUpdate = askChatUpdate
            )

            Row {
                //Chats column
                chatPanel(appState = appState, askChatName)
                //Current chat/conversation
                currentChatView(appState = appState, modifier = Modifier.weight(1F))
                //Conversation users viewer
                chatManagePanel(appState = appState, askAddMember = askAddMember, askChatUpdate = askChatUpdate)
            }
        }
    }
}
