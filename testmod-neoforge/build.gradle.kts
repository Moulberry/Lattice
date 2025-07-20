plugins {
    id("earth.terrarium.cloche") version "0.11.6"
}

version = "1.0.0"
group = "com.moulberry.lattice"

repositories {
    cloche.librariesMinecraft()

    mavenCentral()

    cloche {
        main()
        mavenNeoforgedMeta()
        mavenNeoforged()
        mavenForge()
    }
}

dependencies {
    compileOnly(project(":"))
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

    fun createNeoforge(version: String, neoforgeVersion: String) {
        neoforge("neoforge:${version}") {
            minecraftVersion = version
            loaderVersion = neoforgeVersion

            runs {
                client()
            }

            dependencies {
                // note: need to put lattice-neoforge.jar in mods folder... can't figure out a way to properly include
                // it as jarjar due to missing coordinates
                compileOnly(rootProject.tasks.named<Jar>("buildMergedForgelike").get().outputs.files)
            }
        }
    }

//    createNeoforge("1.20.2", "20.2.93")
//    createNeoforge("1.20.4", "20.4.248")
//    createNeoforge("1.20.6", "20.6.136")
//    createNeoforge("1.21.1", "21.1.193")
//    createNeoforge("1.21.3", "21.3.86")
//    createNeoforge("1.21.4", "21.4.147")
//    createNeoforge("1.21.5", "21.5.87")
    createNeoforge("1.21.6", "21.6.20-beta")
}

configurations.all {
    resolutionStrategy.capabilitiesResolution {
        withCapability("cpw.mods:modlauncher") {
            selectHighestVersion()
        }
    }
}
