plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose").version(Versions.compose_jb)
    kotlin("plugin.serialization").version(Versions.Kotlin.lang)
    id("com.google.devtools.ksp").version(Versions.ksp)
}

kotlin {
    android()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.common.routeProcessor.annotations)
                kspAndroid(projects.common.routeProcessor)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(projects.common)
                implementation(projects.common.retrofit)
                implementation(projects.common.okhttp)
                implementation("androidx.paging:paging-runtime-ktx:${Versions.Androidx.paging}")
                implementation("androidx.paging:paging-compose:${Versions.Androidx.pagingCompose}")
                api("androidx.room:room-runtime:${Versions.Androidx.room}")
                api("androidx.room:room-ktx:${Versions.Androidx.room}")
                kspAndroid("androidx.room:room-compiler:${Versions.Androidx.room}")
                implementation("androidx.room:room-paging:${Versions.Androidx.room}")
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
