plugins {
    id("java")
}

group = "io.github.unjoinable"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom-snapshots:1_21_6-a40d7115d4")
    implementation("ch.qos.logback:logback-classic:1.5.18")
}
