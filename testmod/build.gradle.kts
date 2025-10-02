plugins {
    id("earth.terrarium.cloche") version "0.13.6"
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
            loaderVersion = "0.17.2"

            includedClient()

            runs {
                client()
            }

            dependencies {
                fabricApi(apiVersion, version)
                implementation(rootProject.tasks.named<Jar>("buildMergedFabric").get().outputs.files)
                include(rootProject.tasks.named<Jar>("buildMergedFabric").get().outputs.files)
            }

            metadata {
                entrypoint("main") {
                    value = "com.moulberry.lattice.testmod.LatticeTestMod"
                }
            }
        }
    }

    createFabric("1.20.1", "0.92.6")
    createFabric("1.20.2", "0.91.6")
    createFabric("1.20.4", "0.91.3")
    createFabric("1.20.6", "0.100.8")
    createFabric("1.21.1", "0.116.4")
    createFabric("1.21.3", "0.106.1")
    createFabric("1.21.4", "0.119.3")
    createFabric("1.21.5", "0.119.3")
    createFabric("1.21.6", "0.128.1")
    createFabric("1.21.9", "0.133.14")
}
