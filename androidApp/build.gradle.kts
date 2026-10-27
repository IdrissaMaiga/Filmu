plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.iditechs.filmu.android"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.iditechs.filmu"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidCompileSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    val releaseSecrets = rootProject.file("secrets/android-release.env").takeIf { it.exists() }?.readLines()
        ?.mapNotNull { line -> line.split("=", limit = 2).takeIf { it.size == 2 }?.let { it[0].trim() to it[1].trim() } }
        ?.toMap().orEmpty()
    fun releaseSecret(name: String) = System.getenv(name) ?: releaseSecrets[name]
    val keystorePath = releaseSecret("FILMU_KEYSTORE_FILE")
    if (keystorePath != null) {
        signingConfigs.create("release") {
            storeFile = rootProject.file(keystorePath)
            storePassword = releaseSecret("FILMU_KEYSTORE_PASSWORD")
            keyAlias = releaseSecret("FILMU_KEY_ALIAS")
            keyPassword = releaseSecret("FILMU_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":sharedLogic"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.core.ktx)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.hls)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.navigation.compose)
    implementation(libs.datastore.preferences)
    debugImplementation(libs.compose.ui.tooling)
}
