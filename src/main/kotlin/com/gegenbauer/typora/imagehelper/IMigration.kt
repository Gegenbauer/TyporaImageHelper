package com.gegenbauer.typora.imagehelper

interface IMigration {
    fun setTargetImageFilePath(migrationFileInfo: MigrationFileInfo)

    fun setTargetMdReference(migrationFileInfo: MigrationFileInfo)

    companion object {
        var specificImageDir = ""
    }
}