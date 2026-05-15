package org.eraser.oostools

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import rikka.shizuku.Shizuku

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("OosToolsPrefs", Context.MODE_PRIVATE)
    var hasAgreedDisclaimer by remember { mutableStateOf(sharedPref.getBoolean("hasAgreedDisclaimer", false)) }

    if (!hasAgreedDisclaimer) {
        AlertDialog(
            onDismissRequest = { /* Must agree or disagree */ },
            title = { Text(stringResource(R.string.disclaimer_title), color = MaterialTheme.colorScheme.error) },
            text = { 
                Text(
                    text = stringResource(R.string.disclaimer_text),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    sharedPref.edit().putBoolean("hasAgreedDisclaimer", true).apply()
                    hasAgreedDisclaimer = true 
                }) {
                    Text(stringResource(R.string.agree))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    (context as? android.app.Activity)?.finish() 
                }) {
                    Text(stringResource(R.string.disagree), color = MaterialTheme.colorScheme.error)
                }
            }
        )
        return
    }

    // OPlus device check
    var showOplusWarning by remember { mutableStateOf(!isOplusDevice()) }
    if (showOplusWarning) {
        AlertDialog(
            onDismissRequest = { /* non-dismissable */ },
            title = { Text(stringResource(R.string.oplus_check_title), color = MaterialTheme.colorScheme.error) },
            text = { Text(text = stringResource(R.string.oplus_check_text), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { showOplusWarning = false }) {
                    Text(stringResource(R.string.oplus_check_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                    Text(stringResource(R.string.oplus_check_exit), color = MaterialTheme.colorScheme.error)
                }
            }
        )
        return
    }

    // OPlus firmware (ROM) check
    var showFirmwareWarning by remember { mutableStateOf(!isOplusFirmware()) }
    if (showFirmwareWarning) {
        AlertDialog(
            onDismissRequest = { /* non-dismissable */ },
            title = { Text(stringResource(R.string.firmware_check_title), color = MaterialTheme.colorScheme.error) },
            text = { Text(text = stringResource(R.string.firmware_check_text), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { showFirmwareWarning = false }) {
                    Text(stringResource(R.string.oplus_check_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                    Text(stringResource(R.string.oplus_check_exit), color = MaterialTheme.colorScheme.error)
                }
            }
        )
        return
    }

    var currentTab by remember { mutableStateOf(0) }
    var arbResult: Pair<Boolean, String>? by remember { mutableStateOf(null) }
    // SHIZUKU_DISABLED: 强制 workMode=0（Root），不从 SharedPreferences 读取 Shizuku 模式
    var workMode by remember { mutableStateOf(0 /* sharedPref.getInt("workMode", 0) */) }
    var isAuthorized by remember { mutableStateOf<Boolean?>(null) }
    var binderReceived by remember { mutableStateOf(false) }

    /* SHIZUKU_DISABLED: Shizuku Binder 监听器已屏蔽
    DisposableEffect(Unit) {
        val listener = rikka.shizuku.Shizuku.OnBinderReceivedListener {
            binderReceived = true
        }
        val deadListener = rikka.shizuku.Shizuku.OnBinderDeadListener {
            binderReceived = false
        }
        try {
            rikka.shizuku.Shizuku.addBinderReceivedListenerSticky(listener)
            rikka.shizuku.Shizuku.addBinderDeadListener(deadListener)
        } catch(e: Exception) {}

        onDispose {
            try {
                rikka.shizuku.Shizuku.removeBinderReceivedListener(listener)
                rikka.shizuku.Shizuku.removeBinderDeadListener(deadListener)
            } catch(e: Exception) {}
        }
    }
    */

    LaunchedEffect(workMode, binderReceived) {
        RootExecutor.isShizukuMode = false // SHIZUKU_DISABLED: 始终为 false
        // SHIZUKU_DISABLED: 只走 Root 分支
        val hasRoot = RootExecutor.checkRoot()
        isAuthorized = hasRoot
        /* SHIZUKU_DISABLED: Shizuku 权限检查已屏蔽
        if (workMode == 0) {
            val hasRoot = RootExecutor.checkRoot()
            isAuthorized = hasRoot
        } else {
            try {
                if (rikka.shizuku.Shizuku.pingBinder() && rikka.shizuku.Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    isAuthorized = true
                } else {
                    isAuthorized = false
                }
            } catch (e: Exception) {
                isAuthorized = false
            }
        }
        */
    }

    if (isAuthorized == false) {
        AuthScreen(
            onRootGranted = {
                sharedPref.edit().putInt("workMode", 0).apply()
                workMode = 0
                isAuthorized = true
            },
            onShizukuGranted = {
                sharedPref.edit().putInt("workMode", 1).apply()
                workMode = 1
                isAuthorized = true
            }
        )
        return
    }

    if (isAuthorized == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Secondary Full Screen for ARB Result
    if (arbResult != null) {
        ArbResultScreen(
            isTripped = arbResult!!.first,
            rawOutput = arbResult!!.second,
            onBack = { arbResult = null }
        )
        return
    }

    Scaffold(
        topBar = {
            // SHIZUKU_DISABLED: expanded 已随模式菜单一并屏蔽
            // var expanded by remember { mutableStateOf(false) }
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    // SHIZUKU_DISABLED: 模式切换按钮已屏蔽（固定显示 Root 模式）
                    TextButton(onClick = { /* expanded = true */ }) {
                        Text(stringResource(R.string.mode_root))
                    }
                    /* SHIZUKU_DISABLED: 模式切换菜单已屏蔽
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.mode_root)) },
                            onClick = {
                                expanded = false
                                workMode = 0
                                sharedPref.edit().putInt("workMode", 0).apply()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.mode_shizuku)) },
                            onClick = {
                                expanded = false
                                workMode = 1
                                sharedPref.edit().putInt("workMode", 1).apply()
                            }
                        )
                    }
                    */
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.Build, contentDescription = stringResource(R.string.tab_tools)) },
                    label = { Text(stringResource(R.string.tab_tools)) },
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.tab_system)) },
                    label = { Text(stringResource(R.string.tab_system)) },
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.PhoneAndroid, contentDescription = stringResource(R.string.tab_device)) },
                    label = { Text(stringResource(R.string.tab_device)) },
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.Info, contentDescription = stringResource(R.string.tab_about)) },
                    label = { Text(stringResource(R.string.tab_about)) },
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            var isExecuting by remember { mutableStateOf(false) }
            
            when (currentTab) {
                0 -> ToolsTab(context, isExecuting, { isExecuting = it })
                1 -> SystemTab(context, isExecuting, { isExecuting = it }, { arbResult = it })
                2 -> DeviceInfoTab()
                3 -> AboutTab(context)
            }
        }
    }
}

