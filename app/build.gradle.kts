plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.lkms"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lkms"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

configurations.all {
    exclude(group = "org.jetbrains", module = "annotations-java5")
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.annotation)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.splashscreen)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    implementation("com.github.bumptech.glide:glide:5.0.0-rc01")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.0-rc01")

    implementation("io.noties.markwon:core:4.6.2")
    implementation("jp.wasabeef:richeditor-android:2.0.0")

    // https://mvnrepository.com/artifact/com.google.apis/google-api-services-drive
    implementation("com.google.apis:google-api-services-drive:v3-rev20250511-2.0.0")

    // https://mvnrepository.com/artifact/com.google.api-client/google-api-client-android
    implementation("com.google.api-client:google-api-client-android:1.23.0")

    // https://mvnrepository.com/artifact/com.google.http-client/google-http-client-gson
    implementation("com.google.http-client:google-http-client-gson:1.23.0")



    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")

    implementation("com.google.android.gms:play-services-drive:17.0.0")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
}
