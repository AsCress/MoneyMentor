package com.nikolovlaza

import android.util.Log
import com.nikolovlazar.goodbyemoney.db


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.ai.client.generativeai.type.content
import com.nikolovlazar.goodbyemoney.BuildConfig
import com.nikolovlazar.goodbyemoney.models.Expense
import io.realm.kotlin.ext.query
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
        var expenses: List<Expense> = listOf()
        val prompt = "$userInput"

        expenses = db.query<Expense>().find()
        var arraylist = ArrayList<String>()
        var hashMap : HashMap<String,Int> = HashMap<String,Int>()
        var ansmap : HashMap<String,Int> = HashMap<String,Int>()
        for(items in expenses){
            if(hashMap.containsKey(items.category?.name.toString())){
                hashMap.put(items.category?.name.toString(),2)
            }
            else{
                arraylist.add(items.category?.name.toString())
            }
        }
        for(category in arraylist){
            var sum=0
            for(items in expenses){
                if(items.category?.name  == category){
                    sum+=items.amount.toInt()
                }
            }
            ansmap.put(category,sum)
        }
        for(key in ansmap.keys){
            Log.d("DATABASE",key.toString() + ansmap[key].toString())
            //println(hashMap[key])
        }

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
