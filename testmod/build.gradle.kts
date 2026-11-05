plugins {
    id("earth.terrarium.cloche") version "0.18.8+beta-2"
}

version = "1.0.0"
group = "com.moulberry.lattice"

repositories {
    cloche.librariesMinecraft()

    mavenCentral()

    cloche {
        main()
        mavenFabric()
        mavenNeoforged()
        mavenNeoforgedMeta()
    }
}

dependencies {
    compileOnly(project(":"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

cloche {
    metadata {
        modId = "lattice_testmod"
        name = "Lattice Testmod"
        license = "MIT"
    }

    mappings {
        official()
    }

    fun createFabric(version: String, apiVersion: String) {
        fabric("fabric:${version}") {
            minecraftVersion = version
            loaderVersion = "0.19.3"

            includedClient()

            runs {
                client()
            }

            dependencies {
                fabricApi(apiVersion, version.split("-")[0])
                implementation(rootProject.tasks.named<Jar>("buildMerged").get().outputs.files)
                include(rootProject.tasks.named<Jar>("buildMerged").get().outputs.files)
            }

            metadata {
                entrypoint("main") {
                    value = "com.moulberry.lattice.testmod.FabricLatticeTestMod"
                }
            }
        }
    }

    fun createNeoforge(version: String, neoforgeVersion: String) {
        neoforge("neoforge:${version}") {
            minecraftVersion = version
            loaderVersion = neoforgeVersion

            runs {
                client()
            }

            dependencies {
                implementation(rootProject.tasks.named<Jar>("buildMerged").get().outputs.files)
                include(rootProject.tasks.named<Jar>("buildMerged").get().outputs.files)
            }

            metadata {
                modLoader = "javafml"
                loaderVersion {
                    start = "1"
                    startInclusive = true
                }
            }
        }
    }

//    createFabric("26.1", "0.144.0")
    //createNeoforge("26.2", "26.2.0.93")
    createFabric("26.2", "0.153.0")
}
