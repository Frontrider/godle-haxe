package io.github.frontrider.godle.haxe.tasks

import fi.linuxbox.gradle.download.Download
import io.github.frontrider.godle.haxe.*
import io.github.frontrider.godle.haxe.CacheFolder
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.DefaultProviderFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

abstract class HaxeDownloadTask @Inject constructor(workerExecutor: WorkerExecutor,objectFactory: ObjectFactory) : Download(workerExecutor) {

    //https://haxe.org/download/file/4.2.5/haxe-4.2.5-win64.zip/
    //https://haxe.org/download/file/4.2.5/haxe-4.2.5-win.zip/
    //https://haxe.org/download/file/4.2.5/haxe-4.2.5-linux64.tar.gz/
    //https://haxe.org/download/file/4.2.5/haxe-4.2.5-osx.tar.gz/
    //https://objects.githubusercontent.com/github-production-release-asset-2e65be/10282042/a4ab72e4-1d88-4845-a6a2-eb5e7447068c?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIWNJYAX4CSVEH53A%2F20230122%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20230122T133135Z&X-Amz-Expires=300&X-Amz-Signature=4d71dee8697216bc537fc899e9a66d861785f1d305073ed0d6b8b661b890b9af&X-Amz-SignedHeaders=host&actor_id=6755034&key_id=0&repo_id=10282042&response-content-disposition=attachment%3B%20filename%3Dhaxe-4.2.5-linux64.tar.gz&response-content-type=application%2Foctet-stream
    //https://github.com/HaxeFoundation/haxe/releases/download/4.2.5/haxe-4.2.5-linux64.tar.gz
    @Input
    val haxeVersion:Property<String> = objectFactory.property(String::class.java)

    @Input
    val haxeBaseUrl:Property<String> = objectFactory.property(String::class.java).convention(DefaultProviderFactory().provider {
        val platform = os.osName
        val arch = arch
        val type = if(os == SUPPORTED_OS.WINDOWS) "zip" else "tar.gz"
        val version = haxeVersion.get()

        return@provider "https://github.com/HaxeFoundation/haxe/releases/download/$version/haxe-$version-${platform}$arch.$type"
    })

    @get:Internal
    val cacheFolder:RegularFileProperty = objectFactory.fileProperty().convention {
        val platform = os.osName
        val arch = arch
        val type = if(os == SUPPORTED_OS.WINDOWS) "zip" else "tar.gz"
        val version = haxeVersion.get()

        File(CacheFolder,"haxe-$version-${platform}$arch.$type")
    }
    init {
        group =internalTaskGroup
        description = "Download the configured version of haxe"
        from.set(haxeBaseUrl)
        to.set(cacheFolder)
        //IF we already downloaded then this is up-to-date.
        outputs.upToDateWhen {
            to.get().asFile.exists()
        }
    }
}