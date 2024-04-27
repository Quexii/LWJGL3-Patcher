plugins {
    kotlin("jvm") version "1.9.23"
}

group = "eu.shoroa"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
    implementation("commons-io:commons-io:2.16.0")
}

kotlin {
    jvmToolchain(17)
}