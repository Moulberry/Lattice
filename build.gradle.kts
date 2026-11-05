import earth.terrarium.cloche.api.target.CommonTarget
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost
import earth.terrarium.cloche.api.attributes.CommonTargetAttributes
import earth.terrarium.cloche.api.attributes.CompilationAttributes
import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.attributes.ModDistribution
import earth.terrarium.cloche.api.attributes.TargetAttributes

plugins {
    id("earth.terrarium.cloche") version "0.18.8+beta-2"
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

var jarOutputs = mutableListOf<Provider<out Jar>>()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

cloche {
    metadata {
        modId = "lattice"
        name = "Lattice"
        description = "Library for creating configuration GUIs"
        license = "MIT"
        icon = "assets/lattice/icon.png"
        author("Moulberry")

        custom("modmenu" to mapOf("badges" to listOf("library")))
    }

    mappings {
        official()
    }

    common {
        dependencies {
            compileOnly("org.jetbrains:annotations:23.0.0")
        }

        mixins.from("src/26.1/main/mixins/lattice261.mixins.json")
        mixins.from("src/26.2/main/mixins/lattice262.mixins.json")
    }

    val neoforgecommon = common("neoforgecommon")
    val fabriccommon = common("fabriccommon")

    val commonMinecraftVersion: Attribute<String> = Attribute.of("com.moulberry.commonMinecraftVersion", String::class.java)

    fun createCommon(version: String): CommonTarget {
        val rawVersion = version.split("-")[0]
        return common(rawVersion) {
            attributes {
                attribute(commonMinecraftVersion, rawVersion)
            }
        }
    }

    fun create(version: String, fabricLoader: String, neoforge: String) {
        val commonTarget = createCommon(version)

        val rawVersion = version.split("-")[0]
        fabric("fabric:${rawVersion}") {
            minecraftVersion = version
            loaderVersion = fabricLoader

            dependsOn(commonTarget, fabriccommon)

            includedClient()

            jarOutputs.add(finalJar)
        }

        neoforge("neoforge:${rawVersion}") {
            minecraftVersion = version
            loaderVersion = neoforge

            dependsOn(commonTarget, neoforgecommon)

            jarOutputs.add(finalJar)

            metadata {
                modLoader = "javafml"
                loaderVersion {
                    start = "1"
                    startInclusive = true
                }
            }
        }
    }

    //create("26.3", "0.19.5", "26.3.0.101")
    create("26.2", "0.19.3", "26.2.0.93")
    create("26.1", "0.18.4", "26.1.0.19-beta")
}

tasks.register<Jar>("buildMerged") {
    archiveBaseName.set("lattice")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    jarOutputs.forEach { jarProvider ->
        from(project.zipTree(jarProvider.get().archiveFile))
    }

    manifest {
        attributes["Fabric-Jar-Type"] = "classes"
    }
}

fun baseConfiguration(configuration: Configuration) {
    configuration.outgoing.capability("com.moulberry:lattice:${rootProject.version}")
    configuration.outgoing.artifact(tasks.named<Jar>("buildMerged").get())

    configuration.attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
    configuration.attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    configuration.attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
    configuration.attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
    configuration.attributes.attribute(CompilationAttributes.DISTRIBUTION, ModDistribution.client)
}

var latticeConfigurationApi = configurations.create("latticeApi") {
    baseConfiguration(this)
    attributes
        .attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
}

var latticeConfigurationRuntime = configurations.create("latticeRuntime") {
    baseConfiguration(this)
    attributes
        .attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
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

javaComponent.addVariantsFromConfiguration(latticeConfigurationApi) {
    mapToMavenScope("runtime")
    mapToOptional()
}
javaComponent.addVariantsFromConfiguration(latticeConfigurationRuntime) {
    mapToMavenScope("runtime")
    mapToOptional()
}

mavenPublishing {
    configure(JavaLibrary(JavadocJar.Javadoc(), true))

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    //signAllPublications()

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
