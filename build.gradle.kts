plugins {
    id("earth.terrarium.cloche") version "0.11.4"
}

version = project.version
group = project.group

base {
    archivesName.set(project.property("archives_base_name") as String)
}

repositories {
    cloche.librariesMinecraft()

    mavenCentral()

    cloche {
        main()
        mavenFabric()
        mavenForge()
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")
}

var fabricJarOutputs = mutableListOf<Provider<RegularFile>>()

cloche {
    metadata {
        modId = "lattice"
        name = "Lattice"
        description = "Library for creating configuration GUIs"
        license = "MIT"
        icon = "assets/lattice/icon.png"
        author("Moulberry")

        clientOnly = true
        custom("modmenu" to mapOf("badges" to listOf("library")))
    }

    mappings {
        official()
    }

    common {
        dependencies {
            compileOnly("org.jetbrains:annotations:23.0.0")
            implementation("com.moulberry:mixinconstraints:1.0.9") {
                exclude(group = "org.slf4j")
            }
        }

        mixins.from("src/fabric/1.20.1/main/mixins/lattice1201.mixins.json")
        mixins.from("src/fabric/1.20.2/main/mixins/lattice1202.mixins.json")
        mixins.from("src/fabric/1.20.4/main/mixins/lattice1204.mixins.json")
        mixins.from("src/fabric/1.20.6/main/mixins/lattice1206.mixins.json")
        mixins.from("src/fabric/1.21.6/main/mixins/lattice1216.mixins.json")
    }

    fun createFabric(version: String) {
        fabric("fabric:${version}") {
            minecraftVersion = version
            loaderVersion = "0.16.14"

            includedClient()

            dependencies {
                implementation("com.moulberry:mixinconstraints:1.0.9") {
                    exclude(group = "org.slf4j")
                }
                include("com.moulberry:mixinconstraints:1.0.9")
            }

            fabricJarOutputs.add(finalJar)
        }
    }

    createFabric("1.20.1")
    createFabric("1.20.2")
    createFabric("1.20.4")
    createFabric("1.20.6")
    createFabric("1.21.1")
    createFabric("1.21.3")
    createFabric("1.21.4")
    createFabric("1.21.5")
    createFabric("1.21.6")
}

tasks.register<Jar>("mergedFabric") {
    archiveBaseName.set("merged-fabric-jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    fabricJarOutputs.forEach { jarProvider ->
        from(project.zipTree(jarProvider))
    }
}