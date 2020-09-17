import Versions.kotlinVersion
import Versions.ktxLiveData
import Versions.sdkCompileVersion
import Versions.sdkMinVersion
import Versions.sdkTargetVersion

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
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

    implementation(project(":owncloudDomain"))
    implementation(project(":owncloud-android-library:owncloudComLibrary"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("androidx.lifecycle:lifecycle-livedata:${ktxLiveData}")
}
