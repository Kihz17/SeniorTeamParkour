import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.3.7-LOCAL-SNAPSHOT"
    id("net.minecrell.plugin-yml.bukkit") version "0.4.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.kihz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal { content { includeGroup("io.papermc.paper") } }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/")}
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    implementation("mysql:mysql-connector-java:5.1.6")

    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")

    paperDevBundle("1.18.2-R0.1-CUSTOM")
}

apply(plugin = "com.github.johnrengelman.shadow")

tasks {
    build {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

bukkit {
    name = "SeniorTeam"
    description = "SeniorTeam Plugin"
    version = "1.0"
    main = "com.kihz.Core"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    apiVersion = "1.18"
    authors = listOf("Kihz")
    depend = listOf("ProtocolLib")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}