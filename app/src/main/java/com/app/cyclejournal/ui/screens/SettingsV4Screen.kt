package com.app.cyclejournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cyclejournal.R
import com.app.cyclejournal.ui.components.AppSwitch
import com.app.cyclejournal.ui.components.HairLine
import com.app.cyclejournal.ui.components.HeroCtaRow
import com.app.cyclejournal.ui.components.IconTile
import com.app.cyclejournal.ui.components.SectionLabel
import com.app.cyclejournal.ui.components.SettingTile
import com.app.cyclejournal.ui.components.SettingsListRow
import com.app.cyclejournal.ui.components.SoftCard
import com.app.cyclejournal.ui.theme.AlertBrown
import com.app.cyclejournal.ui.theme.AppType
import com.app.cyclejournal.ui.theme.BrandEnd
import com.app.cyclejournal.ui.theme.BrandGradient
import com.app.cyclejournal.ui.theme.BrandTint
import com.app.cyclejournal.ui.theme.CanvasSoft
import com.app.cyclejournal.ui.theme.Dimens
import com.app.cyclejournal.ui.theme.HeroGradient
import com.app.cyclejournal.ui.theme.Ink
import com.app.cyclejournal.ui.theme.Ink2
import com.app.cyclejournal.ui.theme.Ink3
import com.app.cyclejournal.ui.theme.Lavender
import com.app.cyclejournal.ui.theme.LavenderInk
import com.app.cyclejournal.ui.theme.Line
import com.app.cyclejournal.ui.theme.OnBrand
import com.app.cyclejournal.ui.theme.Paper
import com.app.cyclejournal.ui.theme.RoseMuted
import com.app.cyclejournal.ui.theme.ShadowHero
import com.app.cyclejournal.ui.theme.Teal
import com.app.cyclejournal.ui.theme.TealTint
import kotlin.math.abs

private val TEXT_SCALE_STEPS = listOf(
    0.90f to R.string.settings_text_size_small,
    1.00f to R.string.settings_text_size_normal,
    1.10f to R.string.settings_text_size_large,
    1.20f to R.string.settings_text_size_extra
)

private val NUKE_WORDS = listOf("HAPUS", "DELETE")

/**
 * Settings — CycleJournal v4 design (`Setelan · ID v4` / `Settings · EN v4`).
 * Keeps the legacy SettingsScreenView contract so the app wiring stays untouched.
 */
