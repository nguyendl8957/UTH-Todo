plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.uthtodo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.uthtodo"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.work:work-runtime:2.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Quản lý phiên bản các thư viện Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.14.1"))
    // Firebase Authentication: đăng nhập, đăng ký, quản lý tài khoản
    implementation("com.google.firebase:firebase-auth")
    // Quản lý thông tin đăng nhập và giao diện chọn tài khoản
    implementation("androidx.credentials:credentials:1.6.0")
    // Kết nối Credential Manager với Google Play Services
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    // Hỗ trợ lấy Google ID token
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("com.google.firebase:firebase-firestore")

}
