plugins {
    id("org.jetbrains.intellij") version "1.13.0"
    java
    kotlin("jvm") version "1.8.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

group = "io.dragnea"
version = "0.4.0"

intellij {
    version.set("IC-2022.3.2")

    plugins.set(
        listOf(
            "PythonCore:223.8617.56",
            "org.intellij.scala:2022.3.15",
            "com.intellij.java",
            "org.jetbrains.kotlin",
            "org.rust.lang:0.4.187.5175-223"
        )
    )

    updateSinceUntilBuild.set(false)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            allWarningsAsErrors = true
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
            allWarningsAsErrors = true
        }
    }
    runIde {
        maxHeapSize = "4G"
    }
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

repositories {
    mavenCentral()
}
