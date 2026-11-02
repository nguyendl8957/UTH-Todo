package com.example.uthtodo.data.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signInWithCredential(
        credential: AuthCredential,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        onResult(Result.success(user))
                    } else {
                        onResult(
                            Result.failure(
                                IllegalStateException(
                                    "Firebase không trả về người dùng."
                                )
                            )
                        )
                    }
                } else {
                    onResult(
                        Result.failure(
                            task.exception
                                ?: Exception("Đăng nhập thất bại.")
                        )
                    )
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }
}
