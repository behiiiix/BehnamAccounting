package ir.behnam.accounting.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import ir.behnam.accounting.*
import ir.behnam.accounting.R
import ir.behnam.accounting.data.*
import android.app.DatePickerDialog
import java.util.Calendar

private enum class Tab(val title: String) { HOME("خانه"), TRANSACTIONS("تراکنش‌ها"), OBLIGATIONS("سررسیدها"), REPORTS("گزارش‌ها") }

@Composable fun BehnamApp(vm: FinanceViewModel) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }; var addTransaction by remember { mutableStateOf(false) }; var addAccount by remember { mutableStateOf(false) }; var addObligation by remember { mutableStateOf(false) }; var transferMoney by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Row(verticalAlignment = Alignment.CenterVertically) { Image(painter = painterResource(R.drawable.logo_behnam), contentDescription = "لوگوی بهنام", modifier = Modifier.size(34.dp)); Spacer(Modifier.width(8.dp)); Text("بهنام | حسابداری شخصی", fontWeight = FontWeight.Bold) } }, actions = { IconButton(onClick = { addAccount = true }) { Icon(Icons.Default.AccountBalance, "افزودن حساب") } }) },
        bottomBar = { NavigationBar { Tab.entries.forEach { item -> NavigationBarItem(selected = tab == item, onClick = { tab = item }, icon = { Icon(if (item == Tab.HOME) Icons.Default.Home else if (item == Tab.TRANSACTIONS) Icons.Default.ReceiptLong else if (item == Tab.OBLIGATIONS) Icons.Default.Event else Icons.Default.BarChart, item.title) }, label = { Text(item.title) }) } } },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = { if (tab != Tab.REPORTS) FloatingActionButton(onClick = { if (tab == Tab.OBLIGATIONS) addObligation = true else addTransaction = true }, containerColor = Emerald, contentColor = Navy) { Icon(Icons.Default.Add, "افزودن", modifier = Modifier.size(34.dp)) } }) { padding ->
        Box(Modifier.padding(padding)) { when (tab) { Tab.HOME -> Dashboard(vm, { transferMoney = true }, { addTransaction = true }); Tab.TRANSACTIONS -> Transactions(vm); Tab.OBLIGATIONS -> Obligations(vm); Tab.REPORTS -> Reports(vm) } }
    }
    if (addTransaction) TransactionDialog(vm, { addTransaction = false }); if (addAccount) AccountDialog(vm, { addAccount = false }); if (addObligation) ObligationDialog(vm, { addObligation = false }); if (transferMoney) TransferDialog(vm, { transferMoney = false })
}

