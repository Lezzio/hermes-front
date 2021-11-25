package fr.insalyon.hermes.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.insalyon.hermes.AppState

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
            Text(
                text = if (connected) "Connecté" else "Déconnecté",
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.body2,
                color = if (connected) Color(101, 200, 122) else Color(236, 90, 70)
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