package ir.behnam.accounting.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val database: FinanceDatabase) {
    private val dao = database.dao()
    val accounts: Flow<List<AccountEntity>> = dao.accounts()
    val transactions: Flow<List<TransactionEntity>> = dao.transactions()
    val obligations: Flow<List<ObligationEntity>> = dao.obligations()
    suspend fun addAccount(name: String, type: AccountType, openingBalance: Long = 0) = dao.addAccount(AccountEntity(name = name, type = type, balance = openingBalance))
    suspend fun databaseAddObligation(title: String, amount: Long, type: ObligationType, dueAt: Long) = dao.addObligation(ObligationEntity(title = title, amount = amount, type = type, dueAt = dueAt))
    suspend fun addTransaction(accountId: Long, amount: Long, type: TransactionType, category: String, note: String = "") = database.withTransaction {
        require(amount > 0) { "مبلغ باید بزرگ‌تر از صفر باشد." }
        dao.addTransaction(TransactionEntity(accountId = accountId, amount = amount, type = type, category = category, note = note))
        dao.changeBalance(accountId, if (type == TransactionType.INCOME || type == TransactionType.RECEIVABLE_COLLECTION) amount else -amount)
    }
    suspend fun transfer(fromAccountId: Long, toAccountId: Long, amount: Long, note: String = "") = database.withTransaction {
        require(fromAccountId != toAccountId) { "مبدأ و مقصد انتقال نباید یکسان باشند." }; require(amount > 0) { "مبلغ باید بزرگ‌تر از صفر باشد." }
        dao.addTransaction(TransactionEntity(accountId = fromAccountId, amount = amount, type = TransactionType.TRANSFER, category = "انتقال داخلی", note = note))
        dao.addTransaction(TransactionEntity(accountId = toAccountId, amount = amount, type = TransactionType.TRANSFER, category = "انتقال داخلی", note = note))
        dao.changeBalance(fromAccountId, -amount); dao.changeBalance(toAccountId, amount)
    }
    suspend fun settle(obligation: ObligationEntity, accountId: Long, received: Boolean) = database.withTransaction {
        val type = if (received) TransactionType.RECEIVABLE_COLLECTION else TransactionType.DEBT_PAYMENT
        dao.addTransaction(TransactionEntity(accountId = accountId, amount = obligation.amount, type = type, category = obligation.type.name, note = obligation.title))
        dao.changeBalance(accountId, if (received) obligation.amount else -obligation.amount)
        dao.updateObligation(obligation.copy(status = if (received) ObligationStatus.RECEIVED else ObligationStatus.PAID, accountId = accountId))
    }
}
