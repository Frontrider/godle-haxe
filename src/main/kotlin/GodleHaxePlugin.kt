package io.github.frontrider.godle.haxe

import io.github.frontrider.godle.haxe.extension.GodleHaxeExtension
import io.github.frontrider.godle.haxe.tasks.HaxeDownloadTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import ru.vyarus.gradle.plugin.python.PythonExtension
import ru.vyarus.gradle.plugin.python.PythonPlugin
import java.io.File

class GodleHaxePlugin : Plugin<Project> {

    override fun apply(project1: Project) {

        with(project1) {
            //Set the default value for this property.
            if(!project1.hasProperty("haxe.root")) {
                project1.extensions.extraProperties.set("haxe.root", System.getenv("HOME") + "/.haxe/lib")
            }
            val initHaxe = tasks.register("initHaxe") {
                with(it) {
                    group = internalTaskGroup
                    description = "init haxe runtime."
                }
            }.get()

            val godleHaxeExtension = extensions.create("godleHaxe", GodleHaxeExtension::class.java)

            //setup python.
            apply {
                it.plugin(PythonPlugin::class.java)
            }
            val haxeDownloadTask = tasks.register("haxeDownload", HaxeDownloadTask::class.java) {
                it.haxeVersion.set(godleHaxeExtension.haxeVersion)
            }.get()

            val initHaxeLibTask = tasks.register("initHaxeLib", Exec::class.java) {
                with(it) {
                    initHaxe.dependsOn(this)
                    dependsOn(haxeDownloadTask)
                    group = internalTaskGroup
                    description = "init haxe libraries."
                    commandLine(godleHaxeExtension.getHaxeLibBinary())
                    //must be done after evaluation, and we treat the root as a constant.
                    args("setup", project.properties.getOrDefault("haxe.root",System.getenv("USER")+"/.haxe/lib"))
                }
            }.get()

            val extractHaxeTask = tasks.register("extractHaxe", Copy::class.java) { copy ->
                with(copy) {
                    group = internalTaskGroup
                    description = "extract the downloaded haxe archive"
                    dependsOn(haxeDownloadTask)
                    initHaxeLibTask.dependsOn(this)

                    if (os == SUPPORTED_OS.WINDOWS) {
                        from(zipTree(haxeDownloadTask.to.get()))
                    } else {
                        from(tarTree(resources.gzip(haxeDownloadTask.to.get())))
                    }
                    include("/*/**")
                    eachFile {
                        it.path = it.sourcePath.split("/", limit = 2)[1]
                    }
                    destinationDir = godleHaxeExtension.getHaxeFolder()
                }
            }
            haxeDownloadTask.finalizedBy(extractHaxeTask)
            val generateBindings = tasks.register("generateHaxeBindings", Exec::class.java) {
                with(it) {
                    group = "godle"
                    description = "create haxe bindings"

                    dependsOn(initHaxeLibTask)
                    commandLine(godleHaxeExtension.getHaxeLibBinary())
                    args("run", "hxgodot", "generate_bindings", "-y")

                    afterEvaluate {
                        if(godleHaxeExtension.godotJsonBindingPath.isPresent){
                            args("--extension-api-json="+godleHaxeExtension.godotJsonBindingPath.get())
                        }
                    }

                    outputs.upToDateWhen {
                        File(project.rootDir, "bindings").exists()
                    }
                }
            }.get()

            val initHXGodot = tasks.register("initHXGodot", Exec::class.java) {
                with(it) {
                    outputs.upToDateWhen {
                        File(project1.rootDir, "src").exists()
                    }
                    dependsOn(generateBindings)

                    group = "godle"
                    description = "init the haxelib project"

                    dependsOn(initHaxe)
                    commandLine(godleHaxeExtension.getHaxeLibBinary())
                    args("run", "hxgodot", "init", "-y")
                }
            }.get()


            val pythonExtension = extensions.getByType(PythonExtension::class.java)
            pythonExtension.pip("SCons:4.4.0")

            val pipInstall = tasks.named("pipInstall").get()

            listOf("debug", "release").forEach { target ->
                val multiplatformBuild = tasks.register("${target}BuildMultiplatform") {
                    with(it) {
                        dependsOn(pipInstall)
                        group = "godle"
                        description = "Build haxe for all platform with a $target target"
                    }

                }.get()
                SUPPORTED_OS.values().forEach { os ->
                    val buildForOs = tasks.register(
                        "${target}BuildFor${os.osName.replaceFirstChar { it.uppercase() }}",
                        Exec::class.java
                    ) {
                        with(it) {
                            group = internalTaskGroup
                            description = "build haxe for ${os.osName}, with a $target target"
                            dependsOn(initHaxe)
                            commandLine(rootDir.absolutePath + "/.gradle/python/bin/scons")
                            args("platform=${os.haxePlatform}", "target=$target")
                        }
                    }.get()
                    multiplatformBuild.finalizedBy(buildForOs)
                }
            }
        }
    }
}