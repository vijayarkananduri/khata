package com.khata

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import com.khata.data.db.AccountEntity
import com.khata.data.db.TransactionEntity
import com.khata.data.backup.CsvTransactionCodec
import com.khata.data.updates.GitHubReleaseChecker
import com.khata.data.updates.GitHubUpdateResult
import com.khata.domain.stats.SpendPeriod
import com.khata.domain.stats.SpendPeriodRanges
import com.khata.data.prefs.UserSettings
import com.khata.domain.categorizer.KhataCategories
import com.khata.domain.quotes.InAppQuote
import com.khata.domain.quotes.QuoteRepository
import com.khata.notifications.NotificationHelper
import com.khata.notifications.NudgeCadence
import com.khata.ui.theme.*
import com.khata.widget.KhataHomeWidgetProvider
import com.khata.widget.KhataDailyWidgetProvider
import com.khata.widget.KhataWeeklyWidgetProvider
import com.khata.widget.KhataMonthlyWidgetProvider
import com.khata.widget.KhataAllTimeWidgetProvider
import com.khata.widget.KhataProgressRingWidgetProvider
import com.khata.widget.KhataQuoteWidgetProvider
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.widget.Toast
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun KhataRoot(viewModel: KhataViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val quoteRepo = rememberQuoteRepo()
    val splashQuote = remember(settings.language, settings.romanScript) {
        quoteRepo.splashText(settings.language, settings.romanScript)
    }
    KhataTheme(settings.themeMode) {
        val splashReduceMotion = shouldReduceMotion(settings.reduceMotion)
        var splash by remember { mutableStateOf(true) }
        LaunchedEffect(splashReduceMotion) { delay(if (splashReduceMotion) 450 else 1050); splash = false }
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Box(Modifier.fillMaxSize()) {
            DewBackdrop()
            if (!settings.onboardingComplete) {
                OnboardingScreen(onFinish = viewModel::finishOnboarding)
            } else {
                MainShell(viewModel, settings)
            }
            AnimatedVisibility(
                visible = splash && splashQuote.isNotBlank(),
                enter = if (splashReduceMotion) fadeIn(tween(0)) else fadeIn(tween(320)) + scaleIn(initialScale = .95f, animationSpec = tween(320)) +
                    slideInVertically(initialOffsetY = { it / 22 }, animationSpec = tween(320)),
                exit = fadeOut(tween(if (splashReduceMotion) 0 else 220))
            ) {
                Column(
                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DewSurface(modifier = Modifier.size(72.dp), shape = CircleShape, tone = MaterialTheme.colorScheme.primaryContainer, elevation = 15.dp) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("KHATA", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Spacer(Modifier.height(22.dp))
                    Text(
                        splashQuote, fontFamily = CaveatFamily, fontWeight = FontWeight.Bold,
                        fontSize = 30.sp, lineHeight = 38.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun OnboardingScreen(onFinish: (String, String, Boolean, Long) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var username by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf("te") }
    var roman by rememberSaveable { mutableStateOf(false) }
    var budget by rememberSaveable { mutableStateOf("") }
    val languages = listOf("hi" to "हिन्दी · Hindi", "te" to "తెలుగు · Telugu", "ta" to "தமிழ் · Tamil", "ml" to "മലയാളം · Malayalam", "kn" to "ಕನ್ನಡ · Kannada")
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("KHATA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(24.dp))
            Text("Your money, a little clearer.", fontFamily = CaveatFamily, fontSize = 26.sp, lineHeight = 34.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            Text("Every rupee, in its place.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            Spacer(Modifier.height(36.dp))
            when (step) {
                0 -> {
                    Text("First, what should we call you?", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(username, { username = it }, label = { Text("Name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                1 -> {
                    Text("Choose your language", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(10.dp))
                    languages.forEach { (code, label) ->
                        SelectableRow(label, selected = language == code) { language = code }
                    }
                }
                2 -> {
                    val langName = languages.first { it.first == language }.second.substringAfter("· ")
                    Text("You chose $langName.\nHow do you like to read it?", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                    SelectableRow("Native script", selected = !roman) { roman = false }
                    SelectableRow("English letters", selected = roman) { roman = true }
                }
                3 -> {
                    Text("Set a monthly budget", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Optional. You can compare month to month without one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(budget, { budget = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Monthly amount in rupees") }, prefix = { Text("₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                else -> {
                    Text("Your money stays on your device.", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                    Text("Khata works offline, with clear records and no bank credentials.", color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                }
            }
        }
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(5) { index -> Box(Modifier.size(if (index == step) 24.dp else 8.dp, 8.dp).clip(CircleShape).background(if (index == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)) }
            }
            Spacer(Modifier.height(16.dp))
            DewSurface(
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp),
                zIndex = 42f, tone = MaterialTheme.colorScheme.primaryContainer, elevation = 12.dp,
                onClick = { if (step < 4) step++ else onFinish(username, language, roman, ((budget.toDoubleOrNull() ?: 0.0) * 100).toLong()) }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (step == 4) "Open Khata" else "Continue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            if (step == 3) TextButton(onClick = { budget = ""; step = 4 }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Skip budget") }
        }
    }
    }
}

@Composable
private fun MainShell(viewModel: KhataViewModel, settings: UserSettings) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val queue by viewModel.reviewQueue.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf("Home") }
    var showReview by rememberSaveable { mutableStateOf(false) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    val context = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                if (!showReview && selectedCategory == null) {
                    DewSurface(
                        modifier = Modifier.size(62.dp), shape = CircleShape, zIndex = 80f,
                        tone = MaterialTheme.colorScheme.primaryContainer, elevation = 16.dp,
                        reduceMotion = shouldReduceMotion(settings.reduceMotion),
                        onClick = {
                            if (settings.hapticsEnabled) performKhataHaptic(context)
                            showAdd = true
                        }
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Add, contentDescription = "Add transaction", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                        }
                    }
                }
            },
            bottomBar = {
                if (!showReview && selectedCategory == null) KhataBottomBar(tab, shouldReduceMotion(settings.reduceMotion), settings.hapticsEnabled) { tab = it }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when {
                    showReview -> ReviewScreen(queue, categories.map { it.id to it.name }, shouldReduceMotion(settings.reduceMotion), onBack = { showReview = false }, onCorrect = viewModel::correct, onTransfer = viewModel::markTransfer, onIgnore = viewModel::ignore)
                    selectedCategory != null -> CategoryDetailScreen(selectedCategory!!, transactions, onBack = { selectedCategory = null }, onTransaction = { selectedTransaction = it })
                    tab == "Home" -> HomeScreen(
                        transactions = transactions, settings = settings, reviewCount = queue.size,
                        onReview = { showReview = true }, onSearch = { showSearch = true },
                        onTransaction = { selectedTransaction = it }
                    )
                    tab == "Calendar" -> CalendarScreen(transactions, onTransaction = { selectedTransaction = it })
                    tab == "Analysis" -> AnalysisScreen(transactions, categories.map { it.id to it.name }, onTransaction = { selectedTransaction = it }, onCategory = { selectedCategory = it })
                    else -> SettingsScreen(settings, viewModel, transactions, accounts, onClear = viewModel::clearAll)
                }
            }
        }
    }
    if (showAdd) QuickAddDialog(accounts, reduceMotion = shouldReduceMotion(settings.reduceMotion), onDismiss = { showAdd = false }, onSave = { merchant, amount, direction, date, note, category, accountId ->
        if (settings.hapticsEnabled) performKhataHaptic(context)
        viewModel.addTransaction(merchant, amount, direction, date, note, category, accountId)
        showAdd = false
    })
    if (showSearch) SearchOverlay(transactions, onClose = { showSearch = false }, onSelect = { selectedTransaction = it; showSearch = false })
    selectedTransaction?.let { txn ->
        val accountName = accounts.firstOrNull { it.id == txn.accountId }?.name ?: "Manual"
        TransactionDetailDialog(txn, accountName, onDismiss = { selectedTransaction = null }, onCategory = { category -> viewModel.correct(txn, category); selectedTransaction = null }, onDelete = { viewModel.delete(txn); selectedTransaction = null })
    }
}

@Composable
private fun KhataBottomBar(selected: String, reduceMotion: Boolean, hapticsEnabled: Boolean, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val navItems = remember { listOf("Home", "Calendar", "Analysis", "Settings") }
    val latestSelected by rememberUpdatedState(selected)
    val latestOnSelect by rememberUpdatedState(onSelect)
    DewSurface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 16.dp).height(68.dp),
        shape = RoundedCornerShape(32.dp), zIndex = 90f,
        tone = MaterialTheme.colorScheme.surface, elevation = 17.dp, reduceMotion = reduceMotion
    ) {
        val dragThresholdPx = with(LocalDensity.current) { 48.dp.toPx() }
        Row(
            Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 7.dp)
                .pointerInput(hapticsEnabled) {
                    var dragTotal = 0f
                    var startIndex = 0
                    var currentIndex = 0
                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragTotal = 0f
                            startIndex = navItems.indexOf(latestSelected).coerceAtLeast(0)
                            currentIndex = startIndex
                        },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            dragTotal += amount
                            val direction = if (dragTotal < 0f) 1 else -1
                            val steps = (kotlin.math.abs(dragTotal) / dragThresholdPx).toInt()
                            val target = (startIndex + direction * steps).coerceIn(0, navItems.lastIndex)
                            while (currentIndex != target) {
                                currentIndex += if (target > currentIndex) 1 else -1
                                latestOnSelect(navItems[currentIndex])
                                if (hapticsEnabled) performKhataHaptic(context)
                            }
                        },
                        onDragEnd = { dragTotal = 0f },
                        onDragCancel = { dragTotal = 0f }
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            navItems.forEachIndexed { index, item ->
                val active = selected == item
                val activeWidth = when (item) { "Calendar" -> 140.dp; "Analysis", "Settings" -> 132.dp; else -> 100.dp }
                val width by animateDpAsState(if (active) activeWidth else 42.dp, animationSpec = if (reduceMotion) snap() else spring(dampingRatio = .72f, stiffness = 440f), label = "nav-width-$item")
                val icon = when (item) {
                    "Home" -> Icons.Outlined.Home
                    "Calendar" -> Icons.Outlined.CalendarMonth
                    "Analysis" -> Icons.Outlined.BarChart
                    else -> Icons.Outlined.Settings
                }
                val select = {
                    if (selected != item) {
                        if (hapticsEnabled) performKhataHaptic(context)
                        onSelect(item)
                    }
                }
                if (active) {
                    DewSurface(
                        modifier = Modifier.width(width).height(52.dp),
                        shape = RoundedCornerShape(26.dp), zIndex = 101f + index,
                        tone = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .78f),
                        elevation = 7.dp, reduceMotion = reduceMotion, onClick = select
                    ) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(icon, contentDescription = item, modifier = Modifier.size(21.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            AnimatedVisibility(active) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(item, maxLines = 1, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        onClick = select,
                        modifier = Modifier.width(width).height(52.dp),
                        color = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = item, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    transactions: List<TransactionEntity>, settings: UserSettings, reviewCount: Int,
    onReview: () -> Unit, onSearch: () -> Unit,
    onTransaction: (TransactionEntity) -> Unit
) {
    val reduceMotion = shouldReduceMotion(settings.reduceMotion)
    val today = LocalDate.now()
    val budgetMode = settings.monthlyBudgetPaise > 0L
    val selectedPeriod = if (budgetMode) SpendPeriod.MONTH else SpendPeriod.DAY
    val currentRange = SpendPeriodRanges.current(selectedPeriod, today)
    val currentSpend = remember(transactions, currentRange) {
        netSpend(transactions.filter { (it.type == "EXPENSE" || it.type == "REFUND") && currentRange.contains(it.date) })
    }
    val dueNudge = remember(transactions, today) { NudgeCadence.due(transactions, today).firstOrNull() }
    val quoteRepo = rememberQuoteRepo()
    val lifecycleOwner = LocalLifecycleOwner.current
    var resumeToken by remember { mutableIntStateOf(0) }
    var showQuoteNote by rememberSaveable { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) resumeToken++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val trigger = when {
        transactions.isEmpty() -> "EMPTY_STATE"
        budgetMode && currentSpend > settings.monthlyBudgetPaise -> "OVERSPEND"
        else -> "DAILY_NOTE"
    }
    val ambientQuote = remember(settings.language, settings.romanScript, resumeToken) {
        quoteRepo.ambientQuote(settings.language, settings.romanScript)
    }
    val quote = ambientQuote ?: remember(settings.language, settings.romanScript, trigger, resumeToken) {
        quoteRepo.forTrigger(settings.language, trigger, settings.romanScript)
    }
    val hour = java.time.LocalTime.now().hour
    val greeting = when (hour) { in 5..11 -> "Good morning,"; in 12..16 -> "Good afternoon,"; in 17..20 -> "Good evening,"; else -> "Good night," }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(greeting, fontFamily = CaveatFamily, fontWeight = FontWeight.Bold, fontSize = 29.sp, color = MaterialTheme.colorScheme.primary)
                Text("${settings.username.ifBlank { "Hello there" }}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onSearch, modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.Search, contentDescription = "Search transactions") }
        }
        quote?.let { QuoteBanner(it.text, reduceMotion, onReadMore = { showQuoteNote = true }) }
        Spacer(Modifier.height(12.dp))
        SpendingSummaryCard(currentSpend, if (budgetMode) settings.monthlyBudgetPaise else 0L, reduceMotion)
        if (reviewCount > 0) {
            Spacer(Modifier.height(12.dp))
            DewSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), zIndex = 15f, tone = MaterialTheme.colorScheme.tertiaryContainer, elevation = 9.dp, reduceMotion = reduceMotion, onClick = onReview) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.WarningAmber, contentDescription = null); Spacer(Modifier.width(8.dp))
                    Text("$reviewCount to review", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium); Spacer(Modifier.weight(1f)); Icon(Icons.Outlined.ChevronRight, contentDescription = "Open review queue")
                }
            }
        }
        if (settings.nudgesEnabled) {
            dueNudge?.let { nudge ->
                Spacer(Modifier.height(12.dp))
                DewSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), zIndex = 16f, tone = MaterialTheme.colorScheme.secondaryContainer, elevation = 10.dp, reduceMotion = reduceMotion) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.NotificationsActive, null, tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(12.dp))
                        Column { Text("A little heads-up", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold); Text("${nudge.item}: your usual rhythm is about ${nudge.intervalDays} days. You may be running low.", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp)); SectionTitle("Recent activity")
        if (transactions.isEmpty()) Text("No transactions yet. Add one when you're ready.", modifier = Modifier.padding(vertical = 18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        else transactions.take(4).forEach { TransactionRow(it, onClick = { onTransaction(it) }) }
        Spacer(Modifier.height(90.dp))
    }
    if (showQuoteNote) quote?.let { QuoteNoteDialog(it.text, onDismiss = { showQuoteNote = false }) }
}

@Composable
private fun SpendingSummaryCard(spent: Long, budget: Long, reduceMotion: Boolean) {
    val hasBudget = budget > 0L
    val progress = if (hasBudget) (spent.toFloat() / budget.toFloat()).coerceIn(0f, 1f) else 0f
    val overBudget = hasBudget && spent > budget
    val statusColor = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val today = LocalDate.now()
    val periodTitle = if (hasBudget) "THIS MONTH" else "TODAY"
    DewSurface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).animateContentSize(),
        shape = RoundedCornerShape(32.dp), zIndex = 10f,
        tone = MaterialTheme.colorScheme.surface.copy(alpha = .84f), elevation = 20.dp, reduceMotion = reduceMotion
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(periodTitle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(5.dp))
                    Text(money(spent), fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"), maxLines = 1, softWrap = false)
                }
                Box(Modifier.size(54.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .78f)), contentAlignment = Alignment.Center) {
                    Icon(if (hasBudget) Icons.Outlined.AccountBalanceWallet else Icons.Outlined.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(Modifier.height(15.dp))
            if (hasBudget) {
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape), color = statusColor, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (overBudget) "Over budget by ${money(spent - budget)}" else "${money((budget - spent).coerceAtLeast(0L))} left", style = MaterialTheme.typography.bodyMedium, color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text("of ${money(budget)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text(if (spent == 0L) "Your daily snapshot will appear here." else "Your spending recorded for ${today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun QuoteBanner(text: String, reduceMotion: Boolean, onReadMore: () -> Unit) {
    val longQuote = text.codePointCount(0, text.length) > 64
    DewSurface(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(24.dp),
        zIndex = 11f, tone = MaterialTheme.colorScheme.surface, elevation = 11.dp,
        reduceMotion = reduceMotion, onClick = if (longQuote) onReadMore else null
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp)) {
            Text(text, fontFamily = CaveatFamily, fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 31.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
            if (longQuote) {
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tap to read the full note", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun QuoteNoteDialog(text: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 20.dp
        ) {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.StickyNote2, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(9.dp))
                    Text("A little note", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(18.dp))
                Text(text, style = MaterialTheme.typography.headlineSmall.copy(lineHeight = 34.sp), color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(18.dp))
                Text("Keep this thought close.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Close") }
            }
        }
    }
}

@Composable
private fun CategoryCard(id: String, label: String, amount: Long, percent: Int, modifier: Modifier, onClick: () -> Unit) {
    DewSurface(onClick = onClick, modifier = modifier.heightIn(min = 126.dp), shape = RoundedCornerShape(22.dp), zIndex = 30f, tone = MaterialTheme.colorScheme.surface, elevation = 9.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(id, Modifier.size(20.dp)); Spacer(Modifier.weight(1f)); Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(label, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
            Text(moneyShort(amount), fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"))
            Text("$percent%", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TransactionRow(transaction: TransactionEntity, onClick: () -> Unit) {
    val color = categoryAccent(transaction.categoryId)
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = .15f)), contentAlignment = Alignment.Center) { CategoryIcon(transaction.categoryId, Modifier.size(21.dp), color) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(transaction.rawText.ifBlank { "Transaction" }, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            Text("${KhataCategories.names[transaction.categoryId] ?: "Uncategorized"} · ${transaction.date}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text((if (transaction.direction == "CREDIT") "+" else "−") + money(transaction.amountPaise), color = if (transaction.direction == "CREDIT") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"))
    }
}

@Composable
private fun ReviewScreen(queue: List<TransactionEntity>, categories: List<Pair<String, String>>, reduceMotion: Boolean, onBack: () -> Unit, onCorrect: (TransactionEntity, String) -> Unit, onTransfer: (TransactionEntity) -> Unit, onIgnore: (TransactionEntity) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }; Text("Review queue", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        if (queue.isEmpty()) {
            Spacer(Modifier.weight(1f)); Text("All caught up", fontFamily = CaveatFamily, fontSize = 30.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.align(Alignment.CenterHorizontally)); Spacer(Modifier.weight(1f))
        } else {
            val txn = queue.first()
            Text("1 / ${queue.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(24.dp))
            DewSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), zIndex = 34f, tone = MaterialTheme.colorScheme.surface, elevation = 15.dp, reduceMotion = reduceMotion) {
                Column(Modifier.padding(20.dp)) {
                    Text(txn.rawText, fontWeight = FontWeight.Medium, fontSize = 20.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${if (txn.direction == "CREDIT") "+" else "−"}${money(txn.amountPaise)}", fontSize = 32.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"))
                    Text(txn.date, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text(txn.rawText, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Spacer(Modifier.height(22.dp)); Text("Choose a category", fontWeight = FontWeight.Medium)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
                categories.filter { it.first != "uncategorized" }.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (id, name) -> AssistChip(onClick = { onCorrect(txn, id) }, label = { Text(name) }, leadingIcon = { CategoryIcon(id, Modifier.size(18.dp)) }, modifier = Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { onTransfer(txn) }, modifier = Modifier.weight(1f)) { Text("Transfer") }
                TextButton(onClick = { onIgnore(txn) }, modifier = Modifier.weight(1f)) { Text("Ignore") }
            }
        }
    }
}

@Composable
private fun CalendarScreen(transactions: List<TransactionEntity>, onTransaction: (TransactionEntity) -> Unit) {
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var selectedDay by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val days = month.lengthOfMonth()
    val offset = month.atDay(1).dayOfWeek.value % 7
    val today = LocalDate.now()
    val spendByDay = remember(transactions, month) {
        transactions.filter { (it.type == "EXPENSE" || it.type == "REFUND") && it.date.startsWith(month.toString()) }
            .groupBy { it.date.takeLast(2).toIntOrNull() ?: 1 }.mapValues { netSpend(it.value) }
    }
    val maxSpend = remember(spendByDay) { spendByDay.values.maxOrNull()?.coerceAtLeast(1) ?: 1 }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Calendar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val selected = runCatching { LocalDate.parse(selectedDay) }.getOrDefault(month.atDay(1))
                    month = month.minusMonths(1)
                    selectedDay = month.atDay(selected.dayOfMonth.coerceAtMost(month.lengthOfMonth())).toString()
                }) { Icon(Icons.Outlined.KeyboardArrowLeft, "Previous month") }
                Text(month.format(DateTimeFormatter.ofPattern("MMM yyyy")), fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
                IconButton(onClick = {
                    val selected = runCatching { LocalDate.parse(selectedDay) }.getOrDefault(month.atDay(1))
                    month = month.plusMonths(1)
                    selectedDay = month.atDay(selected.dayOfMonth.coerceAtMost(month.lengthOfMonth())).toString()
                }) { Icon(Icons.Outlined.KeyboardArrowRight, "Next month") }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) { listOf("S", "M", "T", "W", "T", "F", "S").forEach { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) } }
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.heightIn(max = 350.dp), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(offset) { Spacer(Modifier.aspectRatio(1f)) }
            items(days) { index ->
                val day = index + 1
                val value = spendByDay[day] ?: 0L
                val ratio = value.toFloat() / maxSpend
                val tint = if (value == 0L) MaterialTheme.colorScheme.surfaceVariant else if (ratio < .5f) MaterialTheme.colorScheme.secondary.copy(alpha = .30f + ratio * .3f) else MaterialTheme.colorScheme.primary.copy(alpha = .35f + ratio.coerceAtMost(1f) * .35f)
                val date = month.atDay(day).toString()
                Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(tint).then(if (date == selectedDay) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp)) else if (date == today.toString()) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)) else Modifier).clickable { selectedDay = date }, contentAlignment = Alignment.Center) { Text(day.toString(), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) }
            }
        }
        Spacer(Modifier.height(18.dp)); SectionTitle("${LocalDate.parse(selectedDay).format(DateTimeFormatter.ofPattern("d MMMM"))} · ${money(spendByDay[LocalDate.parse(selectedDay).dayOfMonth] ?: 0L)}")
        val dayTxns = remember(transactions, selectedDay) { transactions.filter { it.date == selectedDay } }
        if (dayTxns.isEmpty()) Text("No transactions on this day.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
        else LazyColumn { items(dayTxns) { TransactionRow(it) { onTransaction(it) } } }
    }
}

