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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


data class Chatroom(          // Datenklasse für Chatrooms
    val id: Int,
    var name: String,

)


object ChatroomHolder {      // Singleton-Objekt, um den aktuellen Chatroom zu halten
    var chatroom: MutableState<Chatroom?> = mutableStateOf(null)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatroomsScreen(navController: NavController, networkService: NetworkServiceHolder) {  // Hauptbildschirm für Chatrooms
    var showAddDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var chatrooms by remember { mutableStateOf(emptyArray<Chatroom>()) }
    var loadingError by remember { mutableStateOf<String?>(null) }

    networkService.service?.let {               // Überprüfen, ob der Netzwerkdienst verfügbar ist
        it.getChatrooms().thenAccept { loadedRooms ->
            runBlocking(Dispatchers.Main) {
                chatrooms = loadedRooms.map { room ->    //Laden der Chatrooms vom Server
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
            Row(                                                    // Reihe für Buttons "Create" und "Search"
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

            LazyColumn(                                                                        // LazyColumn für die Anzeige der Chatrooms
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                                    .background(
                                        MaterialTheme.colorScheme.secondary,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        ChatroomHolder.chatroom.value = room                        // Setzen des ausgewählten Chatrooms
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
                AddChatroom(                                            // Aufruf des Dialogs zum Erstellen eines neuen Chatrooms
                    networkService = networkService,
                    onDismiss = { showAddDialog = false },
                    onChatroomCreated = { newRoom ->
                        ChatroomHolder.chatroom.value = newRoom
                        networkService.service.getChatrooms().thenAccept { loadedRooms ->   // Aktualisieren der Chatrooms nach dem Erstellen eines neuen
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
                SearchChatroom(                                         // Aufruf des Suchdialogs
                    chatrooms = chatrooms,
                    onDismiss = { showSearchDialog = false },
                    onChatroomSelected = {
                        ChatroomHolder.chatroom.value = it
                        navController.navigate("DisplayChatroom")                   // Setzen des aktuellen Chatrooms und Navigaton zum Detailbildschirm des ausgewählten Chatrooms
                        showSearchDialog = false
                    }
                )
            }
        }
    }
}


@Composable
fun SearchChatroom(                             // Dialog zum Suchen von Chatrooms
    chatrooms: Array<Chatroom>,
    onDismiss: () -> Unit,
    onChatroomSelected: (Chatroom) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredChatrooms = chatrooms.filter {
        it.name.contains(searchQuery, ignoreCase = true)           // Filtert die Chatrooms basierend auf der Suchanfrage
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column {
                OutlinedTextField(
                    value = searchQuery,                                // Eingabefeld für die Suchanfrage
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
                    items(filteredChatrooms.size) { index ->                            // Durchlaufen der gefilterten Chatrooms und Anzeige in einer LazyColumn
                        val room = filteredChatrooms[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    onChatroomSelected(room)
                                    onDismiss()
                                }
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    RoundedCornerShape(8.dp)
                                ),
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
fun AddChatroom(                                        // Dialog zum Erstellen eines neuen Chatrooms
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
                    value = chatroomName,                   // Eingabefeld für den Chatroom-Namen
                    onValueChange = { chatroomName = it },
                    label = { Text("Chatroom Name") },
                    colors = textColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,                       // Eingabefeld für das Passwort
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    colors = textColors,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
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
                        networkService.service.sendCommand(CommandType.CREATE_ROOM, CreateRoomRequest(chatroomName, password) ).thenAccept{ // Senden des Befehls zum Erstellen eines Chatrooms
                            runBlocking(Dispatchers.Main) {
                                val createdRoom = Chatroom(it.toInt(), chatroomName)        // Erstellen eines Chatroom-Objekts mit der ID und dem Namen
                                onChatroomCreated(createdRoom)                              // Callback, um den erstellten Chatroom zurückzugeben
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
fun DisplayChatroomDetail(navController: NavController, networkService: NetworkServiceHolder) {     // Detailansicht eines ausgewählten Chatrooms
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },                      // Eingabefeld für das Passwort
                label = { Text("Passwort") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    chatroom.value?.let { room ->       // Überprüfen, ob ein Chatroom ausgewählt ist
                        joinError = null
                        networkService.service.sendCommand(CommandType.JOIN_ROOM, JoinRoomRequest(room.id, password)).thenAccept { result -> // Senden des Befehls zum Beitreten eines Chatrooms
                            runBlocking(Dispatchers.Main) {
                                if (result.toInt() == room.id) {          // Überprüfen, ob das Ergebnis der ID des Chatrooms entspricht
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