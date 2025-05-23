plugins {
    id("org.jetbrains.intellij") version "1.17.4"
    java
    kotlin("jvm") version "2.1.21"
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
}

group = "io.dragnea"
version = "0.4.2"

intellij {
    version.set("IC-2023.1.1")

    plugins.set(
        listOf(
            "PythonCore:231.8770.65",
            "org.intellij.scala:2023.1.16",
            "com.intellij.java",
            "org.jetbrains.kotlin",
            "org.rust.lang:0.4.193.5352-231",
        ),
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
