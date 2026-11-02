package com.example.uthtodo.data.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // Lấy người dùng Firebase hiện tại (nếu có).
    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Đăng nhập bằng Google hoặc OAuth credential.
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

    // Đăng ký tài khoản mới bằng Email/Password.
    fun registerWithEmail(
        email: String,
        password: String,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user

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
                                ?: Exception("Không thể tạo tài khoản.")
                        )
                    )
                }
            }
    }

    // Đăng nhập bằng Email/Password.
    fun signInWithEmail(
        email: String,
        password: String,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user

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
                                ?: Exception("Không thể đăng nhập.")
                        )
                    )
                }
            }
    }

    fun sendVerificationEmail(
        onResult: (Result<Unit>) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onResult(
                Result.failure(
                    IllegalStateException("Chưa có người dùng đăng nhập.")
                )
            )
            return
        }

        if (user.isEmailVerified) {
            onResult(Result.success(Unit))
            return
        }

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(Result.success(Unit))
                } else {
                    onResult(
                        Result.failure(
                            task.exception
                                ?: Exception("Không thể gửi email xác minh.")
                        )
                    )
                }
            }
    }

    fun checkEmailVerified(
        onResult: (Result<Boolean>) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            onResult(
                Result.failure(
                    IllegalStateException("Chưa có người dùng đăng nhập.")
                )
            )
            return
        }

        user.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val verified = auth.currentUser?.isEmailVerified == true
                onResult(Result.success(verified))
            } else {
                onResult(
                    Result.failure(
                        task.exception
                            ?: Exception("Không thể kiểm tra xác minh email.")
                    )
                )
            }
        }
    }

    fun sendPasswordResetEmail(
        email: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(Result.success(Unit))
                } else {
                    onResult(
                        Result.failure(
                            task.exception
                                ?: Exception("Không thể gửi email đặt lại mật khẩu.")
                        )
                    )
                }
            }
    }

    // Đăng xuất khỏi Firebase Authentication.
    fun signOut() {
        auth.signOut()
    }
}