@Composable
private fun AnalysisScreen(transactions: List<TransactionEntity>, categories: List<Pair<String, String>>, onTransaction: (TransactionEntity) -> Unit, onCategory: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedMonth by rememberSaveable { mutableIntStateOf(LocalDate.now().monthValue) }
    var selectedYear by rememberSaveable { mutableIntStateOf(LocalDate.now().year) }
    val year = selectedYear
    val earliestYear = remember(transactions) {
        transactions.mapNotNull { runCatching { LocalDate.parse(it.date).year }.getOrNull() }.minOrNull()?.coerceAtMost(LocalDate.now().year) ?: LocalDate.now().year
    }
    val monthPrefix = remember(year, selectedMonth) { "%04d-%02d".format(Locale.US, year, selectedMonth) }
    val monthTransactions = remember(transactions, monthPrefix) { transactions.filter { it.date.startsWith(monthPrefix) } }
    val monthExpensesAndRefunds = remember(monthTransactions) { monthTransactions.filter { it.type == "EXPENSE" || it.type == "REFUND" } }
    val monthTotal = remember(monthExpensesAndRefunds) { netSpend(monthExpensesAndRefunds) }
    val monthEntryCount = remember(monthExpensesAndRefunds) { monthExpensesAndRefunds.size }
    val filtered = remember(query, transactions) {
        val q = query.trim().lowercase()
        if (q.isBlank()) emptyList() else transactions.filter { t ->
            val amount = t.amountPaise / 100.0
            when {
                q.startsWith(">") -> amount > (q.drop(1).toDoubleOrNull() ?: Double.MAX_VALUE)
                q.startsWith("<") -> amount < (q.drop(1).toDoubleOrNull() ?: Double.MIN_VALUE)
                q.toDoubleOrNull() != null -> amount.toLong() == q.toDouble().toLong()
                else -> t.rawText.lowercase().contains(q) || (KhataCategories.names[t.categoryId] ?: "").lowercase().contains(q)
            }
        }
    }
    val byCategory = remember(monthTransactions) {
        monthTransactions.filter { it.type == "EXPENSE" || it.type == "REFUND" }
            .groupBy { it.categoryId }
            .mapValues { (_, items) -> netSpend(items) }
            .filterValues { it > 0L }
            .entries.sortedByDescending { it.value }
    }
    val merchantTop = remember(monthTransactions) {
        monthTransactions.filter { it.type == "EXPENSE" || it.type == "REFUND" }
            .groupBy { it.normalizedMerchant }
            .mapValues { netSpend(it.value) }
            .filterValues { it > 0L }
            .entries.sortedByDescending { it.value }.take(5)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text("Analysis", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp, bottom = 12.dp))
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Search transactions") }, shape = RoundedCornerShape(16.dp))
        if (query.isNotBlank()) {
            Text("Transactions", fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 16.dp))
            if (filtered.isEmpty()) Text("No matching transactions.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
            else LazyColumn { items(filtered) { TransactionRow(it) { onTransaction(it) } } }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    SectionTitle("Spending rhythm")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedYear = (selectedYear - 1).coerceAtLeast(earliestYear) }, enabled = selectedYear > earliestYear, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.KeyboardArrowLeft, "Previous year") }
                        Text(year.toString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { selectedYear = (selectedYear + 1).coerceAtMost(LocalDate.now().year) }, enabled = selectedYear < LocalDate.now().year, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.KeyboardArrowRight, "Next year") }
                    }
                }
                YearHeatmap(transactions, year, selectedMonth, onMonthSelected = { selectedMonth = it })
                Spacer(Modifier.height(12.dp))
                MonthSummaryCard(YearMonth.of(year, selectedMonth), monthTotal, monthEntryCount)
                Spacer(Modifier.height(18.dp)); SectionTitle("Top merchants · ${YearMonth.of(year, selectedMonth).format(DateTimeFormatter.ofPattern("MMMM"))}")
                if (merchantTop.isEmpty()) Text("No recorded spending for this month.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                merchantTop.forEachIndexed { index, entry ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
                        Text(entry.key.ifBlank { "Unknown" }, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(money(entry.value), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"))
                    }
                }
                Spacer(Modifier.height(20.dp)); SectionTitle("Categories · ${YearMonth.of(year, selectedMonth).format(DateTimeFormatter.ofPattern("MMMM"))}")
                val total = byCategory.sumOf { it.value }.coerceAtLeast(0L)
                if (byCategory.isEmpty()) {
                    DewSurface(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(28.dp), tone = MaterialTheme.colorScheme.surface.copy(alpha = .80f), elevation = 13.dp) {
                        Text("Your category picture will build as you add expenses.", Modifier.padding(20.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    DewSurface(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(30.dp), tone = MaterialTheme.colorScheme.surface.copy(alpha = .80f), elevation = 17.dp) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                                    CategoryDonut(byCategory.map { it.key to it.value }, total)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Text(moneyShort(total), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                                Spacer(Modifier.width(18.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                                    byCategory.take(3).forEach { item ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(10.dp).clip(CircleShape).background(categoryAccent(item.key)))
                                            Spacer(Modifier.width(8.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(KhataCategories.names[item.key] ?: item.key, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("${(item.value.toDouble() * 100.0 / total.toDouble()).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .72f))
                            Spacer(Modifier.height(6.dp))
                            byCategory.forEach { item ->
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onCategory(item.key) }.padding(vertical = 10.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CategoryIcon(item.key, Modifier.size(22.dp)); Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(KhataCategories.names[item.key] ?: item.key, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(money(item.value), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
                                        }
                                        LinearProgressIndicator(progress = { (item.value.toFloat() / total.toFloat()).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(7.dp).clip(CircleShape), color = categoryAccent(item.key), trackColor = MaterialTheme.colorScheme.surfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(90.dp))
            }
        }
    }
}


@Composable
private fun CategoryDonut(items: List<Pair<String, Long>>, total: Long) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val segments = items.map { categoryAccent(it.first) to it.second }
    Canvas(Modifier.fillMaxSize()) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
        drawArc(track, -90f, 360f, false, style = stroke)
        if (total > 0L) {
            var start = -90f
            segments.forEach { (color, amount) ->
                val sweep = (amount.toDouble() / total.toDouble() * 360.0).toFloat().coerceAtLeast(1.2f)
                drawArc(color, start, (sweep - 2.5f).coerceAtLeast(.4f), false, style = stroke)
                start += sweep
            }
        }
    }
}

@Composable
private fun YearHeatmap(transactions: List<TransactionEntity>, year: Int, selectedMonth: Int, onMonthSelected: (Int) -> Unit) {
    val byDate = remember(transactions, year) {
        transactions.filter { it.date.startsWith(year.toString()) && (it.type == "EXPENSE" || it.type == "REFUND") }
            .groupBy { it.date }.mapValues { netSpend(it.value) }
    }
    val maximum = byDate.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val today = LocalDate.now()
    val firstDay = LocalDate.of(year, 1, 1)
    val weeks = remember(year) {
        val leading = firstDay.dayOfWeek.value % 7
        val dates: List<LocalDate?> = List(leading) { null } + (0 until firstDay.lengthOfYear()).map { firstDay.plusDays(it.toLong()) }
        (dates + List((7 - dates.size % 7) % 7) { null }).chunked(7)
    }
    Column {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            (1..12).forEach { month ->
                FilterChip(
                    selected = selectedMonth == month,
                    onClick = { onMonthSelected(month) },
                    label = { Text(YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM")), maxLines = 1) }
                )
            }
        }
        DewSurface(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(24.dp), zIndex = 36f, tone = MaterialTheme.colorScheme.surface, elevation = 11.dp) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    weeks.forEach { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            week.forEach { date ->
                                val amount = date?.let { byDate[it.toString()] ?: 0L } ?: 0L
                                val intensity = if (amount <= 0L) 0f else (amount.toFloat() / maximum.toFloat()).coerceIn(0f, 1f)
                                val selected = date?.monthValue == selectedMonth
                                val alpha = when {
                                    date == null -> 0f
                                    date.isAfter(today) -> .14f
                                    amount <= 0L -> if (selected) .34f else .18f
                                    intensity < .25f -> if (selected) .46f else .26f
                                    intensity < .50f -> if (selected) .62f else .36f
                                    intensity < .75f -> if (selected) .78f else .46f
                                    else -> if (selected) .94f else .58f
                                }
                                val tint = if (date == null || (date != null && date.isAfter(today))) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
                                    else if (amount <= 0L) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                                Box(Modifier.size(11.dp).clip(RoundedCornerShape(3.dp)).background(tint))
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(5.dp))
                    listOf(.26f, .42f, .62f, .82f).forEach { alpha -> Box(Modifier.padding(horizontal = 2.dp).size(10.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))) }
                    Spacer(Modifier.width(5.dp))
                    Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MonthSummaryCard(month: YearMonth, total: Long, entries: Int) {
    DewSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), zIndex = 37f, tone = MaterialTheme.colorScheme.surface, elevation = 12.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase(Locale.US), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = .7.sp)
                Spacer(Modifier.height(4.dp))
                Text(money(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$entries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(if (entries == 1) "entry" else "entries", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


@Composable
private fun CategoryDetailScreen(categoryId: String, transactions: List<TransactionEntity>, onBack: () -> Unit, onTransaction: (TransactionEntity) -> Unit) {
    val chosen = transactions.filter { it.categoryId == categoryId }
    val total = chosen.sumOf { it.amountPaise }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }; CategoryIcon(categoryId, Modifier.size(24.dp)); Spacer(Modifier.width(8.dp)); Text(KhataCategories.names[categoryId] ?: categoryId, style = MaterialTheme.typography.titleLarge) }
        Text(money(total), fontSize = 30.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp, top = 12.dp), style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"))
        Text("${chosen.size} transactions", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.height(18.dp)); SectionTitle("Transactions")
        LazyColumn { items(chosen) { TransactionRow(it) { onTransaction(it) } } }
    }
}

@Composable
private fun SettingsScreen(settings: UserSettings, viewModel: KhataViewModel, transactions: List<TransactionEntity>, accounts: List<AccountEntity>, onClear: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val reduceMotion = shouldReduceMotion(settings.reduceMotion)
    var username by remember(settings.username) { mutableStateOf(settings.username) }
    var budget by remember(settings.monthlyBudgetPaise) { mutableStateOf(if (settings.monthlyBudgetPaise > 0) (settings.monthlyBudgetPaise / 100.0).toString() else "") }
    var clearConfirm by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(NotificationHelper.canNotify(context)) }
    var toastMessage by remember { mutableStateOf("") }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf("") }
    var pendingImport by remember { mutableStateOf<List<TransactionEntity>?>(null) }
    var checkingUpdates by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf("") }
    var updateResult by remember { mutableStateOf<GitHubUpdateResult?>(null) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsEnabled = granted && NotificationHelper.canNotify(context)
        if (notificationsEnabled) NotificationHelper.showPermissionSuccess(context)
    }
    val csvExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            val snapshot = transactions.toList()
            coroutineScope.launch {
                importMessage = "Preparing ${snapshot.size} transaction rows…"
                try {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("Unable to open the selected destination.")
                        output.bufferedWriter(Charsets.UTF_8).use { it.write(CsvTransactionCodec.encode(snapshot)) }
                    }
                    importMessage = "Exported ${snapshot.size} transactions to CSV."
                } catch (error: Exception) {
                    importMessage = "Export failed: ${error.message ?: "could not write the file"}"
                }
            }
        }
    }
    val csvImport = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val fallbackAccount = accounts.firstOrNull()?.id ?: "manual"
            val accountIds = accounts.map { it.id }.toSet() + fallbackAccount
            coroutineScope.launch {
                importMessage = "Reading transaction file…"
                try {
                    val parsed = withContext(Dispatchers.IO) {
                        CsvTransactionCodec.decode(readLimitedCsv(context, uri), fallbackAccount, accountIds)
                    }
                    pendingImport = parsed
                    importMessage = ""
                } catch (error: Exception) {
                    importMessage = "Import failed: ${error.message ?: "the file could not be read"}"
                }
            }
        }
    }
    val accountBalances = remember(accounts, transactions) {
        accounts.associate { account ->
            account.id to (account.openingBalancePaise + transactions.asSequence().filter { it.accountId == account.id }
                .sumOf { if (it.direction == "CREDIT") it.amountPaise else -it.amountPaise })
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp, bottom = 20.dp))
        SectionTitle("Profile")
        OutlinedTextField(username, { username = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextButton(onClick = { viewModel.username(username.trim()) }) { Text("Save name") }
        Spacer(Modifier.height(12.dp)); SectionTitle("Language · script")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("hi" to "Hindi", "te" to "Telugu", "ta" to "Tamil", "ml" to "Malayalam", "kn" to "Kannada").forEach { (code, label) -> FilterChip(selected = settings.language == code, onClick = { viewModel.language(code); viewModel.roman(false) }, label = { Text(label) }) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Read in English letters", Modifier.weight(1f)); Switch(settings.romanScript, { viewModel.roman(it) }) }
        Spacer(Modifier.height(12.dp)); SectionTitle("Appearance")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("SYSTEM" to "System", "WALLPAPER" to "Wallpaper", "LIGHT" to "Light", "DARK" to "Dark").forEach { (value, label) -> FilterChip(selected = settings.themeMode == value, onClick = { viewModel.theme(value) }, label = { Text(label) }) } }
        Text(if (settings.themeMode == "WALLPAPER") "Uses wallpaper-derived accents on Android 12+ while keeping warm-paper surfaces and readable ink; older versions use Khata’s fixed palette." else "Theme-aware ink, accessible contrast and one warm Ember palette.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Reduce motion", Modifier.weight(1f)); listOf("FOLLOW_SYSTEM", "ON", "OFF").forEach { value -> FilterChip(selected = settings.reduceMotion == value, onClick = { viewModel.motion(value) }, label = { Text(when (value) { "FOLLOW_SYSTEM" -> "Auto"; else -> value.lowercase().replaceFirstChar { it.uppercase() } }) }) } }
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Haptics", Modifier.weight(1f)); Switch(settings.hapticsEnabled, { viewModel.haptics(it) }) }
        Spacer(Modifier.height(10.dp)); SectionTitle("Smart nudges")
        Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Restock reminders"); Text("Learns your grocery, fuel and essentials rhythm locally. Allow notifications below for background alerts.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }; Switch(settings.nudgesEnabled, { viewModel.nudges(it) }) }
        Spacer(Modifier.height(12.dp)); SectionTitle("Budget")
        OutlinedTextField(budget, { budget = it.filter { c -> c.isDigit() || c == '.' } }, modifier = Modifier.fillMaxWidth(), label = { Text("Monthly limit") }, prefix = { Text("₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
        TextButton(onClick = { viewModel.budget(((budget.toDoubleOrNull() ?: 0.0) * 100).toLong()) }) { Text("Save budget") }
        Spacer(Modifier.height(12.dp)); SectionTitle("Notifications")
        Text("Khata can alert you when a recorded expense takes you over your monthly budget.", color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
        DewSurface(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp),
            shape = RoundedCornerShape(26.dp), zIndex = 42f,
            tone = if (notificationsEnabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
            elevation = 11.dp, reduceMotion = reduceMotion,
            onClick = {
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    notificationsEnabled = NotificationHelper.canNotify(context)
                    if (notificationsEnabled) NotificationHelper.showPermissionSuccess(context)
                }
            }
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(if (notificationsEnabled) Icons.Outlined.NotificationsActive else Icons.Outlined.Notifications, null, tint = if (notificationsEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp)); Text(if (notificationsEnabled) "Alerts enabled · send test" else "Enable budget alerts", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
        if (!notificationsEnabled) Text("You can turn alerts off any time in Android Settings.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp)); SectionTitle("Home screen")
        DewSurface(modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(54.dp), shape = RoundedCornerShape(27.dp), zIndex = 42f, tone = MaterialTheme.colorScheme.primaryContainer, elevation = 13.dp, reduceMotion = reduceMotion, onClick = { showWidgetPicker = true }) {
            Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Icon(Icons.Outlined.Widgets, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(9.dp)); Text("Choose a Khata widget", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold) }
        }
        Text("Add Daily, Weekly, Monthly, All-time, Budget ring, or Quote as a separate home-screen widget. Widgets refresh after entries and periodically.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
        if (showWidgetPicker) AlertDialog(
            onDismissRequest = { showWidgetPicker = false },
            title = { Text("Choose a widget") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    HomeWidgetChoice.entries.forEach { choice ->
                        TextButton(
                            onClick = {
                                pinKhataWidget(context, choice.provider)
                                showWidgetPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(choice.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text(choice.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showWidgetPicker = false }) { Text("Close") } }
        )
        Spacer(Modifier.height(12.dp)); SectionTitle("Accounts")
        if (accounts.isEmpty()) Text("Loading accounts…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        accounts.forEach { account ->
            val balance = accountBalances[account.id] ?: account.openingBalancePaise
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary.copy(alpha = .14f)), contentAlignment = Alignment.Center) { Icon(if (account.accountType == "BANK") Icons.Outlined.AccountBalance else Icons.Outlined.Wallet, null, tint = MaterialTheme.colorScheme.secondary) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(account.name, fontWeight = FontWeight.Medium); Text("${account.institution} · ${account.accountType.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
                    Text(money(balance), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"))
                }
            }
        }
        if (toastMessage.isNotBlank()) { Text(toastMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp)); TextButton(onClick = { toastMessage = "" }) { Text("Dismiss") } }
        Spacer(Modifier.height(16.dp)); SectionTitle("Data")
        OutlinedButton(onClick = { csvExport.launch("khata-transactions.csv") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.FileDownload, null); Spacer(Modifier.width(8.dp)); Text("Export CSV · ${transactions.size} transactions")
        }
        OutlinedButton(onClick = { csvImport.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.FileUpload, null); Spacer(Modifier.width(8.dp)); Text("Import transactions from CSV")
        }
        Text("Exports include every transaction. Imports are validated, merge with existing records, and skip IDs already present.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
        if (importMessage.isNotBlank()) Text(importMessage, color = if (importMessage.startsWith("Import failed") || importMessage.startsWith("Export failed")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(14.dp)); SectionTitle("About")
        Text("Khata 2.1.0 · Everyday, in balance.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Transactions stay on this device. Update checks contact GitHub only when you tap the button.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        DewSurface(modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(52.dp), shape = RoundedCornerShape(26.dp), zIndex = 42f, tone = MaterialTheme.colorScheme.primaryContainer, elevation = 11.dp, reduceMotion = reduceMotion, onClick = {
            if (!checkingUpdates) {
                checkingUpdates = true
                updateMessage = "Checking the latest GitHub release…"
                coroutineScope.launch {
                    try {
                        updateResult = GitHubReleaseChecker.check(context)
                        updateMessage = if (updateResult?.updateAvailable == true) "Khata ${updateResult?.latestVersion} is available." else "You’re up to date · Khata ${updateResult?.currentVersion}."
                    } catch (error: Exception) {
                        updateResult = null
                        updateMessage = error.message ?: "Could not check for updates. Try again when online."
                    } finally {
                        checkingUpdates = false
                    }
                }
            }
        }) {
            Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Outlined.SystemUpdate, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(9.dp)); Text(if (checkingUpdates) "Checking…" else "Check for updates", fontWeight = FontWeight.SemiBold)
            }
        }
        if (updateMessage.isNotBlank()) Text(updateMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
        updateResult?.takeIf { it.updateAvailable }?.let { release ->
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.releaseUrl))) }, modifier = Modifier.fillMaxWidth()) { Text("Open Khata ${release.latestVersion} release") }
        }
        OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vijayarkananduri/khata"))) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.StarOutline, null); Spacer(Modifier.width(8.dp)); Text("Star Khata on GitHub")
        }
        OutlinedButton(onClick = { clearConfirm = true }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) { Icon(Icons.Outlined.Delete, null); Spacer(Modifier.width(8.dp)); Text("Clear all transaction data") }
        Spacer(Modifier.height(80.dp))
    }
    pendingImport?.let { imported ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Import ${imported.size} transactions?") },
            text = { Text("Khata will merge these rows with your current data. Matching transaction IDs are skipped; your existing records are not replaced.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingImport = null
                    viewModel.importTransactions(imported) { result ->
                        result.onSuccess { added -> importMessage = "Imported $added transactions; ${imported.size - added} duplicate IDs were skipped." }
                            .onFailure { error -> importMessage = "Import failed: ${error.message ?: "database write failed"}" }
                    }
                }) { Text("Import") }
            },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Cancel") } }
        )
    }
    if (clearConfirm) AlertDialog(onDismissRequest = { clearConfirm = false }, title = { Text("Clear all data?") }, text = { Text("This permanently deletes all transactions from this device. Settings and categories stay.") }, confirmButton = { TextButton(onClick = { onClear(); clearConfirm = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { clearConfirm = false }) { Text("Cancel") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddDialog(accounts: List<AccountEntity>, reduceMotion: Boolean, onDismiss: () -> Unit, onSave: (String, Long, String, LocalDate, String, String?, String) -> Unit) {
    var merchant by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var direction by rememberSaveable { mutableStateOf("DEBIT") }
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var note by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("auto") }
    var showNote by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAccounts by remember { mutableStateOf(false) }
    var accountId by remember(accounts) { mutableStateOf(accounts.firstOrNull()?.id ?: "manual") }
    val date = runCatching { LocalDate.parse(dateText) }.getOrDefault(LocalDate.now())
    val account = accounts.firstOrNull { it.id == accountId }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add transaction") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount") }, prefix = { Text("₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = direction == "DEBIT", onClick = { direction = "DEBIT" }, label = { Text("Expense") })
                    FilterChip(selected = direction == "CREDIT", onClick = { direction = "CREDIT" }, label = { Text("Income") })
                }
                OutlinedTextField(merchant, { merchant = it }, label = { Text("What was it for?") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(date.format(DateTimeFormatter.ofPattern("d MMM")), maxLines = 1)
                        }
                    }
                    Box(Modifier.weight(1.2f)) {
                        OutlinedButton(onClick = { showAccounts = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.AccountBalanceWallet, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(account?.name ?: "Manual", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = showAccounts, onDismissRequest = { showAccounts = false }) {
                            accounts.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { accountId = item.id; showAccounts = false }) }
                        }
                    }
                }
                Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = category == "auto", onClick = { category = "auto" }, label = { Text("Auto") })
                    listOf("groceries", "dining", "transport", "utilities", "shopping", "health").forEach { id ->
                        FilterChip(selected = category == id, onClick = { category = id }, label = { Text(KhataCategories.names[id] ?: id) })
                    }
                }
                if (showNote) OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                else TextButton(onClick = { showNote = true }) { Text("Add a note") }
            }
        },
        confirmButton = {
            val canSave = merchant.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            DewSurface(
                modifier = Modifier.widthIn(min = 88.dp).height(42.dp),
                shape = RoundedCornerShape(21.dp), zIndex = 110f,
                tone = if (canSave) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                elevation = if (canSave) 8.dp else 0.dp, reduceMotion = reduceMotion,
                onClick = if (canSave) {
                    { onSave(merchant.trim(), ((amount.toDoubleOrNull() ?: 0.0) * 100).toLong(), direction, date, note.trim(), category.takeUnless { it == "auto" }, accountId) }
                } else null
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Save", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (canSave) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
    if (showDatePicker) {
        val today = LocalDate.now()
        val datePicker = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    java.time.Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate() <= today
                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = {
                datePicker.selectedDateMillis?.let { millis ->
                    val picked = java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    if (picked <= LocalDate.now()) dateText = picked.toString()
                }
                showDatePicker = false
            }) { Text("Choose date") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePicker) }
    }
}

