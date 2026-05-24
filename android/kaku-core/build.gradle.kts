plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

group = "com.github.anuress.kaku"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "1.8" }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.okhttp)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.kaku.core"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}