@Composable
fun SettingsV4Screen(
    isDarkMode: Boolean,
    isDiscreet: Boolean,
    isPro: Boolean,
    appLanguage: String = "system",
    onLanguageChanged: (String) -> Unit = {},
    appTextScale: Float = 1f,
    onTextScaleChanged: (Float) -> Unit = {},
    isPromilMode: Boolean = false,
    onTogglePromilMode: (Boolean) -> Unit = {},
    isPinConfigured: Boolean,
    anonymousRecoveryKey: String,
    onToggleDiscreet: () -> Unit,
    onToggleDark: () -> Unit,
    isFingerprintUnlockEnabled: Boolean = false,
    onFingerprintUnlockChanged: (Boolean) -> Unit = {},
    isPeriodReminderEnabled: Boolean = true,
    onPeriodReminderChanged: (Boolean) -> Unit = {},
    isBbtReminderEnabled: Boolean = true,
    onBbtReminderChanged: (Boolean) -> Unit = {},
    appVersionName: String = "",
    onOpenPin: () -> Unit,
    onBuyPro: () -> Unit,
    onCopyRecoveryKey: (() -> Unit)? = null,
    onBackupLocal: ((Boolean, String?) -> Unit)? = null,
    onRestoreLocal: (() -> Unit)? = null,
    onNukeData: (String) -> Boolean,
    onToast: (String) -> Unit
) {
    var isLanguageDialogOpen by remember { mutableStateOf(false) }
    var isBackupDialogOpen by remember { mutableStateOf(false) }
    var isBackupEncrypted by remember { mutableStateOf(false) }
    var backupPinInput by remember { mutableStateOf("") }
    var isGuideExpanded by remember { mutableStateOf(false) }
    var isNukeOpen by remember { mutableStateOf(false) }
    var nukeKeyword by remember { mutableStateOf("") }
    var nukePin by remember { mutableStateOf("") }
    var isNukePinWrong by remember { mutableStateOf(false) }

    // Hoisted so click handlers don't call @Composable stringResource().
    val promilTitle = stringResource(R.string.v4_promil_title)
    val promilSubtitle = stringResource(R.string.v4_promil_subtitle)
    val promilEnable = stringResource(R.string.v4_promil_enable)
    val promilActive = stringResource(R.string.v4_promil_active)
    val promilOnToast = stringResource(R.string.v4_promil_on_toast)
    val promilOffToast = stringResource(R.string.v4_promil_off_toast)
    val reminderOnToast = stringResource(R.string.v4_reminder_on_toast)
    val reminderOffToast = stringResource(R.string.v4_reminder_off_toast)
    val fingerprintOnToast = stringResource(R.string.settings_biometric_enabled)
    val fingerprintOffToast = stringResource(R.string.settings_biometric_disabled)
    val bbtReminderOnToast = stringResource(R.string.settings_bbt_reminder) + " · " + stringResource(R.string.v4_promil_active)
    val bbtReminderOffToast = stringResource(R.string.settings_bbt_reminder) + " · " + stringResource(R.string.settings_cancel)
    val backupToast = stringResource(R.string.v4_backup_toast)
    val restoreToast = stringResource(R.string.v4_restore_toast)
    val wipeToast = stringResource(R.string.nuke_wiping_title)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding,
            top = 14.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionGap)
    ) {
        // ---------------------------------------------------------- profile --
        item {
            ProfileCardV4(
                isPro = isPro,
                anonymousRecoveryKey = anonymousRecoveryKey,
                onBuyPro = onBuyPro,
                onCopyRecoveryKey = onCopyRecoveryKey
            )
        }

        // ------------------------------------------------------- promil cta --
        item {
            HeroCtaRow(
                title = promilTitle,
                subtitle = promilSubtitle,
                actionLabel = if (isPromilMode) promilActive else promilEnable,
                onAction = {
                    onTogglePromilMode(!isPromilMode)
                    onToast(if (isPromilMode) promilOffToast else promilOnToast)
                }
            )
        }

        // ------------------------------------------------- guide / faq card ---
        item { SectionLabel(stringResource(R.string.v4_section_guide)) }
        item {
            // One card, not two tiles: both entry points held the same Q&A set.
            GuideAccordionCard(
                isExpanded = isGuideExpanded,
                onToggle = { isGuideExpanded = !isGuideExpanded }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SettingTile(
                    title = stringResource(R.string.v4_tile_discreet),
                    subtitle = stringResource(R.string.v4_tile_discreet_sub),
                    icon = Icons.Rounded.VisibilityOff,
                    tint = LavenderInk,
                    background = Lavender,
                    modifier = Modifier.weight(1f),
                    trailing = { AppSwitch(checked = isDiscreet, onCheckedChange = { onToggleDiscreet() }) }
                )
                SettingTile(
                    title = stringResource(R.string.v4_tile_reminder),
                    subtitle = stringResource(R.string.v4_tile_reminder_sub),
                    icon = Icons.Rounded.NotificationsActive,
                    tint = BrandEnd,
                    background = BrandTint,
                    modifier = Modifier.weight(1f),
                    trailing = {
                        AppSwitch(checked = isPeriodReminderEnabled, onCheckedChange = {
                            onPeriodReminderChanged(it)
                            onToast(if (it) reminderOnToast else reminderOffToast)
                        })
                    }
                )
            }
        }

        // ---------------------------------------------------------- privacy --
        item {
            SoftCard(modifier = Modifier.fillMaxWidth(), radius = Dimens.CardRadius, fill = Paper, border = Line) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    SettingsListRow(
                        title = stringResource(R.string.v4_pin_lock),
                        icon = Icons.Rounded.Lock,
                        onClick = onOpenPin,
                        value = stringResource(if (isPinConfigured) R.string.v4_pin_change else R.string.v4_pin_set),
                        valueColor = BrandEnd
                    )
                    HairLine()
                    SettingsListRow(
                        title = stringResource(R.string.v4_fingerprint),
                        icon = Icons.Rounded.Fingerprint,
                        trailing = {
                            AppSwitch(checked = isFingerprintUnlockEnabled, onCheckedChange = {
                                onFingerprintUnlockChanged(it)
                                onToast(if (it) fingerprintOnToast else fingerprintOffToast)
                            })
                        }
                    )
                    HairLine()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BackupPill(
                            label = stringResource(R.string.v4_backup),
                            icon = Icons.Rounded.SaveAlt,
                            tint = BrandEnd,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (onBackupLocal != null) {
                                isBackupEncrypted = false
                                backupPinInput = ""
                                isBackupDialogOpen = true
                            } else {
                                onToast(backupToast)
                            }
                        }
                        BackupPill(
                            label = stringResource(R.string.v4_restore),
                            icon = Icons.Rounded.Restore,
                            tint = Teal,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (onRestoreLocal != null) onRestoreLocal.invoke() else onToast(restoreToast)
                        }
                    }
                    HairLine()
                    SettingsListRow(
                        title = stringResource(R.string.v4_delete_all),
                        icon = Icons.Rounded.Delete,
                        titleColor = AlertBrown,
                        iconTint = AlertBrown,
                        showChevron = true,
                        onClick = {
                            nukeKeyword = ""
                            nukePin = ""
                            isNukePinWrong = false
                            isNukeOpen = true
                        }
                    )
                }
            }
        }

        // ------------------------------------------------------ application --
        item { SectionLabel(stringResource(R.string.v4_section_app)) }
        item {
            SoftCard(modifier = Modifier.fillMaxWidth(), radius = Dimens.CardRadius, fill = Paper, border = Line) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    SettingsListRow(
                        title = stringResource(R.string.v4_row_language),
                        icon = Icons.Filled.Language,
                        value = languageLabel(appLanguage),
                        showChevron = true,
                        onClick = { isLanguageDialogOpen = true },
                        modifier = Modifier.testTag("settings_language")
                    )
                    HairLine()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("settings_text_size"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Label and hint stack: side by side, the hint squeezed the title into a
                        // one-letter-per-line column at the largest text size.
                        Text(
                            text = stringResource(R.string.v4_row_text_size),
                            style = AppType.rowTitle.copy(fontWeight = FontWeight.Medium),
                            color = Ink,
                            maxLines = 1
                        )
                        Text(stringResource(R.string.settings_text_size_hint), style = AppType.micro, color = Ink3)
                        // Word labels ("Small/Large/XL") read as copy and truncate at the top steps;
                        // four progressively bigger "A" glyphs say the same thing in any language.
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            TEXT_SCALE_STEPS.forEach { (scale, labelRes) ->
                                TextSizePill(
                                    scale = scale,
                                    label = stringResource(labelRes),
                                    selected = abs(appTextScale - scale) < 0.01f,
                                    onClick = { onTextScaleChanged(scale) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    HairLine()
                    SettingsListRow(
                        title = stringResource(R.string.v4_theme_dark),
                        icon = Icons.Rounded.DarkMode,
                        trailing = { AppSwitch(checked = isDarkMode, onCheckedChange = { onToggleDark() }) }
                    )
                    HairLine()
                    SettingsListRow(
                        title = stringResource(R.string.settings_bbt_reminder),
                        icon = Icons.Rounded.Thermostat,
                        trailing = {
                            AppSwitch(checked = isBbtReminderEnabled, onCheckedChange = {
                                onBbtReminderChanged(it)
                                onToast(if (it) bbtReminderOnToast else bbtReminderOffToast)
                            })
                        }
                    )
                    HairLine()
                    // The 30 second guard is enforced in MainActivity; 1.1.3 showed it as information.
                    SettingsListRow(
                        title = stringResource(R.string.settings_auto_lock),
                        icon = Icons.Rounded.LockClock,
                        value = stringResource(R.string.settings_auto_lock_30_seconds)
                    )
                }
            }
        }

        item { PrivacyFooter() }
        item {
            Text(
                text = stringResource(R.string.app_version_footer, appVersionName),
                style = AppType.caption,
                color = Ink3,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (isLanguageDialogOpen) {
        LanguageDialog(
            appLanguage = appLanguage,
            onPick = {
                onLanguageChanged(it)
                isLanguageDialogOpen = false
            },
            onDismiss = { isLanguageDialogOpen = false }
        )
    }

    if (isBackupDialogOpen) {
        AlertDialog(
            onDismissRequest = { isBackupDialogOpen = false },
            title = {
                Text(stringResource(R.string.settings_backup_data_self_managed), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.settings_backup_lock_method_prompt), style = AppType.body.copy(fontSize = 14.sp), color = Ink2)
                    BackupLockOption(
                        title = stringResource(R.string.settings_backup_lock_standard),
                        description = stringResource(R.string.settings_backup_lock_standard_description),
                        selected = !isBackupEncrypted,
                        onClick = { isBackupEncrypted = false }
                    )
                    BackupLockOption(
                        title = stringResource(R.string.settings_backup_lock_encrypted),
                        description = stringResource(R.string.settings_backup_lock_encrypted_description),
                        selected = isBackupEncrypted,
                        onClick = { isBackupEncrypted = true }
                    )
                    if (isBackupEncrypted) {
                        OutlinedTextField(
                            value = backupPinInput,
                            onValueChange = { input -> backupPinInput = input.filter { it.isDigit() }.take(4) },
                            label = { Text(stringResource(R.string.restore_pin_placeholder), fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isBackupEncrypted || backupPinInput.length == 4,
                    onClick = {
                        isBackupDialogOpen = false
                        onBackupLocal?.invoke(isBackupEncrypted, backupPinInput.takeIf { isBackupEncrypted })
                    }
                ) { Text(stringResource(R.string.settings_create_backup), fontWeight = FontWeight.Bold, color = BrandEnd) }
            },
            dismissButton = {
                TextButton(onClick = { isBackupDialogOpen = false }) { Text(stringResource(R.string.settings_cancel)) }
            }
        )
    }

    if (isNukeOpen) {
        val keywordOk = NUKE_WORDS.any { it.equals(nukeKeyword.trim(), ignoreCase = true) }
        val canConfirm = keywordOk && (!isPinConfigured || nukePin.length == 4)
        AlertDialog(
            onDismissRequest = { isNukeOpen = false },
            title = { Text(stringResource(R.string.nuke_confirm_title), fontWeight = FontWeight.Bold, color = AlertBrown) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.nuke_confirm_message), style = AppType.body, color = Ink)
                    OutlinedTextField(
                        value = nukeKeyword,
                        onValueChange = { nukeKeyword = it },
                        label = { Text(stringResource(R.string.nuke_confirm_hint), fontSize = 13.sp) },
                        singleLine = true
                    )
                    if (isPinConfigured) {
                        OutlinedTextField(
                            value = nukePin,
                            onValueChange = { input -> nukePin = input.filter { it.isDigit() }.take(4) },
                            label = { Text(stringResource(R.string.nuke_pin_prompt), fontSize = 13.sp) },
                            isError = isNukePinWrong,
                            singleLine = true
                        )
                        if (isNukePinWrong) {
                            Text(stringResource(R.string.nuke_wrong_pin), style = AppType.micro, color = AlertBrown)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = canConfirm,
                    onClick = {
                        if (onNukeData(nukePin)) {
                            isNukeOpen = false
                            onToast(wipeToast)
                        } else {
                            isNukePinWrong = true
                        }
                    }
                ) { Text(stringResource(R.string.nuke_confirm_action), fontWeight = FontWeight.Bold, color = AlertBrown) }
            },
            dismissButton = {
                TextButton(onClick = { isNukeOpen = false }) { Text(stringResource(R.string.app_close)) }
            }
        )
    }
}

private val TextSizeGlyphStyle = TextStyle(fontFamily = AppType.Display, fontWeight = FontWeight.Bold)

/** One step of the text-size control: a pill carrying an "A" that grows with the step. */
@Composable
private fun TextSizePill(
    scale: Float,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glyphSize = (11f + (scale - 0.85f) * 26f).coerceIn(12f, 24f)
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(CircleShape)
            .background(if (selected) BrandTint else Paper)
            .border(1.dp, if (selected) BrandEnd else Line, CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "A",
            style = TextSizeGlyphStyle.copy(fontSize = glyphSize.sp),
            color = if (selected) BrandEnd else Ink2
        )
    }
}

/**
 * Quick guide + FAQ as one accordion card.
 *
 * The v4 design put them in two tiles, but the app ships a single Q&A set (`settings_faq_1..4`), so
 * two entry points into identical content was noise - this is the legacy accordion, restyled.
 */
@Composable
private fun GuideAccordionCard(isExpanded: Boolean, onToggle: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth(), radius = Dimens.CardRadius, fill = Paper, border = Line) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconTile(
                    icon = Icons.Rounded.Book,
                    tint = Teal,
                    background = TealTint,
                    size = 34.dp,
                    corner = 11.dp,
                    iconSize = 18.dp
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.settings_quick_guide_faq),
                        style = AppType.cardTitle.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink
                    )
                    Text(stringResource(R.string.settings_quick_guide_subtitle), style = AppType.micro, color = Ink3)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = Ink3,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (isExpanded) {
                HairLine()
                val items = listOf(
                    stringResource(R.string.settings_faq_1_title) to
                        stringResource(R.string.settings_faq_1_body_prefix) + stringResource(R.string.settings_faq_1_body_suffix),
                    stringResource(R.string.settings_faq_2_title) to stringResource(R.string.settings_faq_2_body),
                    stringResource(R.string.settings_faq_3_title) to stringResource(R.string.settings_faq_3_body),
                    stringResource(R.string.settings_faq_4_title) to stringResource(R.string.settings_faq_4_body)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items.forEach { (title, body) ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(title, style = AppType.rowTitle.copy(fontWeight = FontWeight.Bold), color = BrandEnd)
                            Text(body, style = AppType.body.copy(fontSize = 12.sp), color = Ink2)
                        }
                    }
                }
            }
        }
    }
}

