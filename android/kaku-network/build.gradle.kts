import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "com.github.anuress.kaku"

android {
    namespace = "com.kaku.network"
    compileSdk = 34
    defaultConfig { minSdk = 21 }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "kaku-network"
            }
        }
    }
}

dependencies {
    implementation(project(":kaku-core"))
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(kotlin("test"))
}
