import earth.terrarium.cloche.api.target.CommonTarget

plugins {
    id("earth.terrarium.cloche") version "0.11.6"
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
        mavenNeoforgedMeta()
        mavenNeoforged()
        mavenForge()
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")
}

var fabricJarOutputs = mutableListOf<Provider<RegularFile>>()
var forgeLikeJarOutputs = mutableListOf<Provider<RegularFile>>()

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
    }

    val commonMinecraftVersion: Attribute<String> = Attribute.of("com.moulberry.commonMinecraftVersion", String::class.java)

    fun createCommon(version: String): CommonTarget {
        return common(version) {
            attributes {
                attribute(commonMinecraftVersion, version)
            }

            dependencies {
                implementation("com.moulberry:mixinconstraints:1.0.9") {
                    exclude(group = "org.slf4j")
                }
            }

            mixins.from("src/${version}/main/mixins/lattice${version.replace(".", "")}.mixins.json")
        }
    }

    fun createFabric(commonTarget: CommonTarget, version: String) {
        fabric("fabric:${version}") {
            minecraftVersion = version
            loaderVersion = "0.16.14"

            dependsOn(commonTarget)

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

    fun createForge(commonTarget: CommonTarget, version: String, forgeVersion: String) {
        forge("forge:${version}") {
            minecraftVersion = version
            loaderVersion = forgeVersion

            dependsOn(commonTarget)

            dependencies {
                implementation("com.moulberry:mixinconstraints:1.0.9") {
                    exclude(group = "org.slf4j")
                }
                include("com.moulberry:mixinconstraints:1.0.9")
            }

            forgeLikeJarOutputs.add(finalJar)
        }
    }

    fun createNeoforge(commonTarget: CommonTarget, version: String, neoforgeVersion: String) {
        neoforge("neoforge:${version}") {
            minecraftVersion = version
            loaderVersion = neoforgeVersion

            dependsOn(commonTarget)

            dependencies {
                implementation("com.moulberry:mixinconstraints:1.0.9") {
                    exclude(group = "org.slf4j")
                }
                include("com.moulberry:mixinconstraints:1.0.9")
            }

            forgeLikeJarOutputs.add(finalJar)
        }
    }

    fun createAll(version: String, forgeVersion: String?, neoforgeVersion: String?) {
        val common = createCommon(version)
        createFabric(common, version)
        if (forgeVersion != null) {
            createForge(common, version, forgeVersion)
        }
        if (neoforgeVersion != null) {
            createNeoforge(common, version, neoforgeVersion)
        }
    }

    // todo: forge 1.20.6+ has an issue with mappings
    // todo: neoforge 1.20.2 has an issue with dependency resolution
    // todo: neoforge 1.20.1 doesn't exist... maybe can workaround
    createAll("1.20.1", null, null)
    createAll("1.20.2", null, null)//"20.2.93")
    createAll("1.20.4", null, null)//"20.4.248")
    createAll("1.20.6", null, null)//"20.6.136")
    createAll("1.21.1", null, null)//"21.1.193")
    createAll("1.21.3", null, null)//"21.3.86")
    createAll("1.21.4", null, null)//"21.4.147")
    createAll("1.21.5", null, null)//"21.5.87")
    createAll("1.21.6", null, null)//"21.6.20-beta")
}

tasks.register<Jar>("buildMergedFabric") {
    archiveBaseName.set("lattice-fabric")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    fabricJarOutputs.forEach { jarProvider ->
        from(project.zipTree(jarProvider))
    }
}

tasks.register<Jar>("buildMergedForgelike") {
    archiveBaseName.set("lattice-forgelike")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    forgeLikeJarOutputs.forEach { jarProvider ->
        from(project.zipTree(jarProvider))
    }
}

configurations.all {
    resolutionStrategy.capabilitiesResolution {
        withCapability("cpw.mods:modlauncher") {
            selectHighestVersion()
        }
    }
}
