plugins {
    application
}

version = "1.0.0"
group = "com.sqdstudios.easyrelease.cli"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)

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