import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.googleKotlinKsp)
}

var extraVersionName = ""
val extraVersionNameFile = file("extra_version.txt")
if (extraVersionNameFile.exists()) {
    extraVersionName = extraVersionNameFile.readText()
}

val signingProperties = Properties()
val signingPropertiesFile = file("key.properties")
if (signingPropertiesFile.exists()) {
    signingProperties.load(signingPropertiesFile.inputStream())
}

val signingKeyStoreFile = file("key.jks")

val canSign =
    signingKeyStoreFile.exists() && signingProperties["alias"] != null && signingProperties["password1"] != null && signingProperties["password2"] != null

android {
    namespace = "xyz.xfqlittlefan.winnitodo"
    compileSdk = 34

    defaultConfig {
        applicationId = "xyz.xfqlittlefan.winnitodo"
        minSdk = 27
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.0${extraVersionName}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            if (canSign) {
                signingConfig = signingConfigs.create("release") {
                    storeFile = signingKeyStoreFile
                    storePassword = signingProperties["password1"] as String
                    keyAlias = signingProperties["alias"] as String
                    keyPassword = signingProperties["password2"] as String
                }
            }
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
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.animation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
