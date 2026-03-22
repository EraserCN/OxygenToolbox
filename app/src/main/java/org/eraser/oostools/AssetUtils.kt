package org.eraser.oostools

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object AssetUtils {
    fun copyAssetsToFilesDir(context: Context, assetNames: List<String>) {
        assetNames.forEach { assetName ->
            val outFile = File(context.filesDir, assetName)
            if (!outFile.exists() || outFile.length() == 0L) {
                context.assets.open(assetName).use { inputStream ->
                    FileOutputStream(outFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }
}
