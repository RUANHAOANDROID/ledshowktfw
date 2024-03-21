val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val log4j_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
    kotlin("plugin.serialization") version "1.5.10"
}

group = "com.uchi"
version = "100117"

application {
    mainClass.set("com.uchi.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
//    implementation("org.apache.logging.log4j:log4j-core:$log4j_version")
//    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation ("joda-time:joda-time:2.10.14")
    implementation("org.jetbrains.exposed:exposed-core:0.34.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.34.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.34.1")
    implementation("io.ktor:ktor-client-core:1.6.7")
    implementation("io.ktor:ktor-client-cio:1.6.7")
    implementation("io.ktor:ktor-client-json:1.6.7")
    implementation("org.xerial:sqlite-jdbc:3.36.0.2")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
