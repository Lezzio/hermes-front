package fr.insalyon.hermes.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.insalyon.hermes.model.LogChat
import java.text.SimpleDateFormat

@Composable
fun ConversationRow(activeChat: Boolean, logChat: LogChat, modifier: Modifier) {
    // Add padding around our message
    val chatColor = if(activeChat) Color(189, 211, 255) else Color.White
    Row(
        modifier = modifier.background(chatColor)
            .padding(all = 8.dp)
            .fillMaxWidth(1F),
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
            val formatter = SimpleDateFormat("MM/dd 'at' HH:mm")
            Text(
                text = formatter.format(logChat.message.time),
                modifier = Modifier.padding(all = 1.dp),
                style = MaterialTheme.typography.body2
            )
        }
    }
}