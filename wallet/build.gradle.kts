plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization").version(Versions.Kotlin.lang)
    id("com.google.devtools.ksp").version(Versions.ksp)
    id("org.jetbrains.compose").version(Versions.compose_jb)
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
                implementation("androidx.compose.runtime:runtime-livedata:1.0.5")

                implementation("androidx.navigation:navigation-ui-ktx:${Versions.navigation}")
                implementation("androidx.navigation:navigation-compose:${Versions.navigation}")

                implementation("joda-time:joda-time:2.10.13")
                implementation("io.github.dimensiondev:maskwalletcore:0.4.0")

                implementation(projects.debankapi)
                implementation(projects.common)
                implementation(projects.common.retrofit)
                implementation(projects.common.okhttp)

                api("androidx.room:room-runtime:${Versions.room}")
                api("androidx.room:room-ktx:${Versions.room}")
                kspAndroid("androidx.room:room-compiler:${Versions.room}")
                implementation("androidx.room:room-paging:${Versions.room}")

                implementation("androidx.paging:paging-runtime-ktx:3.1.0")
                implementation("androidx.paging:paging-compose:1.0.0-alpha14")

                implementation("com.journeyapps:zxing-android-embedded:4.3.0")

                implementation("androidx.core:core-ktx:1.7.0")
                implementation("androidx.appcompat:appcompat:1.4.1")
                implementation("com.google.android.material:material:1.5.0")

                implementation("com.github.WalletConnect:kotlin-walletconnect-lib:0.9.7")
                implementation("com.squareup.moshi:moshi:1.8.0")
                implementation("com.github.komputing.khex:extensions:1.1.2")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation("androidx.test.ext:junit:1.1.3")
                implementation("androidx.test.espresso:espresso-core:3.4.0")
            }
        }
    }
}

android {
    setupLibrary()
}
