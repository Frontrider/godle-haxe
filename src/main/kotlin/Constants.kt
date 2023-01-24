package io.github.frontrider.godle.haxe

import org.apache.commons.lang3.SystemUtils


//Global cache folders.
internal val CacheFolder =
    if ((System.getenv("HAXE_HOME") != null)) {
        "${System.getenv("HAXE_HOME")}/haxe_cache"
    } else
        if (System.getenv("GRADLE_HOME") != null) "${System.getenv("GRADLE_HOME")}/godle/haxe_cache" else "${
            System.getenv("HOME")
        }/.gradle/godle/haxe_cache"
internal val HaxeFolder =
    if ((System.getenv("HAXE_HOME") != null)) {
        "${System.getenv("HAXE_HOME")}/haxe"
    } else
        if (System.getenv("GRADLE_HOME") != null) "${System.getenv("GRADLE_HOME")}/godle/haxe" else "${
            System.getenv("HOME")
        }/.gradle/godle/haxe"


internal val internalTaskGroup = "godle internal"

//System type detection
enum class SUPPORTED_OS(val osName: String, val haxePlatform: String) {
    LINUX("linux", "linux"),
    MAC("osx", "macos"),
    WINDOWS("windows", "windows")
}

val os = when {
    SystemUtils.IS_OS_MAC -> {
        SUPPORTED_OS.MAC
    }

    SystemUtils.IS_OS_WINDOWS -> {
        SUPPORTED_OS.WINDOWS
    }
    //we default to linux if we had no idea what the system is.
    else -> {
        SUPPORTED_OS.LINUX
    }
}
val arch = when (SystemUtils.OS_ARCH) {
    "x86" -> "32"
    else -> "64"
}