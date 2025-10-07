import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.telepesa"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.telepesa.customer"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Load API configuration from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        val apiBaseUrl = localProperties.getProperty("API_BASE_URL")
            ?: project.findProperty("API_BASE_URL") as String?
            ?: System.getenv("API_BASE_URL")
            ?: "http://localhost:8082/api/v1"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xsuppress-version-warnings")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }

    // Dynamic Feature Modules
    dynamicFeatures += setOf(
        ":feature:onboarding",
        ":feature:auth",
        ":feature:home",
    )
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:security"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.androidx.lifeycle)
    implementation(libs.androidx.splash.screen)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.compose.ui)
    implementation(libs.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)

    // Material Design
    implementation(libs.material)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Networking
    implementation(libs.bundles.networking)

    // Database
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Security
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)

    // Location Services
    implementation(libs.bundles.location)

    // Logging
    implementation(libs.timber)
    implementation(libs.chucker.no.op)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Force Compose Compiler version
    implementation("androidx.compose.compiler:compiler:1.5.0")
    constraints {
        implementation("androidx.compose.compiler:compiler:1.5.0") {
            because("Force Compose Compiler version to be compatible with Kotlin 1.9.0")
        }
    }

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.tooling)
}

configurations.all {
    // Exclude conflicting annotation libraries
    exclude(group = "com.intellij", module = "annotations")
}
