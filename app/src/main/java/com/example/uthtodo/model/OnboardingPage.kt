package com.example.uthtodo.model

import androidx.annotation.DrawableRes

data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes val illustrationRes: Int,
    val buttonText: String
)