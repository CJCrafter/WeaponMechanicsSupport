group = "com.cjcrafter"
version = "1.2.0"

plugins {
    java
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // https://mvnrepository.com/artifact/net.dv8tion/JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.20")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.cjcrafter:openai:2.1.0")
    implementation("com.cjcrafter:gitbook:1.0.0")
    implementation("com.h2database:h2:2.2.224")
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")

    implementation("ch.qos.logback:logback-classic:1.4.12")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

// Configure shadowJar task
tasks.shadowJar {
    archiveClassifier.set("shadow")

    manifest {
        attributes("Main-Class" to "com.cjcrafter.weaponmechanicssupport.Main")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.test {
    useJUnitPlatform()
}