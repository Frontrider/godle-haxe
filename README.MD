#Gradle Godot Haxe plugin.

Prepares the entire haxe toolchain to be used, the only thing that needs to be installed on the host is Java and python.

The python requirement may or may not be removed in the future.

Example:
```kotlin


import io.github.frontrider.godle.dsl.versioning.asGodot4Beta
import io.github.frontrider.godle.haxe.GodleHaxePlugin
import io.github.frontrider.godle.haxe.extension.GodleHaxeExtension


plugins {
    id("io.github.frontrider.godle") version "0.20.0"
    id("io.github.frontrider.godle-haxe") version "0.1.0"
}

godle{
    //specify the version of godot to be used.
    asGodot4Beta("14")
}

godleHaxe{
    godotJsonBindingPath.set(godle.getBindingJsonPath())
    haxeDependencies {
        //hxgodot includes this, this is an example
        add("hxcpp")
        add("<name>","<git url>")
    }
}

//generate the binding json before the 
afterEvaluate {
    val generateHaxeBindings:Task by tasks
    val godleGenerateBindings:Task by tasks

    generateHaxeBindings.dependsOn(godleGenerateBindings)
}

```

The `releaseBuildFor<Platform>` tasks can be used to build for that platform, the `debugBuildFor<Platform>` builds for debug.
`releaseBuildMultiplatform` and `debugBuildMultiplatform` tries to build for all platforms.

`initHXGodot` runs `hxgodot init`.