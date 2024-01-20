package com.nikolovlazar.goodbyemoney


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.ai.client.generativeai.type.content
import com.nikolovlazar.goodbyemoney.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {

    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private lateinit var generativeModel: GenerativeModel


    init{
        val config = generationConfig{
            temperature = 0f
        }

        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = "AIzaSyBjSKOboV0zxG1rhwE2qmMHdSflJXYNnFQ",
            generationConfig = config
        )
    }

    fun questioning(userInput: String) {
        _uiState.value = HomeUiState.Loading
        val prompt = "$userInput"

        viewModelScope.launch(Dispatchers.IO) {

            try{
                val content = content {
                    text(prompt)
                }

                var output = ""
                generativeModel.generateContentStream(content).collect{
                    output += it.text
                    _uiState.value = HomeUiState.Success(output);
                }
            }catch (e: Exception){
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Error in generating content")
            }
        }
    }
}

sealed interface HomeUiState {
    object Initial : HomeUiState
    object Loading : HomeUiState
    data class Success(
        val outputText: String
    ) : HomeUiState

    data class Error(
        val error: String
    ) : HomeUiState
}
