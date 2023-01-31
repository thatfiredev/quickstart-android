package com.google.firebase.quickstart.auth.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.compose.googlesignin.GoogleSignIn
import com.google.firebase.quickstart.auth.compose.ui.theme.QuickstartandroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickstartandroidTheme {
                // A surface container using the 'background' color from the theme
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = stringResource(id = R.string.app_name)) }
                        )
                    }
                ) { contentPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        color = MaterialTheme.colors.background
                    ) {
                        NavHost(navController = navController, startDestination = "chooser") {
                            composable("chooser") {
                                ChooserScreen(
                                    onNavigateToOption = { screenName ->
                                        navController.navigate(screenName)
                                    }
                                )
                            }
                            composable("GoogleSignIn") {
                                GoogleSignIn()
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    QuickstartandroidTheme {
        Greeting("Android")
    }
}