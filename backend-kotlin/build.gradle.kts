plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    application
}

// Keep backend build/test output out of the OneDrive checkout, where Windows
// can lock Gradle's replaceable test-result directories.
val localBuildRoot = System.getenv("LOCALAPPDATA") ?: System.getProperty("java.io.tmpdir")
val backendBuildId = projectDir.canonicalFile.absolutePath.hashCode().toUInt().toString(16)
if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) {
    layout.buildDirectory.set(file("$localBuildRoot/CourtFinderBuild/backend-$backendBuildId"))
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:3.6.1")
    implementation("io.ktor:ktor-server-netty-jvm:3.6.1")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.6.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.6.1")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:3.6.1")
    implementation("io.ktor:ktor-server-status-pages-jvm:3.6.1")
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")
    implementation("org.postgresql:postgresql:42.7.14")
    implementation("com.auth0:java-jwt:4.6.1")
    implementation("com.google.api-client:google-api-client:2.9.0")
    implementation("com.google.firebase:firebase-admin:9.11.0")
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("com.example.courtfinder.backend.ApplicationKt")
}

kotlin {
    jvmToolchain(23)
}
