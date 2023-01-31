package com.google.firebase.quickstart.auth

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice
import com.google.firebase.quickstart.auth.java.MainActivity

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
            Choice(
                "Java",
                "Run the Firebase Auth quickstart written in Java.",
                Intent(this, MainActivity::class.java)
            ),
            Choice(
                "Kotlin",
                "Run the Firebase Auth quickstart written in Kotlin.",
                Intent(this, com.google.firebase.quickstart.auth.kotlin.MainActivity::class.java)
            ),
            Choice(
                "Compose",
                "Run the Firebase Auth quickstart written in Compose.",
                Intent(this, com.google.firebase.quickstart.auth.compose.MainActivity::class.java)
            )
        )
    }
}