@Composable
private fun SearchOverlay(transactions: List<TransactionEntity>, onClose: () -> Unit, onSelect: (TransactionEntity) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember(transactions, query) { transactions.filter { it.rawText.contains(query, true) || (KhataCategories.names[it.categoryId] ?: "").contains(query, true) || (query.toLongOrNull()?.let { value -> it.amountPaise / 100 == value } ?: false) } }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(0.dp)) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onClose) { Icon(Icons.Outlined.ArrowBack, "Close search") }; OutlinedTextField(query, { query = it }, modifier = Modifier.weight(1f), singleLine = true, placeholder = { Text("Search merchant, category, amount") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }) }
                Text(if (query.isBlank()) "Recent transactions" else "Results", fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 12.dp))
                LazyColumn { items(results.take(50)) { TransactionRow(it) { onSelect(it) } } }
            }
        }
    }
}

@Composable
private fun TransactionDetailDialog(transaction: TransactionEntity, accountName: String, onDismiss: () -> Unit, onCategory: (String) -> Unit, onDelete: () -> Unit) {
    var showCategories by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(transaction.rawText, maxLines = 1, overflow = TextOverflow.Ellipsis) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${if (transaction.direction == "CREDIT") "+" else "−"}${money(transaction.amountPaise)}", fontSize = 26.sp, fontWeight = FontWeight.Medium)
            Text(transaction.date); Text("${KhataCategories.names[transaction.categoryId] ?: transaction.categoryId} · ${transaction.type}")
            if (transaction.note.isNotBlank()) Text(transaction.note)
            TextButton(onClick = { showCategories = !showCategories }) { Text("Re-categorize") }
            if (showCategories) Column(Modifier.heightIn(max = 190.dp).verticalScroll(rememberScrollState())) {
                KhataCategories.names.filterKeys { it !in listOf("uncategorized", "income", "investments", "fees") }.forEach { (id, name) -> TextButton(onClick = { onCategory(id) }) { Text(name) } }
            }
            Text("Account · $accountName", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text("Original · ${transaction.rawText}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }, dismissButton = { TextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) } })
}

@Composable
private fun SelectableRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick); Text(label, modifier = Modifier.padding(start = 6.dp), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SectionTitle(title: String) { Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface) }

@Composable
private fun CategoryIcon(id: String, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary) {
    val icon = when (id) {
        "groceries" -> Icons.Outlined.ShoppingBasket
        "dining" -> Icons.Outlined.Restaurant
        "transport" -> Icons.Outlined.DirectionsCar
        "utilities" -> Icons.Outlined.Bolt
        "health" -> Icons.Outlined.LocalPharmacy
        "shopping" -> Icons.Outlined.ShoppingBag
        "entertainment" -> Icons.Outlined.Movie
        "subscriptions" -> Icons.Outlined.Autorenew
        "fuel" -> Icons.Outlined.LocalGasStation
        "education" -> Icons.Outlined.School
        "rent" -> Icons.Outlined.HomeWork
        "fees" -> Icons.Outlined.ReceiptLong
        "transfers" -> Icons.Outlined.SwapHoriz
        "income" -> Icons.Outlined.Savings
        "investments" -> Icons.Outlined.TrendingUp
        else -> Icons.Outlined.HelpOutline
    }
    Icon(icon, contentDescription = KhataCategories.names[id] ?: id, modifier = modifier, tint = tint)
}

@Composable
private fun categoryAccent(id: String): Color = when (KhataCategories.colors[id]) {
    "tide" -> MaterialTheme.colorScheme.secondary
    "saffron" -> MaterialTheme.colorScheme.tertiary
    "plum" -> androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary, .5f)
    "brick" -> MaterialTheme.colorScheme.error
    "muted" -> MaterialTheme.colorScheme.onSurfaceVariant
    else -> MaterialTheme.colorScheme.primary
}

