package com.google.firebase.quickstart.auth.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.SignInMethod
import com.google.firebase.quickstart.auth.compose.ui.theme.QuickstartandroidTheme

val signInMethods = listOf(
    SignInMethod("GoogleSignIn", R.string.desc_google_sign_in, R.id.action_google),
    SignInMethod("FacebookLogin", R.string.desc_facebook_login, R.id.action_facebook),
    SignInMethod("EmailPassword", R.string.desc_emailpassword, R.id.action_emailpassword),
    SignInMethod("Passwordless", R.string.desc_passwordless, R.id.action_passwordless),
    SignInMethod("PhoneAuth", R.string.desc_phone_auth, R.id.action_phoneauth),
    SignInMethod("AnonymousAuth", R.string.desc_anonymous_auth, R.id.action_anonymousauth),
    SignInMethod("FirebaseUI", R.string.desc_firebase_ui, R.id.action_firebaseui),
    SignInMethod("CustomAuth", R.string.desc_custom_auth, R.id.action_customauth),
    SignInMethod("GenericIdp", R.string.desc_generic_idp, R.id.action_genericidp),
    SignInMethod("MultiFactor", R.string.desc_multi_factor, R.id.action_mfa),
)

@Composable
fun ChooserScreen(
    onNavigateToOption: (selectedMethod: String) -> Unit
) {
    LazyColumn {
        items(signInMethods) { method ->
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 8.dp)
                    .clickable {
                        onNavigateToOption(method.className)
                    }
            ) {
                Text(
                    text = method.className,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = stringResource(id = method.descriptionId),
                    style = MaterialTheme.typography.body1
                )
                Divider(modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun ChooserScreenPreview() {
    QuickstartandroidTheme {
        ChooserScreen {}
    }
}
