package com.gegenbauer.typora.imagehelper

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private val imageFileExt = hashSetOf(
    "png",
    "svg",
    "gif",
    "webp",
    "jpg",
    "jpeg",
    "bmp",
    "tiff",
    "tif",
    "awebp"
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
    if (this.absolutePath == dest) {
        return
    }
    kotlin.runCatching {
        FileInputStream(this).channel.use { source ->
            FileOutputStream(dest).channel.use { dest ->
                dest.transferFrom(source, 0, source.size())
            }
        }
    }.onFailure {
        println("copy file failed, source=${this.absoluteFile} dest=$dest")
    }.onSuccess {
        if (File(dest).exists()) {
            this.delete()
        }
    }
}



fun File.xCopyTo(dest: File) {
    xCopyTo(dest.absolutePath)
}

/**
 * get relative path of target file from this file
 * given file = "a/b/c/d/e.txt", targetFile = "a/b/c/f/g.txt", return "../f/g.txt"
 */
fun File.getRelativePath(target: File): String {
    val thisPath = absolutePath
    val targetPath = target.absolutePath
    val thisPathList = thisPath.split(File.separator)
    val targetPathList = targetPath.split(File.separator)
    val commonPath = mutableListOf<String>()
    for (i in 0 until minOf(thisPathList.size, targetPathList.size)) {
        if (thisPathList[i] == targetPathList[i]) {
            commonPath.add(thisPathList[i])
        } else {
            break
        }
    }
    val thisPathDiff = thisPathList.subList(commonPath.size, thisPathList.size)
    val targetPathDiff = targetPathList.subList(commonPath.size, targetPathList.size)
    val relativePath = StringBuilder()
    for (i in 0 until thisPathDiff.size - 1) {
        relativePath.append("..${File.separator}")
    }
    for (i in targetPathDiff.indices) {
        relativePath.append(targetPathDiff[i])
        if (i != targetPathDiff.size - 1) {
            relativePath.append(File.separator)
        }
    }
    return relativePath.toString()
}

fun isDirValid(dir: String): Boolean {
    return File(dir).isDirValid()
}

fun checkAndCreateDir(dirPath: String) {
    val dir = File(dirPath)
    if (dir.exists().not()) {
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