@Composable
fun ToolsTab(
    context: Context,
    isExecuting: Boolean,
    setExecuting: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.tools_header_assistant),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        val shelfLabel = context.getString(R.string.tools_shelf_title)
        ActionCard(
            title = shelfLabel,
            description = stringResource(R.string.tools_shelf_desc),
            isExecuting = isExecuting,
            onClick = {
                val cmd = "settings put secure assistant_screen_type 1 && settings put secure assistant_screen_type_left_enable 1"
                executeAction(cmd, coroutineScope, onStart = { setExecuting(true) }) { result ->
                    setExecuting(false)
                    showResult(context, shelfLabel, result)
                }
            }
        )

        val discoverLabel = context.getString(R.string.tools_discover_title)
        ActionCard(
            title = discoverLabel,
            description = stringResource(R.string.tools_discover_desc),
            isExecuting = isExecuting,
            onClick = {
                val cmd = "settings put secure assistant_screen_type 2 && settings put secure assistant_screen_type_left_enable 1"
                executeAction(cmd, coroutineScope, onStart = { setExecuting(true) }) { result ->
                    setExecuting(false)
                    showResult(context, discoverLabel, result)
                }
            }
        )

        val disableLabel = context.getString(R.string.tools_disable_title)
        ActionCard(
            title = disableLabel,
            description = stringResource(R.string.tools_disable_desc),
            isExecuting = isExecuting,
            onClick = {
                val cmd = "settings put secure assistant_screen_type 0 && settings put secure assistant_screen_type_left_enable 0"
                executeAction(cmd, coroutineScope, onStart = { setExecuting(true) }) { result ->
                    setExecuting(false)
                    showResult(context, disableLabel, result)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.tools_header_system_apps),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        val smsLabel = context.getString(R.string.tools_sms_title)
        ActionCard(
            title = smsLabel,
            description = stringResource(R.string.tools_sms_desc),
            isExecuting = isExecuting,
            onClick = {
                val cmd = "pm grant com.android.mms android.permission.READ_PRIVILEGED_PHONE_STATE"
                executeAction(cmd, coroutineScope, onStart = { setExecuting(true) }) { result ->
                    setExecuting(false)
                    showResult(context, smsLabel, result)
                }
            }
        )

        val installSmsLabel = context.getString(R.string.tools_install_sms_title)
        ActionCard(
            title = installSmsLabel,
            description = stringResource(R.string.tools_install_sms_desc),
            isExecuting = isExecuting,
            onClick = {
                setExecuting(true)
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        AssetUtils.copyAssetsToFilesDir(context, listOf("sms_app.apk"))
                        val apkPath = "${context.filesDir.absolutePath}/sms_app.apk"
                        val result = RootExecutor.execute("/system/bin/pm install -r \"$apkPath\"")

                        withContext(Dispatchers.Main) {
                            setExecuting(false)
                            if (result.success) {
                                Toast.makeText(context, context.getString(R.string.toast_success, installSmsLabel), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.toast_failed, installSmsLabel, result.error), Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            setExecuting(false)
                            Toast.makeText(context, context.getString(R.string.error, e.message), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SystemTab(
    context: Context,
    isExecuting: Boolean,
    setExecuting: (Boolean) -> Unit,
    onArbResult: (Pair<Boolean, String>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showAntiArb by remember { mutableStateOf(false) }
    var showWidevine by remember { mutableStateOf(false) }
    var showShizukuUnavailable by remember { mutableStateOf(false) }

    if (showShizukuUnavailable) {
        AlertDialog(
            onDismissRequest = { showShizukuUnavailable = false },
            title = { Text(stringResource(R.string.shizuku_unavailable_title), color = MaterialTheme.colorScheme.error) },
            text = { Text(stringResource(R.string.shizuku_unavailable_desc)) },
            confirmButton = {
                TextButton(onClick = { showShizukuUnavailable = false }) { Text(stringResource(R.string.ok)) }
            }
        )
    }

    if (showAntiArb) {
        AntiArbDialog(
            onConfirm = {
                showAntiArb = false
                setExecuting(true)
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val cmd = """
                            SLOT=${'$'}(getprop ro.boot.slot_suffix 2>/dev/null); 
                            if [ "${'$'}SLOT" = "_a" ]; then INACTIVE="_b"; else INACTIVE="_a"; fi; 
                            MD5_A=${'$'}(md5sum /dev/block/by-name/xbl_config${'$'}SLOT | cut -d' ' -f1); 
                            MD5_B=${'$'}(md5sum /dev/block/by-name/xbl_config${'$'}INACTIVE | cut -d' ' -f1); 
                            if [ "${'$'}MD5_A" = "${'$'}MD5_B" ]; then 
                              echo "SAME"; 
                            else 
                              dd if=/dev/block/by-name/xbl_config${'$'}SLOT of=/dev/block/by-name/xbl_config${'$'}INACTIVE bs=4096 >/dev/null 2>&1; 
                              echo "FLASHED"; 
                            fi
                        """.trimIndent().replace("\n", " ").replace("\r", "")

                        val result = RootExecutor.execute(cmd)

                        withContext(Dispatchers.Main) {
                            setExecuting(false)
                            if (result.success) {
                                if (result.output.contains("SAME")) {
                                    Toast.makeText(context, context.getString(R.string.anti_arb_success_same), Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.anti_arb_success_flashed), Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.anti_arb_failed, result.error), Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            setExecuting(false)
                            Toast.makeText(context, context.getString(R.string.error, e.message), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            onDismiss = { showAntiArb = false }
        )
    }

    if (showWidevine) {
        WidevineDialog(
            onConfirm = {
                showWidevine = false
                setExecuting(true)
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        // Step 1: copy attestation from assets to app filesDir
                        AssetUtils.copyAssetsToFilesDir(context, listOf("attestation"))
                        val srcPath = "${context.filesDir.absolutePath}/attestation"

                        // Step 2: push to /data/local/tmp via root cp
                        val pushResult = RootExecutor.execute("cp \"$srcPath\" /data/local/tmp/attestation")
                        if (!pushResult.success) {
                            withContext(Dispatchers.Main) {
                                setExecuting(false)
                                Toast.makeText(context, context.getString(R.string.widevine_failed, pushResult.error), Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }

                        // Step 3: run KmInstallKeybox
                        val installCmd = "LD_LIBRARY_PATH=/vendor/lib64/hw KmInstallKeybox /data/local/tmp/attestation attestation true"
                        val installResult = RootExecutor.execute(installCmd)

                        // Step 4: always clean up temp file
                        RootExecutor.execute("rm /data/local/tmp/attestation")

                        withContext(Dispatchers.Main) {
                            setExecuting(false)
                            if (installResult.success && installResult.output.contains("InstallKeybox is done", ignoreCase = true)) {
                                Toast.makeText(context, context.getString(R.string.widevine_success), Toast.LENGTH_LONG).show()
                            } else {
                                val err = if (installResult.output.isNotEmpty()) installResult.output else installResult.error
                                Toast.makeText(context, context.getString(R.string.widevine_failed, err), Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            setExecuting(false)
                            Toast.makeText(context, context.getString(R.string.error, e.message), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            onDismiss = { showWidevine = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.system_header_diagnostics),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        val arbLabel = context.getString(R.string.system_arb_status_title)
        ActionCard(
            title = arbLabel,
            description = stringResource(R.string.system_arb_status_desc),
            isExecuting = isExecuting,
            onClick = {
                if (RootExecutor.isShizukuMode) {
                    showShizukuUnavailable = true
                    return@ActionCard
                }
                setExecuting(true)
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        AssetUtils.copyAssetsToFilesDir(context, listOf("run-it-eng.sh", "arbscan"))
                        val scriptPath = "${context.filesDir.absolutePath}/run-it-eng.sh"
                        val arbScanPath = "${context.filesDir.absolutePath}/arbscan"
                        
                        val command = "chmod +x $scriptPath && chmod +x $arbScanPath && su -c $scriptPath"
                        val result = RootExecutor.execute(command)

                        withContext(Dispatchers.Main) {
                            setExecuting(false)
                            if (result.success || result.output.isNotEmpty()) {
                                val output = result.output
                                val tripped = output.contains("Anti-Rollback Enabled") || output.contains("ARB Index: 1")
                                onArbResult(Pair(tripped, output))
                            } else {
                                Toast.makeText(context, context.getString(R.string.arb_check_failed, result.error), Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            setExecuting(false)
                            Toast.makeText(context, context.getString(R.string.error, e.message), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )

        ActionCard(
            title = stringResource(R.string.system_anti_arb_title),
            description = stringResource(R.string.system_anti_arb_desc),
            isExecuting = isExecuting,
            onClick = {
                if (RootExecutor.isShizukuMode) {
                    showShizukuUnavailable = true
                    return@ActionCard
                }
                showAntiArb = true
            }
        )

        ActionCard(
            title = stringResource(R.string.system_widevine_title),
            description = stringResource(R.string.system_widevine_desc),
            isExecuting = isExecuting,
            onClick = {
                if (RootExecutor.isShizukuMode) {
                    showShizukuUnavailable = true
                    return@ActionCard
                }
                showWidevine = true
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AntiArbDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableStateOf(10) }
    
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.anti_arb_dialog_title), color = MaterialTheme.colorScheme.error) },
        text = { 
            Text(
                text = stringResource(R.string.anti_arb_dialog_text),
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = countdown == 0
            ) {
                Text(if (countdown > 0) stringResource(R.string.confirm_countdown, countdown) else stringResource(R.string.confirm), color = if (countdown == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun WidevineDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableStateOf(10) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.widevine_dialog_title), color = MaterialTheme.colorScheme.error) },
        text = {
            Text(
                text = stringResource(R.string.widevine_dialog_text),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = countdown == 0
            ) {
                Text(
                    text = if (countdown > 0) stringResource(R.string.confirm_countdown, countdown) else stringResource(R.string.confirm),
                    color = if (countdown == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun ArbResultScreen(isTripped: Boolean, rawOutput: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = if (isTripped) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isTripped) stringResource(R.string.arb_result_tripped) else stringResource(R.string.arb_result_not_tripped),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isTripped) stringResource(R.string.arb_desc_tripped) else stringResource(R.string.arb_desc_not_tripped),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text(
                text = rawOutput,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun DeviceInfoTab() {
    val kernelVersion = remember {
        try {
            java.io.File("/proc/version").readText().trim().take(120)
        } catch (e: Exception) {
            "N/A"
        }
    }
    val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else "N/A"
    val socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MANUFACTURER else "N/A"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.device_info_header_device),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DeviceInfoRow(label = stringResource(R.string.device_info_model), value = Build.MODEL)
                DeviceInfoRow(label = stringResource(R.string.device_info_brand), value = Build.BRAND)
                DeviceInfoRow(label = stringResource(R.string.device_info_manufacturer), value = Build.MANUFACTURER)
                DeviceInfoRow(label = stringResource(R.string.device_info_board), value = Build.BOARD)
                DeviceInfoRow(label = stringResource(R.string.device_info_abis), value = Build.SUPPORTED_ABIS.joinToString(", "))
                DeviceInfoRow(label = stringResource(R.string.device_info_soc_model), value = socModel)
                DeviceInfoRow(label = stringResource(R.string.device_info_soc_manufacturer), value = socManufacturer)
            }
        }

        Text(
            text = stringResource(R.string.device_info_header_software),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DeviceInfoRow(label = stringResource(R.string.device_info_android_version), value = Build.VERSION.RELEASE)
                DeviceInfoRow(label = stringResource(R.string.device_info_api_level), value = Build.VERSION.SDK_INT.toString())
                DeviceInfoRow(label = stringResource(R.string.device_info_security_patch), value = Build.VERSION.SECURITY_PATCH)
                DeviceInfoRow(label = stringResource(R.string.device_info_build_number), value = Build.ID)
                DeviceInfoRow(label = stringResource(R.string.device_info_kernel), value = kernelVersion)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DeviceInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AboutTab(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        AboutCard(context)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    isExecuting: Boolean,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = !isExecuting, onClick = onClick)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

private fun executeAction(
    command: String,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onStart: () -> Unit,
    onResult: (RootExecutor.RootResult) -> Unit
) {
    onStart()
    coroutineScope.launch {
        val result = RootExecutor.execute(command)
        onResult(result)
    }
}

private fun showResult(context: android.content.Context, actionName: String, result: RootExecutor.RootResult) {
    if (result.success) {
        Toast.makeText(context, context.getString(R.string.toast_success, actionName), Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, context.getString(R.string.toast_failed, actionName, result.error), Toast.LENGTH_LONG).show()
    }
}

@Composable
fun AboutCard(context: android.content.Context) {
    var showAck by remember { mutableStateOf(false) }

    if (showAck) {
        AlertDialog(
            onDismissRequest = { showAck = false },
            title = { Text(stringResource(R.string.about_acknowledgements), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(R.string.about_ack_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = stringResource(R.string.about_ack_widevine), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAck = false }) { Text(stringResource(R.string.ok)) }
            }
        )
    }
    var version = "1.0"
    var buildTime = "Unknown"
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        version = pInfo.versionName ?: "1.0"
        buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(pInfo.lastUpdateTime))
    } catch (e: Exception) {
        // Ignore
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(androidx.compose.ui.graphics.Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.about_version, version),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.about_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(text = stringResource(R.string.about_author), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = stringResource(R.string.about_platform, Build.SUPPORTED_ABIS.joinToString()), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = stringResource(R.string.about_build_time, buildTime), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = stringResource(R.string.about_target_sdk, Build.VERSION.SDK_INT), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // GitHub link
            val githubUrl = "https://github.com/EraserCN/OxygenToolbox"
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                context.startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_github),
                    contentDescription = "GitHub",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GitHub",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showAck = true }
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.about_acknowledgements),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun isOplusDevice(): Boolean {
    val brand = Build.BRAND.lowercase()
    val manufacturer = Build.MANUFACTURER.lowercase()
    val oplusKeywords = listOf("oppo", "oneplus", "realme", "oplus")
    return oplusKeywords.any { brand.contains(it) || manufacturer.contains(it) }
}

/**
 * Returns true if the device is running OxygenOS, ColorOS, or RealmeUI.
 * Detection is done by reading known system properties via reflection.
 */
private fun isOplusFirmware(): Boolean {
    fun getProp(key: String): String = try {
        val c = Class.forName("android.os.SystemProperties")
        c.getMethod("get", String::class.java, String::class.java)
            .invoke(null, key, "") as? String ?: ""
    } catch (_: Exception) { "" }

    // OxygenOS
    if (getProp("ro.oxygen.version").isNotEmpty()) return true
    if (getProp("ro.build.version.oplusrom.display").isNotEmpty()) return true
    // ColorOS
    if (getProp("ro.coloros.version").isNotEmpty()) return true
    if (getProp("ro.build.version.oplusrom").isNotEmpty()) return true
    // RealmeUI
    if (getProp("ro.build.version.realmeui").isNotEmpty()) return true
    // Generic OPlus ROM marker
    if (getProp("ro.build.oplus_display_name").isNotEmpty()) return true
    if (getProp("ro.vendor.oplus.market.name").isNotEmpty()) return true

    return false
}

@Composable
fun AuthScreen(onRootGranted: () -> Unit, onShizukuGranted: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    /* SHIZUKU_DISABLED: Shizuku 授权监听器已屏蔽
    DisposableEffect(Unit) {
        val listener = rikka.shizuku.Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == 101 && grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                onShizukuGranted()
            } else {
                Toast.makeText(context, context.getString(R.string.shizuku_auth_failed), Toast.LENGTH_SHORT).show()
            }
        }
        rikka.shizuku.Shizuku.addRequestPermissionResultListener(listener)
        onDispose {
            rikka.shizuku.Shizuku.removeRequestPermissionResultListener(listener)
        }
    }
    */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Build,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.auth_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.auth_btn_root), // SHIZUKU_DISABLED: 只显示 Root 提示
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                coroutineScope.launch {
                    if (RootExecutor.checkRoot()) {
                        onRootGranted()
                    } else {
                        Toast.makeText(context, context.getString(R.string.root_required_text), Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(stringResource(R.string.auth_btn_root))
        }
        
        /* SHIZUKU_DISABLED: Shizuku 授权按钮已屏蔽
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = {
                try {
                    if (!rikka.shizuku.Shizuku.pingBinder()) {
                        Toast.makeText(context, context.getString(R.string.shizuku_not_running), Toast.LENGTH_LONG).show()
                        val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                        if (intent != null) {
                            context.startActivity(intent)
                        }
                    } else {
                        if (rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            onShizukuGranted()
                        } else {
                            rikka.shizuku.Shizuku.requestPermission(101)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(stringResource(R.string.auth_btn_shizuku))
        }
        */
    }
}
