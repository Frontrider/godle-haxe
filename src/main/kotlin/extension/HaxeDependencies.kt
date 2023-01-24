package io.github.frontrider.godle.haxe.extension

import io.github.frontrider.godle.haxe.internalTaskGroup
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import javax.inject.Inject

abstract class HaxeDependencies @Inject constructor(val project: Project) {

    init {
        add("hxgodot", "https://github.com/HxGodot/hxgodot.git")
    }

    fun add(name: String, url: String? = null) {
        with(project) {
            val godleHaxeExtension = project.extensions.getByType(GodleHaxeExtension::class.java)

            val taskProvider = tasks.register("haxeInstall${name.replaceFirstChar { it.uppercase() }}", Exec::class.java) {
                with(it) {
                    group = internalTaskGroup
                    description = "install haxe library $name."
                    commandLine(godleHaxeExtension.getHaxeLibBinary())
                    if (url == null) {
                        args("install", name)
                    } else {
                        args("git", name, url)
                    }
                    dependsOn(tasks.getByPath("initHaxeLib"))
                }
            }
            afterEvaluate {
                tasks.getByPath("initHaxe").dependsOn(taskProvider)
            }
        }
    }
}
