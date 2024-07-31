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
            apiKey = "AIzaSyB0HMyUSms6luN1SEj7Vmw-jg4e7KkxSsM",
            generationConfig = config
        )
    }

    fun questioning(userInput: String) {
        _uiState.value = HomeUiState.Loading
        var expenses: List<Expense> = listOf()


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
        val prompt = "Please answer only if the following question is related to finance, else refuse to answer:$userInput"
        for(key in ansmap.keys){
            Log.d("DATABASE",key.toString() + ansmap[key].toString())
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

    fun initalQuestioning() {
        _uiState.value = HomeUiState.Loading
        var expenses: List<Expense> = listOf()


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
        var prompt = "Give me a financial advice. The following is my category wise spending: "
        for(key in ansmap.keys){
            prompt += key.toString() + ":" + ansmap[key].toString()
        }
        prompt += "Help me in financial advice and a way to improve my finances in a short and consize manner."

        Log.d("PROMPT", prompt)
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
