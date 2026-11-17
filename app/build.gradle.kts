import java.util.Properties
import java.io.File
import java.net.URI
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class ValidatePlayStoreBundleConfiguration : DefaultTask() {
    @get:Input abstract val apiBaseUrlInput: Property<String>
    @get:Input abstract val googleWebClientIdInput: Property<String>
    @get:Input abstract val mapsApiKeyInput: Property<String>
    @get:Input abstract val privacyPolicyUrlInput: Property<String>
    @get:Input abstract val signingValuesInput: ListProperty<String>
    @get:Input abstract val releaseReadyInput: Property<String>

    @TaskAction
    fun validate() {
        val missing = buildList {
            if (!apiBaseUrlInput.get().startsWith("https://")) add("API_BASE_URL (must use HTTPS)")
            if (googleWebClientIdInput.get().isBlank()) add("GOOGLE_WEB_CLIENT_ID")
            if (mapsApiKeyInput.get().isBlank()) add("MAPS_API_KEY")
            if (!privacyPolicyUrlInput.get().startsWith("https://")) add("PRIVACY_POLICY_URL (must use HTTPS)")
            if (signingValuesInput.get().any(String::isBlank)) add("all UPLOAD_* signing properties")
            if (!releaseReadyInput.get().equals("true", ignoreCase = true)) {
                add("PLAY_RELEASE_READY=true after production data and policy review")
            }
        }
        check(missing.isEmpty()) {
            "Play Store bundle configuration is incomplete. Set: ${missing.joinToString()}"
        }
    }
}

abstract class ConfigureLocalBackendForwarding : DefaultTask() {
    @get:Input abstract val adbExecutable: Property<String>
    @get:Input abstract val backendPort: Property<Int>

    @TaskAction
    fun configureForwarding() {
        val adb = adbExecutable.get()
        val port = backendPort.get()
        val deviceOutput = runAdb(adb, "devices")
        val onlineDevices = deviceOutput.lineSequence()
            .drop(1)
            .map { it.trim().split(Regex("\\s+")) }
            .filter { it.size >= 2 && it[1] == "device" }
            .map { it[0] }
            .toList()

        if (onlineDevices.isEmpty()) {
            logger.warn("No online Android devices found; local backend port forwarding was not configured.")
            return
        }

        onlineDevices.forEach { serial ->
            runAdb(adb, "-s", serial, "reverse", "tcp:$port", "tcp:$port")
        }
        logger.lifecycle("ADB forwarding is ready for localhost:$port on ${onlineDevices.size} connected device(s).")
    }

    private fun runAdb(adb: String, vararg arguments: String): String {
        val process = try {
            ProcessBuilder(listOf(adb) + arguments)
                .redirectErrorStream(true)
                .start()
        } catch (error: Exception) {
            throw GradleException("Could not start ADB. Check the Android SDK platform-tools installation.", error)
        }
        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException("ADB port forwarding failed. Check that the selected Android device is online. $output")
        }
        return output
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)

    // Firebase Google Services plugin
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}
val courtFinderApiBaseUrl = localProperties.getProperty("API_BASE_URL", "")
val localBackendUri = runCatching { URI(courtFinderApiBaseUrl) }.getOrNull()
val localBackendPort = localBackendUri?.takeIf {
    it.scheme == "http" && (it.host == "127.0.0.1" || it.host == "localhost")
}?.port
val googleWebClientId = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")
val privacyPolicyUrl = localProperties.getProperty("PRIVACY_POLICY_URL", "")
val uploadStoreFile = localProperties.getProperty("UPLOAD_STORE_FILE", "")
val uploadStorePassword = localProperties.getProperty("UPLOAD_STORE_PASSWORD", "")
val uploadKeyAlias = localProperties.getProperty("UPLOAD_KEY_ALIAS", "")
val uploadKeyPassword = localProperties.getProperty("UPLOAD_KEY_PASSWORD", "")
val releaseReady = localProperties.getProperty("PLAY_RELEASE_READY", "false")
val signingValues = listOf(uploadStoreFile, uploadStorePassword, uploadKeyAlias, uploadKeyPassword)
require(signingValues.all(String::isBlank) || signingValues.all(String::isNotBlank)) {
    "Set all four UPLOAD_* signing properties in local.properties, or leave all four blank."
}

android {
    namespace = "com.example.myapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.yaswanth756.courtfinder"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")
        buildConfigField("String", "COURTFINDER_API_BASE_URL", "\"${courtFinderApiBaseUrl}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${googleWebClientId}\"")
        buildConfigField("String", "COURTFINDER_PRIVACY_POLICY_URL", "\"${privacyPolicyUrl}\"")
    }

    buildTypes {
        release {
            if (signingValues.all(String::isNotBlank)) {
                signingConfig = signingConfigs.create("playUpload").apply {
                    storeFile = rootProject.file(uploadStoreFile)
                    storePassword = uploadStorePassword
                    keyAlias = uploadKeyAlias
                    keyPassword = uploadKeyPassword
                }
            }
            optimization {
                enable = false
            }
            manifestPlaceholders["API_USES_CLEARTEXT"] = "false"
        }
        debug {
            manifestPlaceholders["API_USES_CLEARTEXT"] = "true"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

tasks.register<ValidatePlayStoreBundleConfiguration>("validatePlayStoreBundleConfiguration") {
    apiBaseUrlInput.set(courtFinderApiBaseUrl)
    googleWebClientIdInput.set(googleWebClientId)
    mapsApiKeyInput.set(localProperties.getProperty("MAPS_API_KEY", ""))
    privacyPolicyUrlInput.set(privacyPolicyUrl)
    signingValuesInput.set(signingValues)
    releaseReadyInput.set(releaseReady)
}

if (localBackendPort != null) {
    val sdkDirectory = localProperties.getProperty("sdk.dir")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: System.getenv("ANDROID_HOME")
    val adbName = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) "adb.exe" else "adb"
    val adbPath = sdkDirectory?.let { File(it, "platform-tools/$adbName").absolutePath } ?: adbName
    val localPortForwarding = tasks.register<ConfigureLocalBackendForwarding>("configureLocalBackendForwarding") {
        adbExecutable.set(adbPath)
        backendPort.set(localBackendPort)
    }

    tasks.configureEach {
        if (name == "installDebug") dependsOn(localPortForwarding)
    }
}

tasks.configureEach {
    if (name == "bundleRelease") {
        dependsOn("validatePlayStoreBundleConfiguration")
    }
}

dependencies {
    // Firebase Bill of Materials
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-core")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.googleid)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
