// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.insalyon.hermes.AppState
import fr.insalyon.hermes.chatManagePanel
import fr.insalyon.hermes.client.HermesClient
import fr.insalyon.hermes.components.*
import fr.insalyon.hermes.dialog.globalAddMemberDialog
import fr.insalyon.hermes.dialog.globalAskChatDialog
import fr.insalyon.hermes.dialog.globalAskUsernameDialog
import fr.insalyon.hermes.dialog.globalNotification
import fr.insalyon.hermes.model.LogChat
import kotlinx.coroutines.launch
import java.util.*

var hermesClient: HermesClient? = null

fun main() = application {
    Window(
        onCloseRequest = {
            hermesClient?.closeClient()
            exitApplication()
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
                appState.hermesClient.value?.connect(appState.serverAddress.value, appState.serverPort.value)
                hermesClient = appState.hermesClient.value
                println("User connected as ${appState.username.value}")
            }

            if (appState.notification.value != null && appState.notification.value?.second == true) {
                globalNotification(appState)
            }

            val askChatName = remember { mutableStateOf(false) }
            val askAddMember = remember { mutableStateOf(false) }

            globalAskChatDialog(appState, askChatName)
            globalAddMemberDialog(appState, askAddMember)

            Row {
                //Chats column
                chatPanel(appState = appState, askChatName)
                //Current chat/conversation
                currentChatView(appState = appState, modifier = Modifier.weight(1F))
                //Conversation users viewer
                chatManagePanel(appState = appState, askAddMember = askAddMember)
            }
        }
    }
}

@Composable
fun ConversationRow(logChat: LogChat, modifier: Modifier) {
    // Add padding around our message
    Row(
        modifier = modifier.padding(all = 8.dp),
//        horizontalArrangement = if(msg.messageType == MessageType.SELF) androidx.compose.foundation.layout.Arrangement.End else androidx.compose.foundation.layout.Arrangement.Start
    ) {
        Image(
            painter = painterResource("people.svg"),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                // Set image size to 40 dp
                .size(40.dp)
                // Clip image to be shaped as a circle
                .clip(CircleShape)
        )

        // Add a horizontal space between the image and the column
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = logChat.name,
            )
            // Add a vertical space between the author and message texts
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${logChat.message.sender}: ${logChat.message.content}",
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.body2
            )
        }
    }
}