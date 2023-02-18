package com.gegenbauer.typora.imagehelper

import java.io.File

/**
 * used for image file migration
 * key: source filename
 */
data class MigrationFileInfo(
    val imageFilename: String,
    val imageFile: File,
    var referredMdFile: File? = null,
) {
    fun getTargetImageFilePathByRule(rule: MigrationRule): String {
        return when(rule) {
            MigrationRule.RULE_CURRENT -> {
                val targetDir = referredMdFile!!.parentFile
                val targetFile = File(targetDir, imageFilename)
                targetFile.absolutePath
            }
            MigrationRule.RULE_PARENT -> {
                val targetDir = referredMdFile!!.parentFile.parentFile
                val targetFile = File(targetDir, imageFilename)
                targetFile.absolutePath
            }
            MigrationRule.RULE_SPECIFIC -> {
                val targetFile = File(targetPath, imageFilename)
                targetFile.absolutePath
            }
        }
    }

    fun getTargetMdReferenceByRule(rule: MigrationRule): String {
        return when (rule) {
            MigrationRule.RULE_CURRENT -> {
                "./${imageFile.name}"
            }
            MigrationRule.RULE_PARENT -> {
                "../${imageFile.name}"
            }
            MigrationRule.RULE_SPECIFIC -> {
                "${targetPath}/${imageFile.name}"
            }
        }
    }

    override fun hashCode(): Int {
        return imageFilename.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is MigrationFileInfo && other.imageFilename == imageFilename
    }
}