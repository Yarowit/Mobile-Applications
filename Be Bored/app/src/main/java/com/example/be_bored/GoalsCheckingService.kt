package com.example.be_bored

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.example.be_bored.database.Database

class GoalsCheckingService : Service() {

    var created = false;

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onTaskRemoved(intent)
        if (!created) {
            created = true;
            val handler = Handler()
            val runnable: Runnable = object : Runnable {
                override fun run() {
                    check()
                    handler.postDelayed(this, 10000)
                }
            }
            handler.postDelayed(runnable, 10000)
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }
}

fun check() {
    val dayInMilis : Long = 1000*60*60*24
    val today = System.currentTimeMillis() - System.currentTimeMillis()%dayInMilis
    val x = Database.db.dao().getDailyUseTimeByDateAndType(today - dayInMilis,"Entertainment").toFloat()/1000/60/60
    //TODO: SEND NOTIFICATION HERE

    println(x)
}