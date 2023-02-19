package com.gegenbauer.typora.imagehelper

import java.io.File
import java.util.regex.Pattern

private const val TEMP_FILE_PREFIX = "temp"
private const val UNUSED_IMAGE_FILE_DIR_NAME = "unused_image"

private val migrationFileInfoMap = mutableMapOf<String, MigrationFileInfo>()

/**
 * pattern to match image reference in md file
 */
private val mdImageRefPattern = Pattern.compile("^!\\[[^]]*]\\(([^)]*)\\)")
private var rule = MigrationRule.RULE_CURRENT

/**
 * target dir where need to sort images
 */
private var typoraRootDir = ""

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
            IMigration.specificImageDir = args[2]
        }

        if (isDirValid(IMigration.specificImageDir)) {
            return
        }
        println("rule: $rule, targetPath: ${IMigration.specificImageDir}")

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

/**
 * check content in md file, and replace the image reference
 * in order to make the image reference valid
 */
private fun checkAndModifyMdContent(file: File) {
    val tempFile = File.createTempFile(TEMP_FILE_PREFIX, file.extension, file.parentFile)
    val modifiedContent = StringBuilder()
    var isFileContentModified = false
    file.forEachLine { line ->
        run loop@ {
            val matcher = mdImageRefPattern.matcher(line)
            if (matcher.find()) {
                val originMdRef = matcher.group(1)
                val filename = originMdRef.substringAfterLast("/")
                val migrationFileInfo = migrationFileInfoMap[filename]
                if (migrationFileInfo != null) {
                    migrationFileInfo.referredMdFile = file

                    rule.setTargetImageFilePath(migrationFileInfo)
                    checkPathAndMoveFile(migrationFileInfo)

                    rule.setTargetMdReference(migrationFileInfo)
                    if (migrationFileInfo.targetMdReference != originMdRef) {
                        println("modify line, origin: $originMdRef, target: ${migrationFileInfo.targetMdReference}")
                        modifiedContent.appendLine(line.replace(originMdRef, migrationFileInfo.targetMdReference))
                        isFileContentModified = true
                        return@loop
                    }
                }
            }
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

