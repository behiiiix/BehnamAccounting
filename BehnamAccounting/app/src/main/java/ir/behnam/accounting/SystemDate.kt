package ir.behnam.accounting

import android.content.Context
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

/** Formatting defers to the phone's selected language/calendar. Data itself remains an epoch timestamp. */
fun systemDate(context: Context, time: Long): String = android.text.format.DateFormat.getDateFormat(context).format(Date(time))
fun toman(amount: Long): String = NumberFormat.getNumberInstance(Locale("fa", "IR")).format(amount) + " تومان"
