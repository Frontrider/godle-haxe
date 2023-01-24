package io.github.frontrider.godle.haxe.extension

import io.github.frontrider.godle.haxe.HaxeFolder
import io.github.frontrider.godle.haxe.SUPPORTED_OS
import io.github.frontrider.godle.haxe.os
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.provider.DefaultProviderFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import java.io.File
import javax.inject.Inject

abstract class GodleHaxeExtension @Inject constructor(objectFactory: ObjectFactory, project: Project) {

    val haxeVersion: Property<String> = objectFactory.property(String::class.java).convention("4.2.5")

    val godotJsonBindingPath: Property<String> = objectFactory.property(String::class.java)
    fun getHaxeFolder(): File {
        return File(HaxeFolder + "/" + haxeVersion.get())
    }

    @Nested
    abstract fun getHaxeDependencies(): HaxeDependencies

    fun haxeDependencies(action: Action<HaxeDependencies>) {
        action.execute(getHaxeDependencies())
    }



    val haxeInitTask: Property<Task> =
        objectFactory.property(Task::class.java).convention(DefaultProviderFactory().provider {
            return@provider project.tasks.getByPath("initHaxe")
        })

    fun getHaxeBinary(): File {
        val binary = if (os == SUPPORTED_OS.WINDOWS) {
            "haxe.exe"
        } else {
            "haxe"
        }
        return File(getHaxeFolder(), binary)
    }

    fun getHaxeLibBinary(): File {
        val binary = if (os == SUPPORTED_OS.WINDOWS) {
            "haxelib.exe"
        } else {
            "haxelib"
        }
        return File(getHaxeFolder(), binary)
    }

}