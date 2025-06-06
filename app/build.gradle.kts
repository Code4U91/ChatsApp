import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"

}

android {
    namespace = "com.example.chatapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chatapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val localProperties = Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }
        val agoraAppId = localProperties.getProperty("AGORA_APP_ID") ?: ""

        buildConfigField("String", "AGORA_APP_ID", agoraAppId)

        // remove this if it cause error on some android versions, currently works fine on most
        // tested on android version 9 huawei y9
        // canceled for testing it on emulators
        // ABI Filtering - Keep only necessary architectures
//        ndk {
//            abiFilters += listOf("armeabi-v7a", "arm64-v8a") // Exclude x86 and x86_64
//        }

        buildFeatures {
            buildConfig = true  // Enable BuildConfig generation
        }


    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes +=  "/META-INF/{AL2.0,LGPL2.1}"
                //"META-INF/versions/9/OSGI-INF/MANIFEST.MF"

        }
    }
}

dependencies {


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.googleid)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)




    // Coil for image processing
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    //NAVIGATION

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

 
    implementation(libs.androidx.credentials.play.services.auth)

    //HILT
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)


    implementation(libs.androidx.material.icons.extended)

    implementation (libs.firebase.messaging)

    //agora for video call
   // implementation ("io.agora.rtc:full-sdk:4.5.1")

    // data store
    implementation (libs.androidx.datastore.preferences)


    // light version
    implementation (libs.lite.sdk) // remove this and use full version if creating issue


    //
    implementation (libs.accompanist.permissions)

    implementation (libs.androidx.lifecycle.service)

    //ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)


    implementation(libs.kotlinx.serialization.json)



}
