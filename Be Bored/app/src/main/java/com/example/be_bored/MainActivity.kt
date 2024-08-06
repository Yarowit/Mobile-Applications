package com.example.be_bored

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import com.example.be_bored.database.AppDatabase
import com.example.be_bored.database.Database

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //uruchomienie service
        Intent(this, GoalsCheckingService::class.java).also { intent ->
            startService(intent)
        }

        // inicjalizacja bazy danych
        Database.db = Room.databaseBuilder(
            baseContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()
    }
}