import Versions.archLifecycleVersion
import Versions.junitVersion
import Versions.kotlinVersion
import Versions.mockkVersion
import Versions.sdkCompileVersion
import Versions.sdkMinVersion
import Versions.sdkTargetVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("kotlin-allopen")
}

android {
    compileSdkVersion(sdkCompileVersion)

    defaultConfig {
        minSdkVersion(sdkMinVersion)
        targetSdkVersion(sdkTargetVersion)

    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

}

dependencies {
    implementation(fileTree("libs") { include(listOf("*.jar")) })

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${archLifecycleVersion}")

    // Dependencies for unit tests
    testImplementation(project(":owncloudTestUtil"))
    testImplementation("junit:junit:$junitVersion")
    testImplementation("androidx.arch.core:core-testing:$archLifecycleVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")

}
