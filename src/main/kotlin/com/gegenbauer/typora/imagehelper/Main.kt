package com.gegenbauer.typora.imagehelper

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.util.regex.Pattern

private const val TEMP_FILE_PREFIX = "temp"
private const val UNUSED_IMAGE_FILE_DIR_NAME = "unused_image"

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

private val migrationFileInfoMap = mutableMapOf<String, MigrationFileInfo>()

/**
 * pattern to match image reference in md file
 */
private val mdImageRefPattern = Pattern.compile("^!\\[[^]]*]\\(([^)]*)\\)")
private var rule = MigrationRule.RULE_CURRENT

/**
 * target path to move images to
 * only valid when rule is RULE_SPECIFIC
 */
var targetPath = ""

/**
 * target dir to sort images
 */
var typoraRootDir = ""

// TODO 进一步验证功能，重构代码，编写单元测试
fun main(args: Array<String>) {
    if (args.first() == "-h") {
        println(
            """
            按照指定的规则移动文件夹下的图片，并修改 md 文件中相关引用，确保能成功引用
            请确保你的图片文件名不重复，否则可能会出现意想不到的问题
            param1: 需要处理的文件夹
            param2: 移动规则
                    0-current:    移动到和 md 文件相同的文件夹下
                    1-parent:     移动到 md 文件的父文件夹下
                    2-specific:   移动到指定的文件夹下
                    
        """.trimIndent()
        )
        return
    }

    runCatching {
        typoraRootDir = args[0]
        if (!isDirValid(typoraRootDir)) {
            return
        }

        rule = MigrationRule.values()[args[1].toInt()]
        if (rule == MigrationRule.RULE_SPECIFIC) {
            targetPath = args[2]
        }

        if (isDirValid(targetPath)) {
            return
        }
        println("rule: $rule, targetPath: $targetPath")

        // collect all images and md files
        collectFilesFromDir(typoraRootDir, File::isImage).forEach {
            if (migrationFileInfoMap.containsKey(it.name)) {
                println("duplicate image file: ${it.absolutePath}")
            }
            migrationFileInfoMap[it.name] = MigrationFileInfo(it.name, it)
        }
        val mdFiles = collectFilesFromDir(typoraRootDir, File::isMd)
        val plantUmlFiles = collectFilesFromDir(typoraRootDir, File::isPlantUml)

        // modify md file and move images to path from destImages
        mdFiles.forEach { checkAndModifyMdContent(it) }

        // move unused images to unused_image folder
        moveUnusedImagesToUnusedImageFolder()

        // move the plantuml file with same name of image file to the same folder of image file
        plantUmlFiles.forEach { movePlantUmlFileToSameFolderWithImageFile(it) }

    }.onFailure {
        when (it) {
            is ArrayIndexOutOfBoundsException -> println("invalid args!")
            else -> println("unknown error: ${it.message}")
        }
    }
}

private fun moveUnusedImagesToUnusedImageFolder() {
    val unusedImageDir = File(typoraRootDir, UNUSED_IMAGE_FILE_DIR_NAME)
    if (!unusedImageDir.exists()) {
        unusedImageDir.mkdirs()
    }
    migrationFileInfoMap.values.filter { it.referredMdFile == null }.forEach {
        println("move unused image: ${it.imageFile.absolutePath} to ${unusedImageDir.absolutePath}")
        it.imageFile.xCopyTo(File(unusedImageDir, it.imageFile.name).absolutePath)
    }
}

private fun movePlantUmlFileToSameFolderWithImageFile(plantUmlFile: File) {
    val imageFileName = plantUmlFile.nameWithoutExtension
    // find the image file with same name
    val migrationFileInfo = migrationFileInfoMap.entries.find { it.key.getFilenameWithoutExt() == imageFileName }?.value
    if (migrationFileInfo != null) {
        val targetPath = "${migrationFileInfo.targetPath.getParentPath()}/${plantUmlFile.name}"
        println("move plantuml file: ${plantUmlFile.absolutePath} to $targetPath")
        plantUmlFile.xCopyTo(targetPath)
    }
}

private fun String.getFilenameWithoutExt(): String {
    return substringAfterLast(File.separator)
}

private fun String.getParentPath(): String {
    return substringBeforeLast(File.separator)
}

/**
 * check content in md file, and replace the image reference
 * in order to make the image reference valid
 */
private fun checkAndModifyMdContent(file: File) {
    val tempFile = File.createTempFile(TEMP_FILE_PREFIX, file.extension, file.parentFile)
    val modifiedContent = StringBuilder()
    var isFileContentModified = false
    file.forEachLine { line ->
        val matcher = mdImageRefPattern.matcher(line)
        if (matcher.find()) {
            val originMdRef = matcher.group(1)
            val fileName = originMdRef.substringAfterLast("/")
            val migrationFileInfo = migrationFileInfoMap[fileName]
            if (migrationFileInfo != null) {
                migrationFileInfo.referredMdFile = file
                val targetImageFilePath = migrationFileInfo.getTargetImageFilePathByRule(rule)
                val sourceAbsolutePath = migrationFileInfo.imageFile.absolutePath
                if (targetImageFilePath != sourceAbsolutePath) {
                    println("move image: $sourceAbsolutePath to ${migrationFileInfo.getTargetImageFilePathByRule(rule)}")
                    migrationFileInfo.imageFile.xCopyTo(targetImageFilePath)
                }
                val targetMdRef = migrationFileInfo.getTargetMdReferenceByRule(rule)
                if (targetMdRef != originMdRef) {
                    println("modify line, origin: $originMdRef, target: $targetMdRef")
                    modifiedContent.appendLine(line.replace(originMdRef, migrationFileInfo.getTargetMdReferenceByRule(rule)))
                    isFileContentModified = true
                }
            } else {
                modifiedContent.appendLine(line)
            }
        } else {
            modifiedContent.appendLine(line)
        }
    }
    if (isFileContentModified) {
        tempFile.writeText(modifiedContent.toString())
        file.delete()
        tempFile.renameTo(file)
    } else {
        tempFile.delete()
    }
}

private fun isDirValid(dir: String) = File(dir).run { dir.isNotEmpty() && exists() && isDirectory }

private fun collectFilesFromDir(rootDir: String, filter: (File) -> Boolean): List<File> {
    val images = mutableListOf<File>()
    File(rootDir).visitAllChildren {
        if (filter(it)) {
            images.add(it)
        }
    }
    return images
}

private fun File.visitAllChildren(onFileVisited: (File) -> Unit) {
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
private fun File.xCopyTo(dest: String) {
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

private fun File.isImage(): Boolean {
    return exists() && isFile && (extension in imageFileExt)
}

private fun File.isMd(): Boolean {
    return exists() && isFile && (extension in mdFileExt)
}

private fun File.isPlantUml(): Boolean {
    return exists() && isFile && (extension in plantUmlFileExt)
}
