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
import fr.insalyon.hermes.client.HermesClient
import fr.insalyon.hermes.model.ChatInfo
import fr.insalyon.hermes.model.LogChat
import fr.insalyon.hermes.model.TextMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {

    var username = "aguigal"
    val appState = AppState()
    val hermesClient = HermesClient(username, appState)
    hermesClient.connect("127.0.0.1", 5000)
    println("Connected")

    DesktopMaterialTheme {
        Row {
            //Chats column
            Column(
                Modifier.width(250.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .background(Color(245, 245, 245))
            ) {
                Text(
                    "Créer un chat",
                    Modifier.clickable {
                        hermesClient.createChat("channel 3")
                    }
                )
                repeat(1) {
                    ConversationRow(
                        logChat = LogChat(
                            "Conversation Dark INSA",
                            listOf("benoît"),
                            TextMessage("bijour!", "Benoît", "fokz", Date())
                        ),
                        Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                hermesClient.appState.chats.forEach {
                    ConversationRow(
                        logChat = it,
                        Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
            val scrollState = rememberScrollState(Int.MAX_VALUE)
            val coroutineScope = rememberCoroutineScope()

            //Current chat viewer
            Column(
                Modifier.weight(1F).fillMaxHeight()
            ) {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(scrollState).weight(1F).background(Color.White)
                ) {
                    appState.currentChat.value?.messages?.forEach {
                        MessageCard(
                            msg = Message(
                                author = it.sender,
                                body = it.content,
                                if (it.sender == username) MessageType.SELF else MessageType.OTHER
                            ), modifier = Modifier.align(if (it.sender == username) Alignment.End else Alignment.Start)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(Color.White)
                ) {
                    var msgInput by rememberSaveable { mutableStateOf("") }
                    TextField(
                        value = msgInput,
                        onValueChange = {
                            msgInput = it
                        },
                        label = { Text("Écrivez un message") },
                        modifier = Modifier
                            .weight(1F)
                            .onPreviewKeyEvent {
                                if (it.key == Key.Enter) {
                                    hermesClient.sendMessage(msgInput, "channel 3")
                                    println("Clicked to send $msgInput to ${appState.currentChat.value?.chatName}}")
                                    msgInput = ""
                                    coroutineScope.launch {
                                        scrollState.animateScrollTo(Int.MAX_VALUE)
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                    )
                    Spacer(modifier = Modifier.size(20.dp))
                    Image(
                        painter = painterResource("send.svg"),
                        contentDescription = "Contact profile picture",
                        modifier = Modifier
                            // Set image size to 40 dp
                            .size(40.dp)
                            // Clip image to be shaped as a circle
                            .clip(CircleShape)
                            .clickable {
                                hermesClient.sendMessage(msgInput, "channel 3")
                                println("Clicked to send $msgInput to ${appState.currentChat.value?.chatName}}")
                                msgInput = ""
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(Int.MAX_VALUE)
                                }
                            }
                    )
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }
            //Conversation users viewer
            Column(
                Modifier.width(250.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .background(Color(245, 245, 245))
            ) {
                appState.usersConnected.value.entries.forEach {
                    ConversationUserRow(
                        username = it.key,
                        connected = it.value,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }
        }

//        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
//            Button(onClick = {
//                text = "Hello, Desktop!"
//            }) {
//                Text(text)
//            }
//            repeat(10) {
//                MessageCard(
//                    Message(
//                        author = "Enitrat le S",
//                        body = "Gros la blockchain c'est stylé"
//                    )
//                )
//            }
//        }
    }

}

enum class MessageType {
    SELF,
    OTHER
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

@Composable
fun ConversationUserRow(username: String, connected: Boolean, modifier: Modifier) {
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
                text = username,
            )
            // Add a vertical space between the author and message texts
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (connected) "Connecté" else "Déconnecté",
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.body2,
                color = if (connected) Color.Green else Color.Red
            )
        }
    }
}

data class Message(val author: String, val body: String, val messageType: MessageType)

@Composable
fun MessageCard(msg: Message, modifier: Modifier) {
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
                text = msg.author,
            )
            // Add a vertical space between the author and message texts
            Spacer(modifier = Modifier.height(4.dp))

            Surface(shape = MaterialTheme.shapes.medium, elevation = 1.dp) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
