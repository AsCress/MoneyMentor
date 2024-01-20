package com.nikolovlazar.goodbyemoney


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nikolovlaza.HomeUiState
import com.nikolovlaza.HomeViewModel

@Composable
fun AppContent(viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val appUiState = viewModel.uiState.collectAsState()

    HomeScreen(uiState = appUiState.value) { inputText ->

        viewModel.questioning(userInput = inputText)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(uiState: HomeUiState = HomeUiState.Loading, onSendCliked: (String) -> Unit) {

    var userQues by rememberSaveable() {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Gemini AI ChatBot")}, colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(
                0xFF1B1B1B
            ), titleContentColor = MaterialTheme.colorScheme.onPrimary))
        },
        bottomBar = {
            Column {
                Row(modifier = Modifier.padding(vertical = 20.dp, horizontal = 30.dp), verticalAlignment = Alignment.CenterVertically){

                   // Divider(modifier = Modifier.height(5.dp))
                    // Input Field
                    OutlinedTextField(value = userQues, onValueChange = {
                        userQues = it
                    },
                        placeholder = {Text(text = "Ask question")}, modifier = Modifier.fillMaxWidth(0.83f)
                    )

                    // Send Button
                    IconButton(
                        modifier = Modifier
                        ,onClick = {
                        if(userQues.isNotBlank()) {
                            onSendCliked(userQues)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    ) {
        Column(modifier = Modifier
            .padding(it)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())) {
            when(uiState) {
                is HomeUiState.Initial -> {}
                is HomeUiState.Loading -> {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HomeUiState.Success -> {
                    Card(modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                        Text(text = uiState.outputText)
                    }
                }
                is HomeUiState.Error -> {
                    Card(modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(), shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(text = uiState.error)
                    }
                }
            }
        }
    }
}
