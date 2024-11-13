buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4") // Add this line for the Android Gradle Plugin
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.huawei.agconnect:agcp:1.5.2.300")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

