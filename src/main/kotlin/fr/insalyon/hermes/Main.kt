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
import fr.insalyon.hermes.model.LogChat
import kotlinx.coroutines.launch
import java.util.*

var hermesClient: HermesClient? = null

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
            if(appState.hermesClient.value?.isConnected == false) {
                appState.hermesClient.value?.connect("127.0.0.1", 5000)
                hermesClient = appState.hermesClient.value
                println("Connected")
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
                currentChatView(appState = appState, modifier = Modifier.weight(1F))

                //Conversation users viewer
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

                        if ((admin.equals(appState.username.value) || admin.equals("all")) && appState.username.value != appState.username.value) {
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
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun globalNotification(appState: AppState) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Notification")
        },
        text = {
            Text(text = appState.notification.value?.first ?: "", Modifier.defaultMinSize())
        },
        confirmButton = {
            Button(
                onClick = {
                    appState.notification.value = (appState.notification.value?.first ?: "") to false
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {}
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun globalAskUsernameDialog(appState: AppState) {

    var usernameInput by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Welcome to Hermes!")
        },
        text = {
            TextField(
                value = usernameInput,
                onValueChange = {
                    usernameInput = it
                },
                label = { Text("Choose your username") },
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    appState.username.value = usernameInput
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {}
    )
}

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun globalAddMemberDialog(appState: AppState, globalAddMemberDialog: MutableState<Boolean>) {

    val selectedUsers = mutableStateListOf<String>()

    if (globalAddMemberDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(text = "Add users")
            },
            text = {
                if (appState.usersAddable.isEmpty()) {
                    Column {
                        Text(
                            text = "No more user to add...",
                            color = Color.Red
                        )
                    }
                } else {
                    Column {
                        appState.usersAddable.forEach {
                            Spacer(Modifier.height(5.dp))
                            Row {
                                Text(
                                    text = it,
                                    color = if (selectedUsers.contains(it)) Color.Green else Color.Black,
                                    modifier = Modifier.clickable {
                                        selectedUsers.add(it)
                                    }
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        appState.hermesClient.value?.addUsers(selectedUsers)
                        selectedUsers.clear()
                        globalAddMemberDialog.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        selectedUsers.clear()
                        globalAddMemberDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

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

        appState.hermesClient.value?.appState?.chats?.forEach {
            ConversationRow(
                logChat = it,
                Modifier.align(Alignment.Start)
                    .clickable {
                        println("Access chat")
                        appState.hermesClient.value?.accessChat(it.name)
                    }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun currentChatView(appState: AppState, modifier: Modifier) {
    val scrollState = rememberScrollState(Int.MAX_VALUE)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(appState.messages.size) {
        scrollState.animateScrollTo(Int.MAX_VALUE)
    }

    //Current chat viewer
    Column(
        modifier.fillMaxHeight()
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(scrollState).weight(1F).background(Color.White)
        ) {
            appState.messages.forEach {
                MessageCard(
                    msg = Message(
                        author = it.sender,
                        body = it.content,
                        if (it.sender == appState.username.value) MessageType.SELF else MessageType.OTHER
                    ),
                    modifier = Modifier.align(if (it.sender == appState.username.value) Alignment.End else Alignment.Start)
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
                label = { Text("Type a message...") },
                modifier = Modifier
                    .weight(1F)
                    .onPreviewKeyEvent {
                        if (it.key == Key.Enter && msgInput.isNotEmpty() && msgInput.isNotBlank()) {
                            appState.hermesClient.value?.sendMessage(msgInput, appState.currentChat.value?.chatName)
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
                        if (msgInput.isNotEmpty() && msgInput.isNotBlank()) {
                            appState.hermesClient.value?.sendMessage(msgInput, "channel 3")
                            println("Clicked to send $msgInput to ${appState.currentChat.value?.chatName}}")
                            msgInput = ""
                            coroutineScope.launch {
                                scrollState.animateScrollTo(Int.MAX_VALUE)
                            }
                        }
                    }
            )
            Spacer(modifier = Modifier.size(20.dp))
        }
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
fun ConversationUserRow(appState: AppState, username: String, connected: Boolean, modifier: Modifier) {
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

        //User is admin or everyone is AND can't self ban
        val admin = appState.currentChat.value?.admin
        if ((admin.equals(appState.username.value) || admin.equals("all")) && username != appState.username.value) {
            Spacer(modifier = Modifier.width(15.dp))
            Image(
                painter = painterResource("x.png"),
                contentDescription = "Delete user",
                modifier = Modifier
                    // Set image size to 40 dp
                    .size(15.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        appState.hermesClient.value?.banUser(username)
                    }
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

    Window(onCloseRequest = {
        hermesClient?.closeClient()
        exitApplication()
    }
    ) {
        App()
    }
}
