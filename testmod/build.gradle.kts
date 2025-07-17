plugins {
    id("earth.terrarium.cloche") version "0.11.4"
}

version = "1.0.0"
group = "com.moulberry.lattice"

repositories {
    cloche.librariesMinecraft()

    mavenCentral()

    cloche {
        main()
        mavenFabric()
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

    fun createFabric(version: String, apiVersion: String) {
        fabric("fabric:${version}") {
            minecraftVersion = version
            loaderVersion = "0.16.14"

            includedClient()

            runs {
                client()
            }

            dependencies {
                fabricApi(apiVersion, version)
                implementation(rootProject.tasks.named<Jar>("mergedFabric").get().outputs.files)
                include(rootProject.tasks.named<Jar>("mergedFabric").get().outputs.files)
                implementation("com.moulberry:mixinconstraints:1.0.9")
                include("com.moulberry:mixinconstraints:1.0.9")
            }

            metadata {
                entrypoint("main") {
                    value = "com.moulberry.lattice.testmod.LatticeTestMod"
                }
            }
        }
    }

    createFabric("1.20.1", "0.92.6")
    createFabric("1.20.2", "0.89.0")
    createFabric("1.20.4", "0.97.3")
    createFabric("1.20.6", "0.100.8")
    createFabric("1.21.1", "0.116.4")
    createFabric("1.21.3", "0.106.1")
    createFabric("1.21.4", "0.119.3")
    createFabric("1.21.5", "0.119.3")
    createFabric("1.21.6", "0.128.1")
}