private fun readLimitedCsv(context: android.content.Context, uri: Uri): String {
    val stream = context.contentResolver.openInputStream(uri) ?: error("Unable to open the selected file.")
    stream.use { input ->
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8_192)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= 10 * 1024 * 1024) { "CSV file is larger than 10 MiB." }
            output.write(buffer, 0, read)
        }
        return String(output.toByteArray(), Charsets.UTF_8)
    }
}

@Composable
private fun shouldReduceMotion(mode: String): Boolean {
    if (mode == "ON") return true
    if (mode == "OFF") return false
    val context = LocalContext.current
    return runCatching { AndroidSettings.Global.getFloat(context.contentResolver, AndroidSettings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }.getOrDefault(false)
}

private enum class HomeWidgetChoice(val title: String, val description: String, val provider: Class<out AppWidgetProvider>) {
    DAILY("Daily spending", "Today’s net spend." , KhataDailyWidgetProvider::class.java),
    WEEKLY("Weekly spending", "Rolling last seven days.", KhataWeeklyWidgetProvider::class.java),
    MONTHLY("Monthly spending", "Current calendar month.", KhataMonthlyWidgetProvider::class.java),
    ALL_TIME("All-time spending", "All recorded expenses and refunds.", KhataAllTimeWidgetProvider::class.java),
    PROGRESS_RING("Budget progress ring", "A live ring against your monthly budget.", KhataProgressRingWidgetProvider::class.java),
    QUOTE("Quote of the day", "A short line in your chosen app language.", KhataQuoteWidgetProvider::class.java)
}

private fun pinKhataWidget(context: android.content.Context, provider: Class<out AppWidgetProvider>) {
    val manager = AppWidgetManager.getInstance(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported) {
        val accepted = manager.requestPinAppWidget(ComponentName(context, provider), null, null)
        if (!accepted) Toast.makeText(context, "Open your launcher’s Widgets list to add Khata.", Toast.LENGTH_LONG).show()
    } else Toast.makeText(context, "Long-press your home screen, then choose Widgets → Khata.", Toast.LENGTH_LONG).show()
}

@Suppress("DEPRECATION")
private fun performKhataHaptic(context: android.content.Context) {
    runCatching {
        if (android.provider.Settings.System.getInt(context.contentResolver, android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) return
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) (context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator else (context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator)
        if (!vibrator.hasVibrator()) return
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> vibrator.vibrate(VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE))
            else -> vibrator.vibrate(18L)
        }
    }
}