@Composable private fun Dashboard(vm: FinanceViewModel, onTransfer: () -> Unit, onAddTransaction: () -> Unit) {
    val accounts by vm.accounts.collectAsState(); val obligations by vm.obligations.collectAsState(); val transactions by vm.transactions.collectAsState(); val context = LocalContext.current
    val income = transactions.filter { it.type == TransactionType.INCOME || it.type == TransactionType.RECEIVABLE_COLLECTION }.sumOf { it.amount }
    val expense = transactions.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.DEBT_PAYMENT }.sumOf { it.amount }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { OutlinedButton(onClick = { }, shape = RoundedCornerShape(16.dp)) { Text("پروفایل شخصی  ⌄") }; IconButton(onClick = { }) { Icon(Icons.Default.Notifications, "یادآوری‌ها", tint = Emerald) } } }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) { Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF193452), NavyCard)), RoundedCornerShape(22.dp)).padding(22.dp), horizontalAlignment = Alignment.End) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Icon(Icons.Default.Visibility, "نمایش موجودی"); Text("موجودی کل", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Spacer(Modifier.height(10.dp)); Text(toman(accounts.sumOf { it.balance }), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold); Text("تومان", color = MaterialTheme.colorScheme.onSurfaceVariant); HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = .12f)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) { TextButton(onClick = onTransfer) { Icon(Icons.Default.SwapHoriz, null); Spacer(Modifier.width(4.dp)); Text("انتقال") }; TextButton(onClick = onAddTransaction) { Icon(Icons.Default.AddCircleOutline, null); Spacer(Modifier.width(4.dp)); Text("افزودن تراکنش") } } } } }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(accounts) { AccountTile(it) }; if (accounts.isEmpty()) item { Card(colors = CardDefaults.cardColors(containerColor = NavyCard)) { Text("از بالا یک حساب اضافه کنید", Modifier.padding(20.dp)) } } } }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = NavyCard)) { Column(Modifier.padding(18.dp)) { Text("درآمد و هزینه", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.height(14.dp)); MetricBar("درآمد", income, Emerald); Spacer(Modifier.height(10.dp)); MetricBar("هزینه", expense, Coral); HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color.White.copy(.12f)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("درآمد", color = Emerald); Text(toman(income), fontWeight = FontWeight.Bold) }; Column(horizontalAlignment = Alignment.End) { Text("هزینه", color = Coral); Text(toman(expense), fontWeight = FontWeight.Bold) } } } } }
        item { Text("سررسیدهای نزدیک", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(obligations.filter { it.status == ObligationStatus.OPEN }.take(3)) { item -> Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = NavyCard)) { ListItem(headlineContent = { Text(item.title, fontWeight = FontWeight.SemiBold) }, supportingContent = { Text("${systemDate(context, item.dueAt)}  •  ${toman(item.amount)}") }, leadingContent = { Icon(if (item.type == ObligationType.CHEQUE) Icons.Default.Receipt else Icons.Default.Alarm, null, tint = Emerald) }) } }
        if (obligations.none { it.status == ObligationStatus.OPEN }) item { Text("سررسید نزدیکی ثبت نشده است.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun AccountTile(account: AccountEntity) { val accent = when (account.type) { AccountType.BANK -> Color(0xFF4F9DFF); AccountType.CASH -> Emerald; AccountType.WALLET -> Color(0xFFFF9A57) }
    Card(Modifier.width(210.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = NavyCard), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(.65f))) { Column(Modifier.padding(16.dp)) { Icon(Icons.Default.AccountBalanceWallet, null, tint = accent); Spacer(Modifier.height(12.dp)); Text(account.name, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(18.dp)); Text("موجودی", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(toman(account.balance), color = accent, fontWeight = FontWeight.Bold) } }
}

@Composable private fun MetricBar(label: String, amount: Long, color: Color) { val fill = if (amount == 0L) .05f else .75f
    Row(verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.width(55.dp)); LinearProgressIndicator(progress = { fill }, Modifier.weight(1f).height(10.dp), color = color, trackColor = Color.White.copy(.12f)); Spacer(Modifier.width(8.dp)); Text(toman(amount), color = color, style = MaterialTheme.typography.labelSmall) }
}

@Composable private fun Transactions(vm: FinanceViewModel) { val data by vm.transactions.collectAsState(); val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(12.dp)) { items(data) { tx -> ListItem(headlineContent = { Text(tx.category) }, supportingContent = { Text("${systemDate(context, tx.occurredAt)}  ${tx.note}") }, trailingContent = { Text((if (tx.type == TransactionType.INCOME) "+" else "−") + toman(tx.amount), color = if (tx.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }) }; if (data.isEmpty()) item { Empty("هنوز تراکنشی ثبت نشده است.") } }
}

@Composable private fun Obligations(vm: FinanceViewModel) { val data by vm.obligations.collectAsState(); val accounts by vm.accounts.collectAsState(); val context = LocalContext.current; var settling by remember { mutableStateOf<ObligationEntity?>(null) }
    LazyColumn(Modifier.fillMaxSize().padding(12.dp)) { items(data.filter { it.status == ObligationStatus.OPEN }) { debt -> ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) { Column(Modifier.padding(14.dp)) { Text(debt.title, fontWeight = FontWeight.Bold); Text("${debt.type} • ${toman(debt.amount)} • ${systemDate(context, debt.dueAt)}"); if (accounts.isNotEmpty()) Button(onClick = { settling = debt }) { Text(if (debt.type == ObligationType.RECEIVABLE) "تأیید دریافت" else "تأیید پرداخت") } } } }; if (data.none { it.status == ObligationStatus.OPEN }) item { Empty("بدهی، طلب، چک یا قسط بازی ثبت نشده است.") } }
    settling?.let { debt -> SettleDialog(vm, debt, accounts, { settling = null }) }
}

