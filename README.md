# Lattice

A library for creating configuration GUIs in Minecraft

# Installing

Lattice is available through Maven Central.

__Gradle__
```groovy
dependencies {
    include(modImplementation("com.moulberry:lattice:1.2.6")) {
        attributes {
            attribute(Attribute.of("earth.terrarium.cloche.modLoader", String), "fabric")
        }
    }
}
```

# When to use Lattice
Lattice is only a library for creating configuration GUIs. If you need a fully-featured
library that handles serialization, client/server sync, or other fancy features found
in some configuration libraries, you should not use Lattice.

You should use Lattice if you like how it looks. That's pretty much it.

# Using the library

## LatticeElements
The first step to using Lattice is creating a LatticeElements object.
The LatticeElements object describes all the elements of the config.

The simplest way to create a LatticeElements is by using the annotations on your config class,
and then running `LatticeElements.fromAnnotations(Component.literal("Config Title"), config)`

For an example of how to use the annotations, see the [Example Config](https://github.com/Moulberry/Lattice/blob/master/testmod/src/main/java/com/moulberry/lattice/testmod/TestConfig.java)

If you want to see the example config in action, you can run `./gradlew :testmod:runFabric1216Client`

If you don't want to use the annotations, you can create the LatticeElements object yourself.

## Config screen
Once you have your LatticeElements, you can create a config screen like so:
`Lattice.createConfigScreen(latticeElements, config::saveToDefaultFolder, Minecraft.getInstance().screen)`

The second arg is a runnable that is invoked when the screen is closed, you can use this to save the config.

The third arg is the screen that will appear when you close the config, it's recommended to set this to the current screen.

## Testing the config
It's recommended to test the config at the start of your mod initialization, this can ensure
that you catch any issues early and prevents accidentally releasing a version of your mod with issues

```java
if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
    Minecraft.getInstance().schedule(() -> Lattice.performTest(latticeElements));
}
```

# Screenshots

## Numbers (Gui Scale 2)
![Numbers](https://raw.githubusercontent.com/Moulberry/Lattice/refs/heads/master/screenshots/numbers_guiscale2.png)

## Enums (Gui Scale Auto)
![Enums](https://raw.githubusercontent.com/Moulberry/Lattice/refs/heads/master/screenshots/enums_guiscaleauto.png)

## Subcategories
![Subcategories](https://raw.githubusercontent.com/Moulberry/Lattice/refs/heads/master/screenshots/subcategories.png)

## Searching
![Searching](https://raw.githubusercontent.com/Moulberry/Lattice/refs/heads/master/screenshots/searching.png)

# License
The library is available under the MIT license.
