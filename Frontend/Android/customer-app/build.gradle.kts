buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
        classpath("com.google.android.gms:play-services-maps:19.0.0")
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.2")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.dagger.hilt) apply false
    alias(libs.plugins.diffplug.spotless) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ksp) apply false
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    // apply(plugin = "io.gitlab.arturbosch.detekt")
    
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("${layout.buildDirectory}/**/*.kt")
            ktlint("1.0.1")
                .editorConfigOverride(
                    mapOf(
                        "ktlint_function_naming" to "on",
                        "max_line_length" to "120"
                    )
                )
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }

        kotlinGradle {
            target("*.gradle.kts")
            ktlint("1.0.1")
                .editorConfigOverride(
                    mapOf(
                        "ktlint_function_naming" to "on",
                        "max_line_length" to "120"
                    )
                )
        }
    }

    afterEvaluate {
        if (tasks.findByName("preBuild") != null) {
            tasks.named("preBuild") {
                dependsOn("spotlessApply")
            }
        }
        
               // Suppress Kotlin version compatibility warnings
            // Suppress Kotlin version warnings
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                compilerOptions {
                    freeCompilerArgs.add("-Xsuppress-version-warnings")
                }
            }
    }
}