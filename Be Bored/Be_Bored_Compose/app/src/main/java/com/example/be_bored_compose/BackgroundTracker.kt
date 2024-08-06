package com.example.be_bored_compose

import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.IBinder
import com.example.be_bored_compose.database.Database
import com.example.be_bored_compose.database.Stats
import java.util.*
import android.util.Log
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.be_bored_compose.database.Goals
import kotlin.coroutines.coroutineContext

class BackgroundTracker : Service() {
    var created = false
    var goals_progression = HashMap<Goals, String> ()
    // kategoria - aplikacje z kategorii

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onTaskRemoved(intent)
        if (!created) {
            created = true;
//            Database.db.dao().nukeTable()
//            updateDatabase()
//            Log.d("ddd",(System.currentTimeMillis() - 1000*3600*24).toString())
//            for(el in Database.db.dao().getAll())
//                Log.d("ddd",el.toString())
//            Log.d("baseSize",Database.db.dao().getAll().size.toString())
            val handler = Handler()
            val runnable: Runnable = object : Runnable {
                override fun run() {
                    println("HI")
                    updateDatabase()
                    check()
                    handler.postDelayed(this, 900000)
                }

            }
            handler.postDelayed(runnable, 900000)
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }


private fun getGoalProgress(goal : Goals) : String {
    val stats = arrayOf("hours","entries","notifications")
    val timeIntervals = arrayOf("day","week","month","daily for week","daily for month","weekly for month")
    val intervalsList = ArrayList<ArrayList<Stats>>()
    val database = Database.db.dao()
    val dayInMilis : Long = 1000*60*60*24
    val hourInMilis : Long = 1000*60*60
    val today = System.currentTimeMillis() - System.currentTimeMillis()%dayInMilis
    if(goal.appTypeOrName == null)
        return ""

    when(goal.timeInterval){
        "day" ->  intervalsList.add(database.getDataByDayAndNameOrType(today,goal.appTypeOrName) as ArrayList<Stats>)
        "week" ->  {
            intervalsList.add(ArrayList<Stats>())
            for(day in 0 until 7) {
                intervalsList[0].addAll (
                    database.getDataByDayAndNameOrType(
                        today - day * dayInMilis,
                        goal.appTypeOrName
                    )
                )
                Log.d("perWeek", intervalsList[0].size.toString())
            }
        }
        "month" ->  {
            intervalsList.add(ArrayList<Stats>())
            for(day in 0 until 30)
                intervalsList[0].plus(database.getDataByDayAndNameOrType(today-day*dayInMilis,goal.appTypeOrName))
        }
        "daily for week" ->{
            for(day in 0 until 7){
                intervalsList.add(database.getDataByDayAndNameOrType(today-day*dayInMilis,goal.appTypeOrName) as ArrayList<Stats>)
            }
        }
        "daily for month" ->{
            for(day in 0 until 30){
                intervalsList.add(database.getDataByDayAndNameOrType(today-day*dayInMilis,goal.appTypeOrName) as ArrayList<Stats>)
            }
        }

        "weekly for month" ->{
            for(week in 0 until 4){
                val list = ArrayList<Stats>()
                for(day in 0 until 7)
                    list.addAll(database.getDataByDayAndNameOrType(today-(day+7*week)*dayInMilis,goal.appTypeOrName))
                intervalsList.add(list)
            }
        }
        else -> {}
    }
    var winPercentage = 0f
    for(list in intervalsList){
        var wins = 0f
        var sum = 0
        when(goal.statTracked){
            "hours" -> sum = ((list.sumOf { it.useTime?.toInt() ?: 0 }) / hourInMilis).toInt()
            "entries" -> sum = list.sumOf { it.timesEntered ?: 0 }
            "notifications" -> sum = list.sumOf { it.notificationsSent ?: 0 }
            else -> 0
        }
        if(sum < goal.upperLimit!!)
            wins += 1f

        winPercentage += wins/intervalsList.size
    }
    if(intervalsList.size == 0)
        winPercentage = 1f
//    Log.d("tag",intervalsList[0].size.toString())
    if(winPercentage == 1f)
        return "Excellent!"
    if(winPercentage > 0.75)
        return "Great!"
    if(winPercentage > 0.5)
        return "Ok!"
    if(winPercentage > 0.25)
        return "Lame!"
    if(winPercentage > 0.0)
        return "BAD!"

    return "YOU ARE THE WORST"
}

private fun showAlert(title: String, message: String, image: Int) {
    val notification = NotificationCompat.Builder(this, "alerts")
        .setSmallIcon(R.drawable.ic_stat_name)
        .setContentTitle(title)
        .setContentText(message)
        .setLargeIcon(BitmapFactory.decodeResource(this.resources, image))
        .build()
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(1, notification)
}

fun check() {
    val dayInMilis : Long = 1000*60*60*24
    val today = System.currentTimeMillis() - System.currentTimeMillis()%dayInMilis

    for (goal in Database.db.dao().getAllGoals()) {

        if (goals_progression.contains(goal)) {
            if (goals_progression[goal] != getGoalProgress(goal)) {
                showAlert(goal.appTypeOrName!!, getGoalProgress(goal), R.drawable.sad_dog)
            }
        } else {
            goals_progression.put(goal, getGoalProgress(goal))
        }

    }

}