private fun money(paise: Long): String {
    val rupees = paise / 100
    return "₹" + NumberFormat.getIntegerInstance(Locale("en", "IN")).format(rupees)
}
private fun netSpend(transactions: List<TransactionEntity>): Long = transactions.sumOf { transaction ->
    when {
        transaction.type == "REFUND" && transaction.direction == "CREDIT" -> -transaction.amountPaise
        transaction.type == "EXPENSE" && transaction.direction == "DEBIT" -> transaction.amountPaise
        else -> 0L
    }
}.coerceAtLeast(0L)

private fun moneyShort(paise: Long): String {
    val rupees = paise / 100
    return when { rupees >= 100_000 -> "₹${String.format(Locale.US, "%.1f", rupees / 100_000.0)}L"; rupees >= 1_000 -> "₹${String.format(Locale.US, "%.1f", rupees / 1_000.0)}k"; else -> "₹$rupees" }
}
private fun percentMore(current: Long, previous: Long) = ((current - previous) * 100 / previous.coerceAtLeast(1)).toInt()
private fun percentLess(current: Long, previous: Long) = ((previous - current) * 100 / previous.coerceAtLeast(1)).toInt()

@Composable
private fun rememberQuoteRepo(): QuoteRepository {
    val context = LocalContext.current
    return remember(context) { QuoteRepository(context) }
}
