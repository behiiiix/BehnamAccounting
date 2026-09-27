package ir.behnam.accounting.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

class DbConverters {
    @TypeConverter fun accountType(value: AccountType) = value.name
    @TypeConverter fun accountType(value: String) = AccountType.valueOf(value)
    @TypeConverter fun transactionType(value: TransactionType) = value.name
    @TypeConverter fun transactionType(value: String) = TransactionType.valueOf(value)
    @TypeConverter fun obligationType(value: ObligationType) = value.name
    @TypeConverter fun obligationType(value: String) = ObligationType.valueOf(value)
    @TypeConverter fun obligationStatus(value: ObligationStatus) = value.name
    @TypeConverter fun obligationStatus(value: String) = ObligationStatus.valueOf(value)
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM accounts ORDER BY name") fun accounts(): Flow<List<AccountEntity>>
    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC") fun transactions(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM obligations ORDER BY dueAt") fun obligations(): Flow<List<ObligationEntity>>
    @Insert suspend fun addAccount(account: AccountEntity): Long
    @Insert suspend fun addTransaction(transaction: TransactionEntity): Long
    @Insert suspend fun addObligation(obligation: ObligationEntity): Long
    @Query("UPDATE accounts SET balance = balance + :delta WHERE id = :accountId") suspend fun changeBalance(accountId: Long, delta: Long)
    @Update suspend fun updateObligation(obligation: ObligationEntity)
}

@TypeConverters(DbConverters::class)
@Database(entities = [AccountEntity::class, TransactionEntity::class, ObligationEntity::class], version = 1, exportSchema = false)
abstract class FinanceDatabase : RoomDatabase() { abstract fun dao(): FinanceDao }
