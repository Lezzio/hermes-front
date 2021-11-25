package fr.insalyon.hermes.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.insalyon.hermes.AppState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun globalAddMemberDialog(appState: AppState, askAddMember: MutableState<Boolean>) {

    val selectedUsers = mutableStateListOf<String>()

    if (askAddMember.value) {
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
                        askAddMember.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        selectedUsers.clear()
                        askAddMember.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}