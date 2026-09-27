package ir.behnam.accounting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.behnam.accounting.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {
    val accounts = repository.accounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val obligations = repository.obligations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun addAccount(name: String, type: AccountType, openingBalance: Long = 0) = viewModelScope.launch { repository.addAccount(name, type, openingBalance) }
    fun record(accountId: Long, amount: Long, income: Boolean, category: String, note: String) = viewModelScope.launch { repository.addTransaction(accountId, amount, if (income) TransactionType.INCOME else TransactionType.EXPENSE, category, note) }
    fun addObligation(title: String, amount: Long, type: ObligationType, dueAt: Long) = viewModelScope.launch { repository.databaseAddObligation(title, amount, type, dueAt) }
    fun settle(item: ObligationEntity, accountId: Long) = viewModelScope.launch { repository.settle(item, accountId, item.type == ObligationType.RECEIVABLE) }
    fun transfer(fromAccountId: Long, toAccountId: Long, amount: Long, note: String = "") = viewModelScope.launch { repository.transfer(fromAccountId, toAccountId, amount, note) }
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>) = FinanceViewModel(repository) as T
}
