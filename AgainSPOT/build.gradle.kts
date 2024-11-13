buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4") // Add this line for the Android Gradle Plugin
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.huawei.agconnect:agcp:1.9.1.301") // updated

    }
}

plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
}

