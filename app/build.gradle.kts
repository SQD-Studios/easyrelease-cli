plugins {
    application
}

version = "1.1.1"
group = "com.sqdstudios.easyrelease.cli"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jackson.databind)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "com.sqdstudios.easyrelease.cli.Main"
    applicationName = "easyrelease"
}

tasks {
    test {
        useJUnitPlatform()
    }
}