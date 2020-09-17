import Versions.androidTestVersion
import Versions.archLifecycleVersion
import Versions.extJunitVersion
import Versions.junitVersion
import Versions.kotlinVersion
import Versions.mockkVersion
import Versions.roomVersion
import Versions.sdkCompileVersion
import Versions.sdkMinVersion
import Versions.sdkTargetVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-allopen")
}

android {
    compileSdkVersion(sdkCompileVersion)

    defaultConfig {
        minSdkVersion(sdkMinVersion)
        targetSdkVersion(sdkTargetVersion)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room Database Tests
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }

        multiDexEnabled = true

        manifestPlaceholders = mapOf("appAuthRedirectScheme" to "")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


    sourceSets {
        map { it.java.srcDir("src/${it.name}/kotlin") }
        map { it.assets.srcDir("$projectDir/schemas") }
    }
}

dependencies {
    implementation(fileTree("libs") { include(listOf("*.jar")) })
    implementation(project(":owncloudDomain"))

    // Owncloud Android Library
    api(project(":owncloud-android-library:owncloudComLibrary"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Dependencies for unit tests
    testImplementation(project(":owncloudTestUtil"))
    testImplementation("junit:junit:$junitVersion")
    testImplementation("androidx.arch.core:core-testing:$archLifecycleVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")

    // Dependencies for instrumented tests
    androidTestImplementation(project(":owncloudTestUtil"))
    androidTestImplementation("androidx.test:runner:$androidTestVersion")
    androidTestImplementation("androidx.test.ext:junit:$extJunitVersion")
    androidTestImplementation("androidx.arch.core:core-testing:$archLifecycleVersion")
    androidTestImplementation("android.arch.persistence.room:testing:$roomVersion")
    androidTestImplementation("io.mockk:mockk-android:$mockkVersion") {
        exclude(module = "objenesis")
    }
}
