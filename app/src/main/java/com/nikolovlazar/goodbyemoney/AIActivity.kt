package com.nikolovlazar.goodbyemoney

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nikolovlazar.goodbyemoney.models.Expense
import com.nikolovlazar.goodbyemoney.ui.theme.GoodbyeMoneyTheme
import io.realm.kotlin.ext.query

class AIActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var expenses: List<Expense> = listOf()
            expenses = db.query<Expense>().find();
            for(item in expenses){
                Log.d("DATABASE",item.amount.toString())
            }
            GoodbyeMoneyTheme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}