@Composable private fun Reports(vm: FinanceViewModel) { val data by vm.transactions.collectAsState(); val income = data.filter { it.type == TransactionType.INCOME || it.type == TransactionType.RECEIVABLE_COLLECTION }.sumOf { it.amount }; val expense = data.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.DEBT_PAYMENT }.sumOf { it.amount }; val groups = data.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.DEBT_PAYMENT }.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }.toList().sortedByDescending { it.second }.take(5)
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("گزارش کلی", style = MaterialTheme.typography.headlineSmall) }; item { Text("کل درآمد: ${toman(income)}"); Text("کل هزینه: ${toman(expense)}") }; item { Text("بیشترین دسته‌های هزینه", style = MaterialTheme.typography.titleLarge) }; items(groups) { (category, amount) -> ListItem(headlineContent = { Text(category) }, trailingContent = { Text(toman(amount)) }) }; if (groups.isEmpty()) item { Empty("برای ساخت گزارش، تراکنش ثبت کنید.") } }
}

@Composable private fun Empty(text: String) = Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) { Text(text) }

@Composable private fun TransferDialog(vm: FinanceViewModel, dismiss: () -> Unit) { val accounts by vm.accounts.collectAsState(); var fromId by remember(accounts) { mutableStateOf(accounts.firstOrNull()?.id) }; var toId by remember(accounts) { mutableStateOf(accounts.getOrNull(1)?.id) }; var amount by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss, title = { Text("انتقال بین حساب‌ها") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("مبدأ") ; accounts.forEach { account -> FilterChip(selected = fromId == account.id, onClick = { fromId = account.id }, label = { Text(account.name) }) }; Text("مقصد"); accounts.forEach { account -> FilterChip(selected = toId == account.id, onClick = { toId = account.id }, label = { Text(account.name) }) }; OutlinedTextField(value = amount, onValueChange = { amount = it.filter(Char::isDigit) }, label = { Text("مبلغ (تومان)") }); if (accounts.size < 2) Text("برای انتقال حداقل دو حساب لازم است.", color = MaterialTheme.colorScheme.error) } }, confirmButton = { TextButton(onClick = { val value = amount.toLongOrNull(); if (fromId != null && toId != null && value != null && fromId != toId) { vm.transfer(fromId!!, toId!!, value); dismiss() } }) { Text("انتقال") } }, dismissButton = { TextButton(onClick = dismiss) { Text("انصراف") } })
}

@Composable private fun SettleDialog(vm: FinanceViewModel, debt: ObligationEntity, accounts: List<AccountEntity>, dismiss: () -> Unit) { var accountId by remember(accounts) { mutableStateOf(accounts.firstOrNull()?.id) }
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (debt.type == ObligationType.RECEIVABLE) "تأیید دریافت طلب" else "تأیید پرداخت") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("${debt.title} — ${toman(debt.amount)}"); Text(if (debt.type == ObligationType.RECEIVABLE) "حساب واریز را انتخاب کنید:" else "حساب پرداخت را انتخاب کنید:"); accounts.forEach { account -> FilterChip(selected = accountId == account.id, onClick = { accountId = account.id }, label = { Text("${account.name} (${toman(account.balance)})") }) } } }, confirmButton = { TextButton(onClick = { accountId?.let { vm.settle(debt, it); dismiss() } }) { Text("تأیید نهایی") } }, dismissButton = { TextButton(onClick = dismiss) { Text("انصراف") } })
}

