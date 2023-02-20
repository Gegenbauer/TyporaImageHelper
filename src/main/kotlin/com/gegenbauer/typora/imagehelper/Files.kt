package com.gegenbauer.typora.imagehelper

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private val imageFileExt = hashSetOf(
    "png",
    "svg",
    "gif",
)
private val mdFileExt = hashSetOf(
    "md",
    "markdown",
)
private val plantUmlFileExt = hashSetOf(
    "puml",
    "plantuml",
)

fun File.isImage(): Boolean {
    return exists() && isFile && (extension in imageFileExt)
}

fun File.isMd(): Boolean {
    return exists() && isFile && (extension in mdFileExt)
}

fun File.isPlantUml(): Boolean {
    return exists() && isFile && (extension in plantUmlFileExt)
}

fun File.visitAllChildren(onFileVisited: (File) -> Unit) {
    listFiles()?.forEach {
        if (it.isFile) {
            onFileVisited(it)
        } else if (it.isDirectory) {
            it.visitAllChildren(onFileVisited)
        }
    }
}

/**
 * copy file and delete source file
 */
fun File.xCopyTo(dest: String) {
    kotlin.runCatching {
        FileInputStream(this).channel.use { source ->
            FileOutputStream(dest).channel.use { dest ->
                dest.transferFrom(source, 0, source.size())
            }
        }
    }.onFailure {
        println("copy file failed, source=${this.absoluteFile} dest=$dest")
    }.onSuccess {
        delete()
    }
}

fun isDirValid(dir: String): Boolean {
    return File(dir).isDirValid()
}

fun checkAndCreateDir(dir: String) {
    val dir = File(dir)
    if (dir.exists().not() && dir.isDirectory) {
        dir.mkdir()
    }
}

fun File.isDirValid(): Boolean {
    return exists() && isDirectory
}

fun String.getFilenameWithoutExt(): String {
    return substringAfterLast(File.separator)
}

fun String.getParentPath(): String {
    return substringBeforeLast(File.separator)
}

fun collectFilesFromDir(rootDir: String, filter: (File) -> Boolean): List<File> {
    val images = mutableListOf<File>()
    File(rootDir).visitAllChildren {
        if (filter(it)) {
            images.add(it)
        }
    }
    return images
}
