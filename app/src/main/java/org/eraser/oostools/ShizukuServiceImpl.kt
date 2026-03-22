package org.eraser.oostools

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess

class ShizukuServiceImpl : IShizukuService.Stub() {
    override fun execute(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/bin/sh", "-c", command))
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = StringBuilder()
            val error = StringBuilder()

            var line: String? = outputReader.readLine()
            while (line != null) {
                output.append(line).append("\n")
                line = outputReader.readLine()
            }

            line = errorReader.readLine()
            while (line != null) {
                error.append(line).append("\n")
                line = errorReader.readLine()
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) output.toString().trim() else "ERR:$exitCode:${error.toString().trim()}"
        } catch (e: Exception) {
            "ERR:-1:${e.message}"
        }
    }

    override fun destroy() {
        exitProcess(0)
    }
}
