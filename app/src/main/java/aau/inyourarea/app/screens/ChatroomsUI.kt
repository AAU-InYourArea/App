
import aau.inyourarea.app.network.CommandType
import aau.inyourarea.app.network.NetworkService
import aau.inyourarea.app.network.NetworkServiceHolder
import aau.inyourarea.app.network.messages.CreateRoomRequest
import aau.inyourarea.app.network.messages.JoinRoomRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Color

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class Chatroom(
    val id: Int,
    var name: String,

)


object ChatroomHolder {
    var chatroom: Chatroom? = null
}



@Composable
fun ChatroomsScreen(navController: NavController, networkService: NetworkServiceHolder) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var currentChatroom by remember { mutableStateOf<Chatroom?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var chatrooms by remember { mutableStateOf(emptyArray<Chatroom>()) }
    var loadingError by remember { mutableStateOf<String?>(null) }

    if (networkService.service != null) {

        LaunchedEffect(Unit) {
            networkService.service.getChatrooms().thenAccept { loadedRooms ->
                chatrooms = loadedRooms.map { room ->
                    Chatroom(
                        id = room.id,
                        name = room.name
                    )
                }.toTypedArray()
                loadingError = null
            }.exceptionally { e ->
                loadingError = "Fehler beim Laden: ${e.localizedMessage}"
                null
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.End) {

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Add Chatroom", fontStyle = FontStyle.Italic, color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                currentChatroom?.let {
                    Button(
                        onClick = { navController.navigate("DisplayChatroom") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Show Chatroom", fontStyle = FontStyle.Italic, color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showSearchDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Search Chatroom", fontStyle = FontStyle.Italic, color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { navController.navigate("main") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Back to Main Page", fontStyle = FontStyle.Italic, color = Color.DarkGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loadingError != null) {
            Text(
                text = loadingError ?: "",
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(modifier = Modifier.fillMaxWidth(0.6f).padding(16.dp)) {
            chatrooms.forEach { room ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color.DarkGray)
                            .clickable { currentChatroom = room },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = room.name,
                            color = Color.White,
                            modifier = Modifier.padding(8.dp),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddChatroom(
                networkService = networkService,
                onDismiss = { showAddDialog = false },
                onChatroomCreated = { newRoom ->
                    currentChatroom = newRoom

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
                    currentChatroom = it
                    showSearchDialog = false
                }
            )
        }

        currentChatroom?.let {
            ChatroomHolder.chatroom = it
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
                .background(Color.Black)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Chatroom", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
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
                                .clickable {
                                    onChatroomSelected(room)
                                    onDismiss()
                                }
                                .padding(8.dp)
                                .background(Color.DarkGray),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = room.name,
                                color = Color.White,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Close", color = Color.DarkGray, fontStyle = FontStyle.Italic)
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
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.background(Color.Black).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                OutlinedTextField(
                    value = chatroomName,
                    onValueChange = { chatroomName = it },
                    label = { Text("Chatroom Name", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (chatroomName.isBlank()) {
                            errorMessage = "Name darf nicht leer sein"
                            return@Button
                        }
                        errorMessage = null
                        coroutineScope.launch(Dispatchers.Main) {

                                networkService.service.sendCommand(CommandType.CREATE_ROOM, CreateRoomRequest(chatroomName, password) ).thenAccept{
                                    val createdRoom = Chatroom(it.toInt(), chatroomName)
                                    onChatroomCreated(createdRoom)
                                    onDismiss()
                                }.exceptionally { e ->
                                    errorMessage = "Fehler: ${e.localizedMessage}"
                                    null
                                }

                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Chatroom erstellen", fontStyle = FontStyle.Italic, color = Color.DarkGray)
                }
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = Color.Red)
                }
            }
        }
    }
}



@Composable
fun DisplayChatroomDetail(navController: NavController, networkService: NetworkService) {
    var password by rememberSaveable { mutableStateOf("") }
    val chatroom = ChatroomHolder.chatroom
    val coroutineScope = rememberCoroutineScope()
    var joinError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                if (chatroom != null) {
                    Text(
                        text = "Chatroom: ${chatroom.name}",
                        color = Color.White,
                        fontStyle = FontStyle.Italic,
                        fontSize = 20.sp
                    )
                } else {
                    Text("Kein Chatroom ausgewählt", color = Color.Red)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxHeight()) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        chatroom?.let { room ->
                            joinError = null
                            networkService.sendCommand(CommandType.JOIN_ROOM, JoinRoomRequest(room.id, password)).thenAccept { result ->
                                if (result.toInt() == room.id) {
                                    navController.navigate("insidechatrooms") {
                                        popUpTo("ChatroomsScreen") { inclusive = true }
                                    }
                                } else {
                                    joinError = "Fehler: $result"
                                }
                            }.exceptionally { e ->
                                joinError = "Netzwerkfehler: ${e.localizedMessage}"
                                null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                ) {
                    Text("Join", color = Color.DarkGray, fontStyle = FontStyle.Italic)
                }

                joinError?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = Color.Red)
                }
            }
        }
    }
}