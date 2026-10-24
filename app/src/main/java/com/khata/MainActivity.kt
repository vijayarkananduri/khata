package com.khata

import android.content.Context
import android.os.Bundle
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
class KhataApplication : android.app.Application()

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

    fun finishOnboarding(username: String, language: String, roman: Boolean, budgetPaise: Long) = viewModelScope.launch {
        preferences.completeOnboarding(username, language, roman, budgetPaise)
    }
    fun addAccount(name: String, institution: String, type: String, openingBalancePaise: Long) = viewModelScope.launch {
        repository.addAccount(name, institution, type, openingBalancePaise)
    }
    fun addTransaction(merchant: String, amountPaise: Long, direction: String, date: LocalDate, note: String, chosenCategory: String? = null, accountId: String = "manual") = viewModelScope.launch {
        val today = LocalDate.now()
        val monthPrefix = today.toString().take(7)
        val before = transactions.value.filter { it.type == "EXPENSE" && it.direction == "DEBIT" && it.date.startsWith(monthPrefix) }.sumOf { it.amountPaise }
        repository.addTransaction(merchant, amountPaise, direction, date, note, chosenCategory, accountId)
        val budget = settings.value.monthlyBudgetPaise
        if (direction == "DEBIT" && date.toString().startsWith(monthPrefix) && budget > 0L && before <= budget && before + amountPaise > budget) {
            NotificationHelper.showBudgetExceeded(appContext, before + amountPaise, budget)
        }
    }
    fun correct(transaction: TransactionEntity, category: String) = viewModelScope.launch { repository.correct(transaction, category) }
    fun markTransfer(transaction: TransactionEntity) = viewModelScope.launch { repository.correct(transaction, "transfers", true) }
    fun ignore(transaction: TransactionEntity) = viewModelScope.launch { repository.ignore(transaction.id) }
    fun delete(transaction: TransactionEntity) = viewModelScope.launch { repository.delete(transaction.id) }
    fun clearAll() = viewModelScope.launch { repository.clearAll() }
    fun username(value: String) = viewModelScope.launch { preferences.setUsername(value) }
    fun language(value: String) = viewModelScope.launch { preferences.setLanguage(value) }
    fun roman(value: Boolean) = viewModelScope.launch { preferences.setRoman(value) }
    fun theme(value: String) = viewModelScope.launch { preferences.setTheme(value) }
    fun dew(value: Boolean) = viewModelScope.launch { preferences.setDew(value) }
    fun motion(value: String) = viewModelScope.launch { preferences.setMotion(value) }
    fun haptics(value: Boolean) = viewModelScope.launch { preferences.setHaptics(value) }
    fun budget(value: Long) = viewModelScope.launch { preferences.setBudget(value) }
    fun compare(value: Boolean) = viewModelScope.launch { preferences.setCompare(value) }
}