/** One selectable row of the backup lock dialog (standard vs PIN-encrypted). */
@Composable
private fun BackupLockOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) BrandEnd else Line, RoundedCornerShape(14.dp))
            .background(if (selected) BrandTint else Paper)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(1.5.dp, if (selected) BrandEnd else Line, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(BrandEnd)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = AppType.cardTitle.copy(fontWeight = FontWeight.Bold), color = Ink)
            Text(description, style = AppType.caption, color = Ink3)
        }
    }
}

@Composable
private fun ProfileCardV4(
    isPro: Boolean,
    anonymousRecoveryKey: String,
    onBuyPro: () -> Unit,
    onCopyRecoveryKey: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(Dimens.CardRadiusLg), ambientColor = ShadowHero, spotColor = ShadowHero)
            .clip(RoundedCornerShape(Dimens.CardRadiusLg))
            .background(HeroGradient)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(BrandGradient)
                .clickable { onCopyRecoveryKey?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(OnBrand)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.v4_profile_anonymous), style = AppType.cardTitle, color = Ink)
            Text(anonymousRecoveryKey, style = AppType.caption, color = RoseMuted, maxLines = 1)
            Text(
                text = stringResource(if (isPro) R.string.v4_profile_license_pro else R.string.v4_profile_license_free),
                style = AppType.caption.copy(fontWeight = FontWeight.SemiBold),
                color = BrandEnd
            )
        }
        Row(
            modifier = Modifier
                .height(30.dp)
                .clip(CircleShape)
                .background(BrandGradient)
                .clickable(onClick = onBuyPro)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = OnBrand, modifier = Modifier.size(13.dp))
            Text(
                text = stringResource(if (isPro) R.string.v4_profile_pro_active else R.string.v4_profile_go_pro),
                style = AppType.caption.copy(fontWeight = FontWeight.Bold),
                color = OnBrand,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BackupPill(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(CanvasSoft)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = AppType.caption.copy(fontWeight = FontWeight.Bold), color = Ink, maxLines = 1)
    }
}

@Composable
private fun PrivacyFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CanvasSoft)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Rounded.Lock, contentDescription = null, tint = Ink3, modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.v4_footer_privacy),
            style = AppType.caption,
            color = Ink3,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LanguageDialog(appLanguage: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(
        Triple("system", R.string.language_option_system, Icons.Filled.Language),
        Triple("id", R.string.language_option_id, Icons.Rounded.Spa),
        Triple("en", R.string.language_option_en, Icons.Rounded.AutoAwesome)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.section_language_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { (code, labelRes, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = BrandEnd, modifier = Modifier.size(18.dp))
                        Text(stringResource(labelRes), style = AppType.rowTitle.copy(fontWeight = FontWeight.Medium), color = Ink)
                        Spacer(Modifier.weight(1f))
                        if (appLanguage == code) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(BrandEnd)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.app_close)) }
        }
    )
}

@Composable
private fun languageLabel(code: String): String = when (code) {
    "id" -> stringResource(R.string.language_option_id)
    "en" -> stringResource(R.string.language_option_en)
    else -> stringResource(R.string.language_option_system)
}
