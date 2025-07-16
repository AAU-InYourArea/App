import aau.inyourarea.app.network.CommandType
import aau.inyourarea.app.network.NetworkServiceHolder
import aau.inyourarea.app.network.messages.CreateRoomRequest
import aau.inyourarea.app.network.messages.JoinRoomRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


data class Chatroom(
    val id: Int,
    var name: String,

)


object ChatroomHolder {
    var chatroom: MutableState<Chatroom?> = mutableStateOf(null)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatroomsScreen(navController: NavController, networkService: NetworkServiceHolder) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var chatrooms by remember { mutableStateOf(emptyArray<Chatroom>()) }
    var loadingError by remember { mutableStateOf<String?>(null) }

    networkService.service?.let {
        it.getChatrooms().thenAccept { loadedRooms ->
            runBlocking(Dispatchers.Main) {
                chatrooms = loadedRooms.map { room ->
                    Chatroom(
                        id = room.id,
                        name = room.name
                    )
                }.toTypedArray()
            }
            loadingError = null
        }.exceptionally { e ->
            loadingError = "Fehler beim Laden: ${e.localizedMessage}"
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chatrooms") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top
            ) {
                Button(
                    onClick = { showAddDialog = true }
                ) {
                    Text(
                        text = "Create",
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showSearchDialog = true }
                ) {
                    Text(
                        text = "Search",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loadingError != null) {
                Text(
                    text = loadingError ?: "",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                if (chatrooms.isEmpty()) {
                    item {
                        Text(
                            text = "Keine Chatrooms gefunden",
                            fontSize = 20.sp,
                            color = Color.Red
                        )
                    }
                } else {
                    chatrooms.forEach { room ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                                    .clickable {
                                        ChatroomHolder.chatroom.value = room
                                        navController.navigate("DisplayChatroom")
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = room.name,
                                    modifier = Modifier.padding(8.dp),
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            if (showAddDialog) {
                AddChatroom(
                    networkService = networkService,
                    onDismiss = { showAddDialog = false },
                    onChatroomCreated = { newRoom ->
                        ChatroomHolder.chatroom.value = newRoom
                        networkService.service.getChatrooms().thenAccept { loadedRooms ->
                            chatrooms = loadedRooms.map { room ->
                                Chatroom(
                                    id = room.id,
                                    name = room.name
                                )
                            }.toTypedArray()
                        }

                        showAddDialog = false
                    }
                )
            }

            if (showSearchDialog) {
                SearchChatroom(
                    chatrooms = chatrooms,
                    onDismiss = { showSearchDialog = false },
                    onChatroomSelected = {
                        ChatroomHolder.chatroom.value = it
                        navController.navigate("DisplayChatroom")
                        showSearchDialog = false
                    }
                )
            }
        }
    }
}


@Composable
fun SearchChatroom(
    chatrooms: Array<Chatroom>,
    onDismiss: () -> Unit,
    onChatroomSelected: (Chatroom) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredChatrooms = chatrooms.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Chatroom") },
                    colors =  OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(filteredChatrooms.size) { index ->
                        val room = filteredChatrooms[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    onChatroomSelected(room)
                                    onDismiss()
                                }
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp)),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = room.name,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss
                ) {
                    Text("Close")
                }
            }
        }
    }
}



@Composable
fun AddChatroom(
    networkService: NetworkServiceHolder,
    onDismiss: () -> Unit,
    onChatroomCreated: (Chatroom) -> Unit
) {
    var chatroomName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                val textColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray
                )

                OutlinedTextField(
                    value = chatroomName,
                    onValueChange = { chatroomName = it },
                    label = { Text("Chatroom Name") },
                    colors = textColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    colors = textColors,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (chatroomName.isBlank()) {
                            errorMessage = "Name darf nicht leer sein"
                            return@Button
                        }
                        errorMessage = null
                        networkService.service.sendCommand(CommandType.CREATE_ROOM, CreateRoomRequest(chatroomName, password) ).thenAccept{
                            runBlocking(Dispatchers.Main) {
                                val createdRoom = Chatroom(it.toInt(), chatroomName)
                                onChatroomCreated(createdRoom)
                                onDismiss()
                            }
                        }.exceptionally { e ->
                            errorMessage = "Fehler: ${e.localizedMessage}"
                            null
                        }
                    }
                ) {
                    Text("Chatroom erstellen", fontStyle = FontStyle.Italic)
                }
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = Color.Red)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayChatroomDetail(navController: NavController, networkService: NetworkServiceHolder) {
    var password by rememberSaveable { mutableStateOf("") }
    val chatroom = ChatroomHolder.chatroom
    var joinError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (chatroom.value != null) {
                        "Join Chatroom: ${chatroom.value!!.name}"
                    } else {
                        "Kein Chatroom ausgewählt"
                    })
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Passwort") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    chatroom.value?.let { room ->
                        joinError = null
                        networkService.service.sendCommand(CommandType.JOIN_ROOM, JoinRoomRequest(room.id, password)).thenAccept { result ->
                            runBlocking(Dispatchers.Main) {
                                if (result.toInt() == room.id) {
                                    navController.navigate("main") {
                                        popUpTo("chatrooms") {
                                            inclusive = true
                                        }
                                    }
                                } else {
                                    joinError = "Fehler: $result"
                                }
                            }
                        }.exceptionally { e ->
                            joinError = "Netzwerkfehler: ${e.localizedMessage}"
                            null
                        }
                    }
                }
            ) {
                Text("Join")
            }

            joinError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red)
            }
        }
    }
}