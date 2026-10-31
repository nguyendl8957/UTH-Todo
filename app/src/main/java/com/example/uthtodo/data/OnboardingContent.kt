package com.example.uthtodo.data

import com.example.uthtodo.R
import com.example.uthtodo.model.OnboardingPage

object OnboardingContent {

    val pages = listOf(
        OnboardingPage(
            title = "Mọi việc, gọn trong một chỗ",
            description = "Ghi lại bài tập, việc nhà, lịch hẹn. Không còn quên việc nào.",
            illustrationRes = R.drawable.ic_onboarding_1_2,
            buttonText = "Tiếp tục"
        ),

        OnboardingPage(
            title = "Chia theo Học tập và Cuộc sống",
            description = "Mỗi việc có nhóm riêng, nhìn là biết hôm nay cần lo gì.",
            illustrationRes = R.drawable.ic_onboarding_2_2,
            buttonText = "Tiếp tục"
        ),

        OnboardingPage(
            title = "Nhắc đúng lúc",
            description = "Bật thông báo để UTH Todo nhắc bạn trước hạn chót.",
            illustrationRes = R.drawable.ic_onboarding_3_2,
            buttonText = "Bắt đầu ngay"
        )
    )
}
