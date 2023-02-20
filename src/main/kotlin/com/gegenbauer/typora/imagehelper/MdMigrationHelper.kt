package com.gegenbauer.typora.imagehelper

import java.io.File

object MdMigrationHelper {

    private const val TEMP_FILE_PREFIX = "temp"
    private const val UNUSED_IMAGE_FILE_DIR_NAME = "unused_image"

    private val migrationFileInfoMap = mutableMapOf<String, MigrationFileInfo>()
    /**
     * target dir where need to sort images
     */
    var typoraRootDir = ""

    fun printHelpMessage() {
        println(
            """
            按照指定的规则移动文件夹下的图片，并修改 md 文件中相关引用，确保能成功引用
            param1: 需要处理的文件夹
            param2: 移动规则
                    0-current:    移动到和 md 文件相同的文件夹下
                    1-parent:     移动到 md 文件的父文件夹下
                    2-specific:   移动到指定的文件夹下
            
            建议使用 rule 2，这样如果有多处使用同一张图片，不需要拷贝多份，而且可以统一管理
            使用 rule 0 或 1 时，请确保你的图片文件名不重复，否则可能会出现意想不到的问题
                    
        """.trimIndent()
        )
    }

    fun startMigration() {
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
            run loop@{
                val matchResults = MigrationRule.mdImageRefRegex.findAll(line)
                var tempLine = line
                matchResults.forEach {
                    val originMdRef = it.groupValues[1]
                    val filename = originMdRef.substringAfterLast(File.separator)
                    val migrationFileInfo = migrationFileInfoMap[filename]
                    if (migrationFileInfo != null) {
                        migrationFileInfo.referredMdFile = file

                        MigrationRule.rule.setTargetImageFilePath(migrationFileInfo)
                        checkPathAndMoveFile(migrationFileInfo)

                        MigrationRule.rule.setTargetMdReference(migrationFileInfo)
                        if (migrationFileInfo.targetMdReference != originMdRef) {
                            println("modify line, origin: $originMdRef, target: ${migrationFileInfo.targetMdReference}")
                            tempLine = tempLine.replace(originMdRef, migrationFileInfo.targetMdReference)
                            isFileContentModified = true
                        }
                    } else {
                        println("can not find image file: $filename in md file: ${file.absolutePath}")
                    }
                }
                modifiedContent.appendLine(tempLine)
            }
        }
        if (isFileContentModified) {
            tempFile.writeText(modifiedContent.toString())
            file.delete()
            tempFile.xCopyTo(file)
        } else {
            tempFile.delete()
        }
    }

    /**
     * helps to formalize the md reference before migration
     * used for debug
     */
    private fun formalizeMdReference(file: File) {
        val tempFile = File.createTempFile(TEMP_FILE_PREFIX, file.extension, file.parentFile)
        val modifiedContent = StringBuilder()
        file.forEachLine { line ->
            run loop@{
                val matchResults = MigrationRule.mdImageRefRegex.findAll(line)
                var tempLine = line
                matchResults.forEach {
                    val originMdRef = it.groupValues[1]
                    tempLine = tempLine.replace(originMdRef, originMdRef.replace("\\", File.separator))
                }
                modifiedContent.appendLine(tempLine)
            }
        }
        tempFile.writeText(modifiedContent.toString())
        file.delete()
        tempFile.xCopyTo(file)
    }
}