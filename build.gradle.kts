plugins {
    kotlin("jvm") version "2.1.10"

    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}


group = "com.beauver.minecraft.plugins"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.aikar.co/content/groups/aikar/"){
        name = "aikar"
    }
}

extra["vaadinVersion"] = "24.6.4"
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.nanohttpd:nanohttpd:2.3.1")
}


val targetJavaVersion = 21
kotlin {
    this.jvmToolchain(targetJavaVersion)
}

tasks.runServer {
    minecraftVersion("1.21.4")
}


tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}


