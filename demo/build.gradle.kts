import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.application)
}

java {
    toolchain {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    jvmToolchain {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(17)
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                val rootDirPath = project.rootDir.path
                val projectDirPath = project.projectDir.path
                outputPath = File("$rootDirPath/docs/demo")
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }

            }
        }
        binaries.executable()
    }

    jvm("desktop")

    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.foundation.ExperimentalFoundationApi")
            languageSettings.optIn("androidx.compose.ui.ExperimentalComposeUiApi")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(project(":core"))
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")
            implementation(compose.material3)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs) {
                    exclude("org.jetbrains.compose.material")
                    exclude("org.jetbrains.compose.material3")
                }
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.material3)
                implementation("androidx.activity:activity-compose:1.10.1")
            }
        }
    }
}

compose.experimental {
    web.application {}
}

compose.desktop {
    application {
        mainClass = "com.composeunstyled.demo.MainKt"
    }
}

android {
    namespace = "com.composeunstyled.demo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()
        applicationId = "com.composeunstyled.demo"
        versionCode = 1
        versionName = "1.0.0"
    }
}