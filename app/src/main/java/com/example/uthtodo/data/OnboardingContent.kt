package com.example.uthtodo.data

import com.example.uthtodo.R
import com.example.uthtodo.model.OnboardingPage

object OnboardingContent {

    val pages = listOf(
        OnboardingPage(
            titleRes = R.string.onboarding_1_title,
            descriptionRes = R.string.onboarding_1_description,
            illustrationRes = R.drawable.ic_onboarding_1_2,
            buttonTextRes = R.string.onboarding_continue
        ),

        OnboardingPage(
            titleRes = R.string.onboarding_2_title,
            descriptionRes = R.string.onboarding_2_description,
            illustrationRes = R.drawable.ic_onboarding_2_2,
            buttonTextRes = R.string.onboarding_continue
        ),

        OnboardingPage(
            titleRes = R.string.onboarding_3_title,
            descriptionRes = R.string.onboarding_3_description,
            illustrationRes = R.drawable.ic_onboarding_3_2,
            buttonTextRes = R.string.onboarding_get_started
        )
    )
}
