plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.kaku.network"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}

dependencies {
    implementation(project(":kaku-core"))
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.mockk)
    testImplementation(kotlin("test"))
}
