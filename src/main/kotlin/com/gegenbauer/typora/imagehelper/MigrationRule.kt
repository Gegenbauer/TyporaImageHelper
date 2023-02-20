package com.gegenbauer.typora.imagehelper

import java.io.File

sealed class MigrationRule : IMigration {

    object RuleCurrent : MigrationRule() {
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

    }

    object RuleParent : MigrationRule() {
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
    }

    object RuleSpecific : MigrationRule() {
        override fun setTargetImageFilePath(migrationFileInfo: MigrationFileInfo) {
            val targetFile = File(IMigration.specificImageDir, migrationFileInfo.imageFilename)
            migrationFileInfo.targetPath = targetFile.absolutePath
        }

        override fun setTargetMdReference(migrationFileInfo: MigrationFileInfo) {
            migrationFileInfo.apply {
                targetMdReference = referredMdFile?.getRelativePath(File(targetPath)) ?: ""
            }
        }
    }

    companion object {
        /**
         * regex to match image reference in md file
         */
        val mdImageRefRegex = Regex("!\\[[^]]*]\\(([^)]*)\\)")
        var rule: MigrationRule = RuleCurrent

        fun parseRule(index: Int): MigrationRule {
            return when (index) {
                0 -> RuleCurrent
                1 -> RuleParent
                2 -> RuleSpecific
                else -> RuleCurrent
            }
        }
    }
}

