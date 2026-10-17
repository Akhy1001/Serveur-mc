plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.4")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
tasks.runServer {
    minecraftVersion("1.21.4")
}
tasks.register<Copy>("deployPlugin") {
    dependsOn(tasks.named("jar"))
    from(tasks.named<Jar>("jar").map { it.archiveFile })
    into(file("${System.getProperty("user.home")}/server-test/plugins"))
}

// Déclenche automatiquement le déploiement après chaque build
tasks.named("build") {
    finalizedBy("deployPlugin")
}
