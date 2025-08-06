import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("native.cocoapods")
}

group = "com.github.kevskitv"

version = "0.130.1"

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    cocoapods {
        version = project.version.toString()
        summary = "WebRTC Kotlin Multiplatform SDK"
        homepage = "https://github.com/shepeliev/webrtc-kmp"
        ios.deploymentTarget = "13.0"

        noPodspec()

        pod("WebRTC-SDK") {
            version = libs.versions.webrtc.ios.sdk.get()
            moduleName = "WebRTC"
            packageName = "WebRTC"
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        publishAllLibraryVariants()
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js {
        useCommonJs()
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.rootDir.path)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jsAndWasmJs") {
                withJs()
                withWasmJs()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.coroutines)
        }

        androidMain.dependencies {
            api(libs.webrtc.sdk)
            implementation(libs.kotlin.coroutines.android)
            implementation(libs.androidx.coreKtx)
            implementation(libs.androidx.startup)
        }

        jsMain.dependencies {
            implementation(npm("webrtc-adapter", "8.1.1"))
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlin.browser)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.kotlin.coroutines.test)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.test.rules)
        }

        val iosX64AndSimulatorArm64Main by creating {
            dependsOn(iosMain.get())
        }

        val iosX64Main by getting
        iosX64Main.dependsOn(iosX64AndSimulatorArm64Main)
        val iosSimulatorArm64Main by getting
        iosSimulatorArm64Main.dependsOn(iosX64AndSimulatorArm64Main)
    }
}

android {
    namespace = "com.shepeliev.webrtckmp"

    compileSdk = libs.versions.compileSdk.get().toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDir("src/androidMain/res")

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    testOptions {
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    dependencies {
        androidTestImplementation(libs.androidx.test.core)
        androidTestImplementation(libs.androidx.test.runner)
    }
}
