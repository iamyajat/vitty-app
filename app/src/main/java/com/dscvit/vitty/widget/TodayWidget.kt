package com.dscvit.vitty.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.dscvit.vitty.R
import com.dscvit.vitty.activity.AuthActivity
import com.dscvit.vitty.service.TodayWidgetService
import com.dscvit.vitty.util.Constants.PERIODS
import com.dscvit.vitty.util.Constants.TIME_SLOTS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Implementation of App Widget functionality.
 */
class TodayWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateTodayWidget(context, appWidgetManager, appWidgetId, null, null)
        }
    }

//    override fun onAppWidgetOptionsChanged(
//        context: Context?,
//        appWidgetManager: AppWidgetManager?,
//        appWidgetId: Int,
//        newOptions: Bundle?
//    ) {
//        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
//        if (context != null) {
//            if (appWidgetManager != null) {
//                updateTodayWidget(context, appWidgetManager, appWidgetId, null, null)
//            }
//        }
//    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateTodayWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    courseList: ArrayList<String>?,
    timeList: ArrayList<String>?
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.today_widget)
    val intent = Intent(context, AuthActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
    views.setOnClickPendingIntent(R.id.class_next, pendingIntent)
    val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
    val calendar: Calendar = Calendar.getInstance()
    val d = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    if (courseList == null) {
        fetchTodayFirestore(context, days[d], appWidgetManager, appWidgetId)
    } else if (courseList.isNotEmpty()) {
        saveArray(courseList, "courses_today", context)
        saveArray(timeList!!, "time_today", context)

        val serviceIntent = Intent(context, TodayWidgetService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val bundle1 = Bundle()
        bundle1.putStringArrayList(
            PERIODS,
            courseList
        )
        val bundle2 = Bundle()
        bundle2.putStringArrayList(
            TIME_SLOTS,
            timeList
        )
//        serviceIntent.putExtra(PERIODS, bundle1)
//        serviceIntent.putExtra(TIME_SLOTS, bundle2)
        views.setRemoteAdapter(R.id.periods, serviceIntent)

        // Button Intent
//        val buttonIntent = Intent(context, AuthActivity::class.java)
//        val buttonPendingIntent = PendingIntent.getActivity(context, 0, buttonIntent, 0)
//        views.setOnClickPendingIntent(R.id.periods, buttonPendingIntent)

        Timber.d("WTAF NOBRUH")
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.periods)
    }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun saveArray(array: ArrayList<String>, arrayName: String, context: Context): Boolean {
    val prefs = context.getSharedPreferences("login_info", 0)
    val editor = prefs.edit()
    editor.putInt(arrayName + "_size", array.size)
    for (i in array.indices) editor.putString(arrayName + "_" + i, array[i])
    return editor.commit()
}

fun fetchTodayFirestore(
    context: Context,
    day: String,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) =
    runBlocking {
        fetchTodayData(context, day, appWidgetManager, appWidgetId)
    }

suspend fun fetchTodayData(
    context: Context,
    day: String,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) =
    coroutineScope {
        val db = FirebaseFirestore.getInstance()
        val sharedPref = context.getSharedPreferences("login_info", Context.MODE_PRIVATE)!!
        val uid = sharedPref.getString("uid", "")
        val courseList: ArrayList<String> = ArrayList()
        val timeList: ArrayList<String> = ArrayList()
        if (uid != null && uid != "") {
            db.collection("users")
                .document(uid)
                .collection("timetable")
                .document(day)
                .collection("periods")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        try {
                            val startTime: Date = document.getTimestamp("startTime")!!.toDate()
                            val simpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val sTime: String =
                                simpleDateFormat.format(startTime).toUpperCase(Locale.ROOT)

                            val endTime: Date = document.getTimestamp("endTime")!!.toDate()
                            val eTime: String =
                                simpleDateFormat.format(endTime).toUpperCase(Locale.ROOT)

                            courseList.add(document.getString("courseName")!!)
                            timeList.add("$sTime - $eTime")
                        } catch (e: Exception) {
                        }
                    }
                    updateTodayWidget(context, appWidgetManager, appWidgetId, courseList, timeList)
                }
                .addOnFailureListener { e ->
                    Timber.d("Error: $e")
                }
        } else {
            updateTodayWidget(context, appWidgetManager, appWidgetId, courseList, timeList)
        }
    }