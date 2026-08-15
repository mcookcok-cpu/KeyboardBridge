plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

import org.gradle.api.tasks.Exec

val remoteDir = layout.buildDirectory.dir("androidtvremote-src")

val syncRemoteLib = tasks.register<Exec>("syncRemoteLib") {
    val out = remoteDir.get().asFile
    outputs.dir(out)
    doFirst {
        if (out.exists()) out.deleteRecursively()
        out.parentFile.mkdirs()
    }
    commandLine("git", "clone", "--depth", "1", "https://github.com/kunal52/AndroidTvRemote.git", out.absolutePath)
    doLast {
        val src = out.resolve("src/main/java")
        val target = project.file("src/main/java")
        src.copyRecursively(target, overwrite = true)
        project.file("src/override").copyRecursively(target, overwrite = true)
        // The upstream demo context writes its keystore relative to the JVM working directory.
        // The app replaces that context at runtime with a private Android files directory.
        project.file("src/main/java/com/kunal52/AndroidRemoteTv.java").delete()
        project.file("src/main/java/com/kunal52/Main.java").delete()
        project.file("src/main/java/com/kunal52/AndroidTvListener.java").delete()
    }
}

tasks.named("preBuild") { dependsOn(syncRemoteLib) }

android {
    namespace = "com.julianto.keyboardbridge"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.julianto.keyboardbridge"
        minSdk = 28
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.protobuf:protobuf-java:3.25.5")
    implementation("org.bouncycastle:bcprov-jdk18on:1.70")
    implementation("org.slf4j:slf4j-api:1.7.36")
}

dependencies {
    implementation("org.slf4j:slf4j-android:1.7.36")
}
