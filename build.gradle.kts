plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    // id("com.github.johnrengelman.shadow") version "8.1.1" // Disabled due to Java 21 compatibility issues
    id("com.diffplug.spotless") version "6.23.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "com.hallis21.armorsets"
version = "2.0.0"

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Coroutines for async operations (removed - causing dependency issues)
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Configuration serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Adventure API (included in Paper but explicit for clarity)
    compileOnly("net.kyori:adventure-api:4.16.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.16.0")

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.5")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.80.0")

    // Logging (for testing)
    testImplementation("ch.qos.logback:logback-classic:1.4.14")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

tasks {
    test {
        useJUnitPlatform()
    }

    // Create a fat JAR that includes all dependencies
    val fatJar = register<Jar>("fatJar") {
        archiveClassifier.set("")
        
        from(sourceSets.main.get().output)
        
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { 
                it.name.endsWith("jar") && !it.name.contains("paper-api") 
            }.map { zipTree(it) }
        })
        
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        
        // Exclude signature files that can cause issues
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    build {
        dependsOn(fatJar)
    }

    processResources {
        val props =
            mapOf(
                "version" to project.version,
                "group" to project.group,
                "name" to project.name,
            )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

// Plugin metadata via plugin-yml
bukkit {
    name = "ArmorSetEffects"
    version = project.version.toString()
    main = "com.hallis21.armorsets.ArmorSetsPlugin"
    apiVersion = "1.21"
    author = "hallis21"
    description = "Gives players effects when wearing specific armor sets"
    website = "https://github.com/hallis21/armorseteffects"

    permissions {
        register("armorseteffects.reload") {
            description = "Allows a player to reload the config file"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("armorseteffects.receive") {
            description = "Allows a player to receive armor set bonuses"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("armorseteffects.viewsets") {
            description = "Allows a player to view sets"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("armorseteffects.viewsets.all") {
            description = "Allows a player to view sets (Even hidden ones)"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("armorseteffects.viewsets.inspect") {
            description = "Allows a player to inspect non-hidden sets"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("armorseteffects.viewsets.inspect.all") {
            description = "Allows a player to inspect ALL sets"
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
    }

    commands {
        register("armor-reload") {
            description = "Reloads the armor-set config file"
            usage = "/armor-reload"
            permission = "armorseteffects.reload"
        }
        register("armor-list") {
            description = "Shows all non-hidden sets (Unless permitted)"
            usage = "/armor-list [armor set name]"
            permission = "armorseteffects.viewsets"
        }
        register("armor-gui") {
            description = "Opens the armor sets GUI"
            usage = "/armor-gui"
            permission = "armorseteffects.viewsets"
        }
    }

    softDepend = listOf("PlaceholderAPI")
}

// Code formatting with Spotless
spotless {
    kotlin {
        ktlint("1.0.1")
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        ktlint("1.0.1")
    }
}

// Custom tasks for development
tasks.register<Copy>("copyToTestServer") {
    dependsOn("fatJar")
    from("build/libs")
    include("*.jar")
    exclude("*-sources.jar")
    exclude("*-javadoc.jar") 
    into("test-server/plugins")
    doLast {
        println("Plugin copied to test server!")
    }
}

tasks.register<Exec>("downloadPaperServer") {
    group = "development"
    description = "Downloads Paper server for testing"

    doFirst {
        mkdir("test-server")
    }

    workingDir = file("test-server")
    commandLine(
        "curl",
        "-o",
        "paper-1.21.8.jar",
        "https://api.papermc.io/v2/projects/paper/versions/1.21.8/builds/latest/downloads/paper-1.21.8-latest.jar",
    )

    doLast {
        println("Paper server downloaded to test-server/")
        println("Run 'java -jar paper-1.21.8.jar' in the test-server directory to start")
    }
}

tasks.register("setupDev") {
    group = "development"
    description = "Sets up development environment"
    dependsOn("downloadPaperServer", "fatJar", "copyToTestServer")

    doLast {
        println("Development environment setup complete!")
        println("1. cd test-server")
        println("2. java -jar paper-1.21.8.jar")
        println("3. Accept EULA and configure as needed")
    }
}
