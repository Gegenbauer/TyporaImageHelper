package com.gegenbauer.typora.imagehelper

import java.io.File

enum class MigrationRule : IMigration {
    RULE_CURRENT {
        override fun setTargetImageFilePath(migrationFileInfo: MigrationFileInfo) {
            migrationFileInfo.referredMdFile?.let {
                val targetDir = it.parentFile
                val targetFile = File(targetDir, migrationFileInfo.imageFilename)
                migrationFileInfo.targetPath = targetFile.absolutePath
            }
        }

        override fun setTargetMdReference(migrationFileInfo: MigrationFileInfo) {
            migrationFileInfo.referredMdFile?.let {
                migrationFileInfo.targetMdReference = "./${migrationFileInfo.imageFile.name}"
            }
        }
    },
    RULE_PARENT {
        override fun setTargetImageFilePath(migrationFileInfo: MigrationFileInfo) {
            migrationFileInfo.referredMdFile?.let {
                val targetDir = it.parentFile.parentFile
                val targetFile = File(targetDir, migrationFileInfo.imageFilename)
                migrationFileInfo.targetPath = targetFile.absolutePath
            }
        }

        override fun setTargetMdReference(migrationFileInfo: MigrationFileInfo) {
            migrationFileInfo.referredMdFile?.let {
                migrationFileInfo.targetMdReference = "../${migrationFileInfo.imageFilename}"
            }
        }
    },
    RULE_SPECIFIC {
        override fun setTargetImageFilePath(migrationFileInfo: MigrationFileInfo) {
            val targetFile = File(IMigration.specificImageDir, migrationFileInfo.imageFilename)
            migrationFileInfo.targetPath = targetFile.absolutePath
        }

        override fun setTargetMdReference(migrationFileInfo: MigrationFileInfo) {
            migrationFileInfo.targetMdReference = "${IMigration.specificImageDir}/${migrationFileInfo.imageFile.name}"
        }
    }
}