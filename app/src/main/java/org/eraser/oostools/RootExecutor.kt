package org.eraser.oostools

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object RootExecutor {

    var isShizukuMode: Boolean = false
    private var shizukuService: IShizukuService? = null

    data class RootResult(
        val success: Boolean,
        val output: String,
        val error: String
    )

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            shizukuService = IShizukuService.Stub.asInterface(binder)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            shizukuService = null
        }
    }

    private suspend fun requireShizukuService(): IShizukuService? = suspendCoroutine { cont ->
        if (shizukuService != null) {
            cont.resume(shizukuService)
            return@suspendCoroutine
        }
        val args = UserServiceArgs(ComponentName("org.eraser.oostools", ShizukuServiceImpl::class.java.name))
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(true)
            .version(1)
        
        Shizuku.bindUserService(args, object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                shizukuService = IShizukuService.Stub.asInterface(binder)
                Shizuku.unbindUserService(args, this, true)
                Shizuku.bindUserService(args, serviceConnection)
                cont.resume(shizukuService)
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                cont.resume(null)
            }
        })
    }

    suspend fun checkRoot(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun execute(command: String): RootResult = withContext(Dispatchers.IO) {
        try {
            if (isShizukuMode) {
                val service = requireShizukuService()
                    ?: return@withContext RootResult(false, "", "Failed to connect to Shizuku UserService")
                
                val rawResult = service.execute(command)
                if (rawResult.startsWith("ERR:")) {
                    val parts = rawResult.split(":", limit = 3)
                    RootResult(false, "", parts.getOrNull(2) ?: "Unknown error")
                } else {
                    RootResult(true, rawResult, "")
                }
            } else {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                
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

                RootResult(
                    success = exitCode == 0,
                    output = output.toString().trim(),
                    error = error.toString().trim()
                )
            }
        } catch (e: Exception) {
            RootResult(
                success = false,
                output = "",
                error = e.message ?: "Unknown error"
            )
        }
    }
}