    fun updateDatabase(){
        val day = 86400000L
        val week = day * 7
        val month = day * 30
        val context = this
        // śledzone kategorie z aplikacjami
        val trackedPackages = TrackedApps.apps
        val database = Database.db.dao()

        for(key in trackedPackages.keys){
            for(appName in trackedPackages[key]!!){
                val app = TrackedApps.appPackages[appName]!!
                val timeDay = GetTimeDay(context ,app)
                val openDay = GetOpenDay(context, app)
                val notifDay = GetNotifiDay(context, app)

                val statDay = Stats(appName,key,System.currentTimeMillis() - day,timeDay,notifDay,openDay,day)
                database.insertAll(statDay)

                val timeWeek = GetTimeWeek(context ,app)
                val openWeek = GetOpenWeek(context, app)
                val notifWeek = GetNotifiWeek(context, app)

                val statWeek = Stats(appName,key,System.currentTimeMillis() - week,timeWeek,notifWeek,openWeek,week)
                database.insertAll(statWeek)

                val timeMonth = GetTimeMonth(context ,app)
                val openMonth = GetOpenMonth(context, app)
                val notifMonth = GetNotifiMonth(context, app)

                val statMonth = Stats(appName,key,System.currentTimeMillis() - month,timeMonth,notifMonth,openMonth,month)
                database.insertAll(statMonth)
            }
        }
    }

    private fun GetTimeDay(context: Context, packageName: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val endTime = System.currentTimeMillis()

        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        var totalUsageTime: Long = 0

        for (usageStats in usageStatsList) {
            if (usageStats.packageName == packageName) {
                totalUsageTime += usageStats.totalTimeInForeground
            }
        }

        return totalUsageTime // ( 60 * 1000 )
    }
    private fun GetTimeWeek(context: Context, packageName: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val endTime = System.currentTimeMillis()

        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, startTime, endTime)

        var totalUsageTime: Long = 0

        for (usageStats in usageStatsList) {
            if (usageStats.packageName == packageName) {
                totalUsageTime += usageStats.totalTimeInForeground
            }
        }

        return totalUsageTime // ( 60 * 1000 )
    }
    private fun GetTimeMonth(context: Context, packageName: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val endTime = System.currentTimeMillis()

        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, startTime, endTime)

        var totalUsageTime: Long = 0

        for (usageStats in usageStatsList) {
            if (usageStats.packageName == packageName) {
                totalUsageTime += usageStats.totalTimeInForeground
            }
        }

        return totalUsageTime // ( 60 * 1000 )
    }
    private fun GetOpenDay(context: Context, packageName: String): Int {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val events = usageStatsManager.queryEvents(startTime, endTime)

        var openCount = 0
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == packageName && event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                openCount++
            }
        }

        return openCount
    }

    private fun GetOpenWeek(context: Context, packageName: String): Int {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        val startTime = calendar.timeInMillis

        val events = usageStatsManager.queryEvents(startTime, endTime)

        var openCount = 0
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == packageName && event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                openCount++
            }
        }

        return openCount
    }

    private fun GetOpenMonth(context: Context, packageName: String): Int {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.MONTH, -1)
        val startTime = calendar.timeInMillis

        val events = usageStatsManager.queryEvents(startTime, endTime)

        var openCount = 0
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.packageName == packageName && event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                openCount++
            }
        }

        return openCount
    }

    private fun GetNotifiDay(context: Context, packageName: String): Int {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - UsageStatsManager.INTERVAL_DAILY

        var notificationCount = 0

        val activeNotifications = notificationManager.activeNotifications
        for (notification in activeNotifications) {
            if (notification.packageName == packageName && notification.postTime in startTime..currentTime) {
                notificationCount++
            }
        }

        return notificationCount
    }

    private fun GetNotifiWeek(context: Context, packageName: String): Int {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - UsageStatsManager.INTERVAL_WEEKLY

        var notificationCount = 0

        val activeNotifications = notificationManager.activeNotifications
        for (notification in activeNotifications) {
            if (notification.packageName == packageName && notification.postTime in startTime..currentTime) {
                notificationCount++
            }
        }

        return notificationCount
    }

    private fun GetNotifiMonth(context: Context, packageName: String): Int {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - UsageStatsManager.INTERVAL_MONTHLY

        var notificationCount = 0

        val activeNotifications = notificationManager.activeNotifications
        for (notification in activeNotifications) {
            if (notification.packageName == packageName && notification.postTime in startTime..currentTime) {
                notificationCount++
            }
        }

        return notificationCount
    }


}