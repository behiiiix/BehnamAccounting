package ir.behnam.accounting

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ir.behnam.accounting.data.FinanceDatabase
import ir.behnam.accounting.data.FinanceRepository
import ir.behnam.accounting.ui.BehnamApp
import ir.behnam.accounting.ui.BehnamTheme

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork("deadline-check", ExistingPeriodicWorkPolicy.UPDATE, PeriodicWorkRequestBuilder<DeadlineWorker>(1, java.util.concurrent.TimeUnit.DAYS).build())
        val database = Room.databaseBuilder(applicationContext, FinanceDatabase::class.java, "behnam.db").build()
        val factory = FinanceViewModelFactory(FinanceRepository(database))
        setContent { BehnamTheme { BehnamApp(viewModel(factory = factory)) } }
    }
}
