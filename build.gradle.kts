// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false

    // Firebase Google Services plugin
    id("com.google.gms.google-services") version "4.5.0" apply false
}

val localAppData = System.getenv("LOCALAPPDATA") ?: System.getProperty("java.io.tmpdir")
// Keep KSP/Gradle outputs outside OneDrive, and isolate each checkout so temporary
// verification copies cannot reuse incremental caches from the Android Studio project.
val checkoutBuildId = rootProject.projectDir.canonicalFile.absolutePath.hashCode().toUInt().toString(16)
val externalBuildDir = file("$localAppData/CourtFinderBuild/$checkoutBuildId")

allprojects {
    layout.buildDirectory.set(externalBuildDir.resolve(if (path == ":") "root" else name))
}
