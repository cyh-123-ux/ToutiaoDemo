package com.example.toutiaodemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.toutiaodemo.data.db.NewsDatabase
import com.example.toutiaodemo.ui.MainScreen
import com.example.toutiaodemo.ui.theme.ToutiaoDemoTheme

class MainActivity : ComponentActivity() {
    private lateinit var db: NewsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = NewsDatabase.getInstance(this)
        setContent {
            ToutiaoDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(db = db)
                }
            }
        }
    }
}