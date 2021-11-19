// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    DesktopMaterialTheme {
        Row {
            Column(Modifier.width(250.dp).fillMaxHeight().background(Color(245, 245, 245))) {
                Text("the left column")
            }
            Column(
                Modifier.fillMaxWidth().fillMaxHeight()
            ) {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).weight(1F).background(Color.White)
                ) {
                    repeat(20) {
                        MessageCard(
                            msg = Message(
                                author = "Thomas",
                                body = "This is the message's content woww",
                                MessageType.SELF
                            ), modifier = Modifier.align(Alignment.End)
                        )
                        MessageCard(
                            msg = Message(
                                author = "Thomas",
                                body = "This is the message's content woww",
                                MessageType.OTHER
                            ), modifier = Modifier.align(Alignment.Start)
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
                        modifier = Modifier.weight(1F)
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
                                println("Clicked send icon")
                            }
                    )
                    Spacer(modifier = Modifier.size(20.dp))
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