@Composable private fun AccountDialog(vm: FinanceViewModel, dismiss: () -> Unit) { var name by remember { mutableStateOf("") }; var openingBalance by remember { mutableStateOf("") }; var type by remember { mutableStateOf(AccountType.BANK) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("افزودن حساب") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام حساب؛ مثلاً بانک پارسیان") }); OutlinedTextField(value = openingBalance, onValueChange = { openingBalance = it.filter(Char::isDigit) }, label = { Text("مانده اولیه (تومان)") }); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { AccountType.entries.forEach { value -> FilterChip(selected = type == value, onClick = { type = value }, label = { Text(when (value) { AccountType.BANK -> "بانکی"; AccountType.CASH -> "نقدی"; AccountType.WALLET -> "کیف پول" }) }) } } } }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) { vm.addAccount(name, type, openingBalance.toLongOrNull() ?: 0); dismiss() } }) { Text("ثبت") } }, dismissButton = { TextButton(onClick = dismiss) { Text("انصراف") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TransactionDialog(vm: FinanceViewModel, dismiss: () -> Unit) {
    val accounts by vm.accounts.collectAsState()
    var amount by remember { mutableStateOf("") }; var title by remember { mutableStateOf("") }; var category by remember { mutableStateOf("") }; var income by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var accountId by remember(accounts) { mutableStateOf(accounts.firstOrNull()?.id) }
    val selectedAccount = accounts.firstOrNull { it.id == accountId }
    val quickCategories = listOf("خوراک", "رفت‌وآمد", "قبوض", "پوشاک", "خرید روزمره", "تفریح")
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (income) "ثبت سریع درآمد" else "ثبت سریع هزینه") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row { FilterChip(selected = !income, onClick = { income = false }, label = { Text("هزینه") }); Spacer(Modifier.width(8.dp)); FilterChip(selected = income, onClick = { income = true }, label = { Text("درآمد") }) }
            OutlinedTextField(value = amount, onValueChange = { amount = it.filter(Char::isDigit) }, label = { Text("مبلغ (تومان)") })
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(if (income) "عنوان؛ مثلاً حقوق" else "عنوان؛ مثلاً خرید سیگار") })
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("دسته‌بندی") })
            if (!income) Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { quickCategories.take(3).forEach { item -> AssistChip(onClick = { category = item }, label = { Text(item) }) } }
            ExposedDropdownMenuBox(expanded = accountExpanded, onExpandedChange = { accountExpanded = it }) {
                OutlinedTextField(value = selectedAccount?.let { "${it.name} — ${toman(it.balance)}" } ?: "حساب پرداخت را انتخاب کنید", onValueChange = {}, readOnly = true, label = { Text(if (income) "حساب واریز" else "حساب پرداخت") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(accountExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false }) { accounts.forEach { account -> DropdownMenuItem(text = { Text("${account.name} — ${toman(account.balance)}") }, onClick = { accountId = account.id; accountExpanded = false }) } }
            }
            if (accounts.isEmpty()) Text("ابتدا از بالای صفحه یک حساب بانکی یا نقدی ایجاد کنید.", color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(onClick = { val value = amount.toLongOrNull(); if (value != null && title.isNotBlank() && accountId != null) { vm.record(accountId!!, value, income, category.ifBlank { if (income) "سایر درآمدها" else "سایر هزینه‌ها" }, title); dismiss() } }) { Text("ثبت") } }, dismissButton = { TextButton(onClick = dismiss) { Text("انصراف") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ObligationDialog(vm: FinanceViewModel, dismiss: () -> Unit) { val context = LocalContext.current; var title by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var type by remember { mutableStateOf(ObligationType.DEBT) }; var dueAt by remember { mutableLongStateOf(System.currentTimeMillis()) }; var expanded by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("ثبت سررسید") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان / طرف حساب") }); OutlinedTextField(value = amount, onValueChange = { amount = it.filter(Char::isDigit) }, label = { Text("مبلغ (تومان)") }); ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) { OutlinedTextField(value = when(type) { ObligationType.DEBT -> "بدهی"; ObligationType.RECEIVABLE -> "طلب"; ObligationType.INSTALLMENT -> "قسط"; ObligationType.CHEQUE -> "چک" }, onValueChange = {}, readOnly = true, label = { Text("نوع") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor()); ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { ObligationType.entries.forEach { value -> DropdownMenuItem(text = { Text(value.name) }, onClick = { type = value; expanded = false }) } } }; OutlinedButton(onClick = { val c = Calendar.getInstance(); DatePickerDialog(context, { _, y, m, d -> c.set(y, m, d, 12, 0, 0); dueAt = c.timeInMillis }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }) { Text("تاریخ سررسید: ${systemDate(context, dueAt)}") } } }, confirmButton = { TextButton(onClick = { val value = amount.toLongOrNull(); if (title.isNotBlank() && value != null) { vm.addObligation(title, value, type, dueAt); dismiss() } }) { Text("ثبت") } }, dismissButton = { TextButton(onClick = dismiss) { Text("انصراف") } })
}
