package ir.behnam.accounting.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType { BANK, CASH, WALLET }
enum class TransactionType { INCOME, EXPENSE, TRANSFER, DEBT_PAYMENT, RECEIVABLE_COLLECTION }
enum class ObligationType { DEBT, RECEIVABLE, INSTALLMENT, CHEQUE }
enum class ObligationStatus { OPEN, PAID, RECEIVED, CANCELLED }

@Entity(tableName = "accounts")
data class AccountEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val type: AccountType, val balance: Long = 0)

@Entity(tableName = "transactions")
data class TransactionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val accountId: Long, val amount: Long, val type: TransactionType, val category: String, val note: String = "", val occurredAt: Long = System.currentTimeMillis())

@Entity(tableName = "obligations")
data class ObligationEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val amount: Long, val type: ObligationType, val dueAt: Long, val remindDaysBefore: Int = 3, val status: ObligationStatus = ObligationStatus.OPEN, val accountId: Long? = null, val note: String = "")
