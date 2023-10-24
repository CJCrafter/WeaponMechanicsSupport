group = "com.cjcrafter"
version = "1.0.0"

plugins {
    java
    kotlin("jvm") version "1.9.20-RC"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // https://mvnrepository.com/artifact/net.dv8tion/JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.13")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.cjcrafter:openai:1.3.2")
    implementation("com.cjcrafter:gitbook:1.0.0")

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

// Create javadocJar and sourcesJar tasks
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}