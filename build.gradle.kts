import earth.terrarium.cloche.api.target.CommonTarget
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost
import earth.terrarium.cloche.api.attributes.TargetAttributes

plugins {
    id("earth.terrarium.cloche") version "0.13.6"
    id("com.vanniktech.maven.publish") version("0.28.0") // `maven-publish` doesn't support new maven central
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

var fabricJarOutputs = mutableListOf<Provider<out Jar>>()
var forgeLikeJarOutputs = mutableListOf<Provider<out Jar>>()

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
        }

        mixins.from("src/1.20.1/main/mixins/lattice1201.mixins.json")
        mixins.from("src/1.20.2/main/mixins/lattice1202.mixins.json")
        mixins.from("src/1.20.4/main/mixins/lattice1204.mixins.json")
        mixins.from("src/1.20.6/main/mixins/lattice1206.mixins.json")
        mixins.from("src/1.21.6/main/mixins/lattice1216.mixins.json")
        mixins.from("src/1.21.9/main/mixins/lattice1219.mixins.json")
    }

    val commonMinecraftVersion: Attribute<String> = Attribute.of("com.moulberry.commonMinecraftVersion", String::class.java)

    fun createCommon(version: String): CommonTarget {
        return common(version) {
            attributes {
                attribute(commonMinecraftVersion, version)
            }
        }
    }

    fun createFabric(commonTarget: CommonTarget, version: String) {
        fabric("fabric:${version}") {
            minecraftVersion = version
            loaderVersion = "0.17.2"

            dependsOn(commonTarget)

            includedClient()

            fabricJarOutputs.add(finalJar)
        }
    }

    fun createForge(commonTarget: CommonTarget, version: String, forgeVersion: String) {
        forge("forge:${version}") {
            minecraftVersion = version
            loaderVersion = forgeVersion

            dependsOn(commonTarget)

            forgeLikeJarOutputs.add(finalJar)
        }
    }

    fun createNeoforge(commonTarget: CommonTarget, version: String, neoforgeVersion: String) {
        neoforge("neoforge:${version}") {
            minecraftVersion = version
            loaderVersion = neoforgeVersion

            dependsOn(commonTarget)

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
    createAll("1.21.9", null, null)
}

tasks.register<Jar>("buildMergedFabric") {
    archiveBaseName.set("lattice-fabric")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    fabricJarOutputs.forEach { jarProvider ->
        from(project.zipTree(jarProvider.get().archiveFile))
    }

    manifest {
        attributes["Fabric-Loom-Mixin-Remap-Type"] = "static"
        attributes["Fabric-Jar-Type"] = "classes"
        attributes["Fabric-Mapping-Namespace"] = "intermediary"
    }
}

tasks.register<Jar>("buildMergedForgelike") {
    archiveBaseName.set("lattice-forgelike")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    forgeLikeJarOutputs.forEach { jarProvider ->
        from(project.zipTree(jarProvider.get().archiveFile))
    }
}

configurations.all {
    resolutionStrategy.capabilitiesResolution {
        withCapability("cpw.mods:modlauncher") {
            selectHighestVersion()
        }
    }
}

fun baseFabricConfiguration(configuration: Configuration) {
    configuration.outgoing.capability("com.moulberry:lattice:${rootProject.version}")
    configuration.outgoing.artifact(tasks.named<Jar>("buildMergedFabric").get())

    configuration.attributes.attribute(TargetAttributes.MOD_LOADER, "fabric")
    configuration.attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
    configuration.attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    configuration.attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
    configuration.attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
}

var fabricConfigurationApi = configurations.create("fabricApi") {
    baseFabricConfiguration(this)
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
}

var fabricConfigurationRuntime = configurations.create("fabricRuntime") {
    baseFabricConfiguration(this)
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
}

val javaComponent = components.findByName("java") as AdhocComponentWithVariants

// Hack to remove all cloche variants from publication
rootProject.configurations.forEach {
    try {
        javaComponent.withVariantsFromConfiguration(it) {
            skip()
        }
    } catch (ignored: Exception) {}
}

javaComponent.addVariantsFromConfiguration(fabricConfigurationApi) {
    mapToMavenScope("runtime")
    mapToOptional()
}
javaComponent.addVariantsFromConfiguration(fabricConfigurationRuntime) {
    mapToMavenScope("runtime")
    mapToOptional()
}

mavenPublishing {
    configure(JavaLibrary(JavadocJar.Javadoc(), true))

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("com.moulberry", "lattice", version.toString())

    pom {
        name = "Lattice"
        description = "Library for creating Minecraft configuration GUIs"
        url = "https://github.com/Moulberry/Lattice"
        inceptionYear = "2025"
        packaging = "jar"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit"
            }
        }

        developers {
            developer {
                name = "Moulberry"
                url = "https://github.com/Moulberry"
            }
        }

        issueManagement {
            system = "GitHub"
            url = "https://github.com/Moulberry/Lattice/issues"
        }

        scm {
            url = "https://github.com/Moulberry/Lattice/"
            connection = "scm:git:git://github.com/Moulberry/Lattice.git"
            developerConnection = "scm:git:ssh://git@github.com/Moulberry/Lattice.git"
        }
    }
}
