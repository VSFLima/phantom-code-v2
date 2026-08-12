plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val hasSigningEnv = System.getenv("KEYSTORE_BASE64") != null

android {
    namespace = "com.phantomcode.v2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.phantomcode.v2"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0-v2"
    }

    if (hasSigningEnv) {
        signingConfigs {
            create("release") {
                val ksFile = layout.buildDirectory.file("phantom-release.jks").get().asFile
                ksFile.writeBytes(java.util.Base64.getDecoder().decode(System.getenv("KEYSTORE_BASE64")))
                storeFile = ksFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS") ?: "phantom"
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasSigningEnv) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    packaging {
        jniLibs { useLegacyPackaging = true }
    }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.dsl.JvmTarget.JVM_17) }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    debugImplementation(libs.androidx.ui.tooling)
}
