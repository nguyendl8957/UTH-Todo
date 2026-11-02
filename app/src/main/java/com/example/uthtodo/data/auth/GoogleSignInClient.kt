package com.example.uthtodo.data.auth

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.uthtodo.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

class GoogleSignInClient(
    private val activity: ComponentActivity
) {
    private val credentialManager =
        CredentialManager.create(activity)

    suspend fun getFirebaseCredential(): AuthCredential {
        val googleOption =
            GetSignInWithGoogleOption.Builder(
                activity.getString(R.string.default_web_client_id)
            ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val credential = result.credential

        if (
            credential !is CustomCredential ||
            credential.type !=
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException(
                "Unexpected Google credential type"
            )
        }

        val googleToken = GoogleIdTokenCredential
            .createFrom(credential.data)
            .idToken

        return GoogleAuthProvider.getCredential(
            googleToken,
            null
        )
    }
}
