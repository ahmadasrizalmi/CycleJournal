package com.app.cyclejournal.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val syncState by viewModel.syncState.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showNukeDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var nukeConfirmationText by remember { mutableStateOf("") }

    val lastSync = viewModel.getLastSyncTimestamp()
    val lastSyncText = if (lastSync > 0) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        stringResource(R.string.last_sync_format, sdf.format(Date(lastSync)))
    } else {
        stringResource(R.string.last_sync_never)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_settings),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        containerColor = BackgroundWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Anonymous Patient ID Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
                color = SurfaceSubtle
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.anonymous_id_label),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.anonymousUserId,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryPink
                        )
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(viewModel.anonymousUserId))
                        }) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Salin ID", tint = TextSecondary)
                        }
                    }
                }
            }

            // 2. Cloudflare Zero-Knowledge Backup Card
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.section_backup),
                    style = MaterialTheme.typography.titleMedium
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
                    color = BackgroundWhite
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.CloudQueue, contentDescription = null, tint = PrimaryCoral)
                            Text(stringResource(R.string.d1_status_connected), style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(lastSyncText, style = MaterialTheme.typography.labelSmall)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    pinInput = ""
                                    showBackupDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(stringResource(R.string.action_backup_now), fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    pinInput = ""
                                    showRestoreDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(stringResource(R.string.action_restore_backup), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Sync Status Snackbar / Banner
            when (val state = syncState) {
                is SyncState.InProgress -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryPink)
                }
                is SyncState.Success -> {
                    Text(state.message, color = Color(0xFF16A34A), fontSize = 13.sp)
                }
                is SyncState.Error -> {
                    Text(state.message, color = AlertText, fontSize = 13.sp)
                }
                else -> {}
            }

            // 3. Danger Zone (Nuke All Data)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.section_danger_zone),
                    style = MaterialTheme.typography.titleMedium.copy(color = AlertText)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, AlertBorder, RoundedCornerShape(16.dp)),
                    color = AlertBg
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.danger_zone_desc),
                            fontSize = 13.sp,
                            color = AlertText,
                            lineHeight = 18.sp
                        )
                        Button(
                            onClick = {
                                nukeConfirmationText = ""
                                showNukeDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertBorder),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.action_nuke_data), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Manual Backup PIN Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Verifikasi PIN Cadangan") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4) pinInput = it },
                    label = { Text("PIN 4-Digit") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBackupDialog = false
                        viewModel.performManualBackup(pinInput)
                    },
                    enabled = pinInput.length == 4
                ) {
                    Text("Cadangkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) { Text("Batal") }
            }
        )
    }

    // Manual Restore PIN Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Pulihkan Cadangan Cloud") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Masukkan PIN yang Anda gunakan saat membuat cadangan.")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("PIN 4-Digit") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreDialog = false
                        viewModel.performManualRestore(pinInput)
                    },
                    enabled = pinInput.length == 4
                ) {
                    Text("Pulihkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("Batal") }
            }
        )
    }

    // Nuke All Data Dialog
    if (showNukeDialog) {
        AlertDialog(
            onDismissRequest = { showNukeDialog = false },
            title = { Text(stringResource(R.string.nuke_confirm_title), color = AlertText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.nuke_confirm_message), fontSize = 13.sp)
                    OutlinedTextField(
                        value = nukeConfirmationText,
                        onValueChange = { nukeConfirmationText = it },
                        placeholder = { Text("Ketik HAPUS") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNukeDialog = false
                        viewModel.wipeAllUserData {
                            android.os.Process.killProcess(android.os.Process.myPid())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertBorder),
                    enabled = nukeConfirmationText.trim() == "HAPUS"
                ) {
                    Text(stringResource(R.string.nuke_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNukeDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
