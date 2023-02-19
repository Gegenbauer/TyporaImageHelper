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
    var targetPath: String = "",
    var targetMdReference: String = ""
) {
    override fun hashCode(): Int {
        return imageFilename.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is MigrationFileInfo && other.imageFilename == imageFilename
    }
}