package com.gegenbauer.typora.imagehelper

fun checkPathAndMoveFile(migrationFileInfo: MigrationFileInfo) {
    val sourceAbsolutePath = migrationFileInfo.imageFile.absolutePath
    if (migrationFileInfo.targetPath != sourceAbsolutePath) {
        println("move image: $sourceAbsolutePath to ${migrationFileInfo.targetPath}")
        migrationFileInfo.imageFile.xCopyTo(migrationFileInfo.targetPath)
    }
}