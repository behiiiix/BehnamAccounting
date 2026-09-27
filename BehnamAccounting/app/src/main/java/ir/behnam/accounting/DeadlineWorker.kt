package ir.behnam.accounting

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.behnam.accounting.data.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/** Checks open debts, receivables, cheques and installments once each day. */
class DeadlineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = Room.databaseBuilder(applicationContext, FinanceDatabase::class.java, "behnam.db").build()
        val now = System.currentTimeMillis()
        val due = db.dao().obligations().first().filter { item ->
            item.status == ObligationStatus.OPEN && item.dueAt <= now + TimeUnit.DAYS.toMillis(item.remindDaysBefore.toLong()) && item.dueAt >= now - TimeUnit.DAYS.toMillis(1)
        }
        if (due.isNotEmpty()) notify(due); db.close(); return Result.success()
    }
    private fun notify(items: List<ObligationEntity>) {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("deadlines", "یادآوری سررسیدها", NotificationManager.IMPORTANCE_HIGH))
        manager.notify(20, NotificationCompat.Builder(applicationContext, "deadlines").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("سررسید مالی نزدیک است").setContentText("${items.size} مورد نیاز به بررسی دارد.").setPriority(NotificationCompat.PRIORITY_HIGH).build())
    }
}
