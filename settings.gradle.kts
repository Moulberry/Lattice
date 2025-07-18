pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.msrandom.net/repository/cloche")
        gradlePluginPortal()
    }
}

rootProject.name = "lattice"
include("testmod")
include("testmod-neoforge")
