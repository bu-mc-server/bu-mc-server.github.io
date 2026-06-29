plugins {
    kotlin("jvm") version "2.3.21"
    application
}

group = "com.github.bu-mc-server"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.github.bu-mc-server.MainKt")
}