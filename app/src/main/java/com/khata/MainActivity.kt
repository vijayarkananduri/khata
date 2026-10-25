package com.khata

import android.content.Context
import android.os.Bundle
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khata.data.db.AccountEntity
import com.khata.data.db.TransactionEntity
import com.khata.data.prefs.KhataPreferences
import com.khata.data.prefs.UserSettings
import com.khata.data.repo.KhataRepository
import com.khata.notifications.NotificationHelper
import com.khata.widget.KhataHomeWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltAndroidApp
class KhataApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        com.khata.notifications.RestockNudgeWorker.schedule(this)
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)
        setContent { KhataRoot() }
    }
}

@HiltViewModel
class KhataViewModel @Inject constructor(
    private val repository: KhataRepository,
    private val preferences: KhataPreferences,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    val settings: StateFlow<UserSettings> = preferences.settings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings()
    )
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val reviewQueue = repository.reviewQueue.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val accounts = repository.accounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { viewModelScope.launch { repository.initialize() } }

    private fun refreshWidget() = KhataHomeWidgetProvider.refreshAll(appContext)

    fun finishOnboarding(username: String, language: String, roman: Boolean, budgetPaise: Long) = viewModelScope.launch {
        preferences.completeOnboarding(username, language, roman, budgetPaise)
    }
    fun addAccount(name: String, institution: String, type: String, openingBalancePaise: Long) = viewModelScope.launch {
        repository.addAccount(name, institution, type, openingBalancePaise)
    }
    fun addTransaction(merchant: String, amountPaise: Long, direction: String, date: LocalDate, note: String, chosenCategory: String? = null, accountId: String = "manual") = viewModelScope.launch {
        val today = LocalDate.now()
        val monthPrefix = today.toString().take(7)
        val before = transactions.value.filter { (it.type == "EXPENSE" || it.type == "REFUND") && it.date.startsWith(monthPrefix) }.sumOf {
            if (it.type == "REFUND" && it.direction == "CREDIT") -it.amountPaise else if (it.type == "EXPENSE" && it.direction == "DEBIT") it.amountPaise else 0L
        }.coerceAtLeast(0L)
        val createdType = repository.addTransaction(merchant, amountPaise, direction, date, note, chosenCategory, accountId)
        val budget = settings.value.monthlyBudgetPaise
        if (createdType == "EXPENSE" && direction == "DEBIT" && date.toString().startsWith(monthPrefix) && budget > 0L && before <= budget && before + amountPaise > budget) {
            NotificationHelper.showBudgetExceeded(appContext, before + amountPaise, budget)
        }
        if (createdType != null) refreshWidget()
    }
    fun correct(transaction: TransactionEntity, category: String) = viewModelScope.launch { repository.correct(transaction, category); refreshWidget() }
    fun markTransfer(transaction: TransactionEntity) = viewModelScope.launch { repository.correct(transaction, "transfers", true); refreshWidget() }
    fun ignore(transaction: TransactionEntity) = viewModelScope.launch { repository.ignore(transaction.id) }
    fun delete(transaction: TransactionEntity) = viewModelScope.launch { repository.delete(transaction.id); refreshWidget() }
    fun clearAll() = viewModelScope.launch { repository.clearAll(); refreshWidget() }
    fun username(value: String) = viewModelScope.launch { preferences.setUsername(value) }
    fun language(value: String) = viewModelScope.launch { preferences.setLanguage(value) }
    fun roman(value: Boolean) = viewModelScope.launch { preferences.setRoman(value) }
    fun theme(value: String) = viewModelScope.launch { preferences.setTheme(value) }
    fun motion(value: String) = viewModelScope.launch { preferences.setMotion(value) }
    fun haptics(value: Boolean) = viewModelScope.launch { preferences.setHaptics(value) }
    fun budget(value: Long) = viewModelScope.launch { preferences.setBudget(value) }
    fun compare(value: Boolean) = viewModelScope.launch { preferences.setCompare(value) }
    fun nudges(value: Boolean) = viewModelScope.launch { preferences.setNudges(value) }
}
