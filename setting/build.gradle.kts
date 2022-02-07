plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose").version(Versions.compose_jb)
    kotlin("plugin.serialization").version(Versions.Kotlin.lang)
}

kotlin {
    android()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(projects.common)
                implementation(projects.common.retrofit)
                implementation("androidx.datastore:datastore-preferences:${Versions.datastore}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.Kotlin.serialization}")
                implementation("androidx.biometric:biometric-ktx:${Versions.biometric}")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

android {
    setupLibrary()
}