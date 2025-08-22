plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)


}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation("androidx.compose.ui:ui:1.8.3")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.i18n)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.compose.material:material-icons-extended:1.2.0")
//room dependecies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
//converter dependencies
    implementation("com.google.code.gson:gson:2.13.1")

//data store dependencies
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    // optional -RxJava2 support
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.1.7")
    // optional -RxJava3 support
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.1.7")

    implementation("io.insert-koin:koin-androidx-compose:4.0.2")

//Serialization
    implementation(libs.navigation.compose)
    implementation(libs.ktor.serialization.kotlinx.json)

    //data picker
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))

    //fotocamera async
    implementation("io.coil-kt:coil-compose:2.3.0")

    //gps
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)

    //Camerax
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.camerax.extensions)
    implementation("com.google.code.gson:gson:2.13.1")


    //Slide
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")
}
