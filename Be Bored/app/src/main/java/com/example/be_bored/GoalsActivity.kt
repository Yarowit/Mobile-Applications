package com.example.be_bored

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.children
import androidx.room.Room
import com.example.be_bored.database.AppDatabase
import com.example.be_bored.database.Database
import com.example.be_bored.database.Goals
import com.example.be_bored.database.Stats

class GoalsActivity : AppCompatActivity() {
    // less than <X> <entrie/hours> a <day,week,...> in <appname/type>
    data class goalView(val g:Goals,val v:View)
    val goalList = ArrayList<Goals>()
    var goalToViewId = ArrayList<goalView>()

    var amount : Long = 0
    var stat = ""
    var interval = ""
    var app = ""

    val stats = arrayOf("hours","entries","notifications")
    val timeIntervals = arrayOf("day","week","month","daily for week","daily for month","weekly for month")


    fun add(view: View){
        view.visibility = View.INVISIBLE
        (view.parent as CardView).findViewById<LinearLayout>(R.id.edit).visibility = View.VISIBLE

        // goalStat tracked
        val goalStat = (view.parent as CardView).findViewById<Spinner>(R.id.goalStat)
        goalStat.adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,stats)
        goalStat.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                stat = parent.getItemAtPosition(pos) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>){}
        }

        // goalType
        val goalType = (view.parent as CardView).findViewById<Spinner>(R.id.goalInterval)
        goalType.adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,timeIntervals)
        goalType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                interval = parent.getItemAtPosition(pos) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>){}
        }

        // goalApp
        val goalApp = (view.parent as CardView).findViewById<Spinner>(R.id.goalApp)
        val items = Database.db.dao().getAppTypes().plus(Database.db.dao().getAppNames())
        goalApp.adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,items)
        goalApp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                app = parent.getItemAtPosition(pos) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>){}
        }
    }

    fun remove(view: View){
//        var i = 0
//        for(view in findViewById<LinearLayout>(R.id.layout).children)
//            view.tag = i++
        val lay = findViewById<LinearLayout>(R.id.layout)
        for(i in 0 until lay.childCount)
            lay.getChildAt(i).findViewById<TextView>(R.id.minus).tag = i
//        for(i in 0 until lay.childCount)
//            Log.d("o",lay.getChildAt(i).tag.toString())
        //removal
        Log.d("test","test")
        Log.d("testt",(view.tag as Int).toString())
        Log.d("chc",findViewById<LinearLayout>(R.id.layout).childCount.toString())
//        for()
        Log.d("Size",Database.db.dao().getAllGoals().size.toString())
        Log.d("goal Size",goalList.size.toString())

//        for(el in Database.db.dao().getAllGoals())
//            if(el.appTypeOrName == )

        Database.db.dao().delete(goalList[view.tag as Int])
        goalList.removeAt(view.tag as Int)

        findViewById<LinearLayout>(R.id.layout).removeView(view.parent.parent as CardView)

        // tagi
//        var i = 0
//        for(view in findViewById<LinearLayout>(R.id.layout).children)
//            view.tag = i++
    }

    fun confirm(view: View){
        // ustaw amount
        amount = (view.parent as LinearLayout).findViewById<EditText>(R.id.goalLimit).text.toString().toLong()

        val newGoal = Goals(stat,amount,interval, app)
        (view.parent.parent as CardView).findViewById<TextView>(R.id.goalName).text = "Less than ${newGoal.upperLimit} ${newGoal.statTracked} a ${newGoal.timeInterval} in ${newGoal.appTypeOrName}"
        Database.db.dao().insertAll(newGoal)

        (view.parent as LinearLayout).visibility = View.INVISIBLE
        (view.parent.parent as CardView).findViewById<LinearLayout>(R.id.goal).visibility = View.VISIBLE
        layoutInflater.inflate(R.layout.goalcard,findViewById<LinearLayout>(R.id.layout))

        (view.parent.parent as CardView).findViewById<TextView>(R.id.goalProgress).text = getGoalProgress(newGoal)
        view
        // tagi
//        var i = 0
//        for(view in findViewById<LinearLayout>(R.id.layout).children)
//            view.tag = i++
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

        Log.d("tag",intervalsList[0].size.toString())
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goals)

//        // inicjalizacja bazy danych
//        Database.db = Room.databaseBuilder(
//            baseContext,
//            AppDatabase::class.java, "database-name"
//        ).allowMainThreadQueries().build()

        FakeDatabaseGenerator()
        var i = 0
        for(el in Database.db.dao().getAllGoals()){
            val entry = layoutInflater.inflate(R.layout.goalcard,findViewById<LinearLayout>(R.id.layout))
            val par = findViewById<LinearLayout>(R.id.layout).getChildAt(i++)
            Log.d("vis:",par.findViewById<TextView>(R.id.plus).visibility.toString())
            par.findViewById<TextView>(R.id.plus).visibility = View.INVISIBLE
            par.findViewById<LinearLayout>(R.id.goal).visibility = View.VISIBLE
            par.findViewById<TextView>(R.id.goalName).text = "Less than ${el.upperLimit} ${el.statTracked} a ${el.timeInterval} in ${el.appTypeOrName}"
            par.findViewById<TextView>(R.id.goalProgress).text = getGoalProgress(el)
//            goalToViewId.add(goalView(el,entry))
            goalList.add(el)

        }
        layoutInflater.inflate(R.layout.goalcard,findViewById<LinearLayout>(R.id.layout))

//        var i = 0
//        for(view in findViewById<LinearLayout>(R.id.layout).children)
//            view.tag = i++
    }

    private fun FakeDatabaseGenerator(){
//        Database.db.dao().nukeGoals()
        Database.db.dao().nukeTable()
        val list = ArrayList<Stats>()
        val dayInMilis : Long = 1000*60*60*24
        val hourInMilis : Long = 1000*60*60
        val today = System.currentTimeMillis() - System.currentTimeMillis()%dayInMilis

        list.add(Stats("Youtube","Entertainment",today-6*dayInMilis,hourInMilis*2,2,3,dayInMilis))
        list.add(Stats("Youtube","Entertainment",today-5*dayInMilis,hourInMilis*3,3,1,dayInMilis))
        list.add(Stats("Youtube","Entertainment",today-4*dayInMilis,hourInMilis*4,1,5,dayInMilis))
        list.add(Stats("Youtube","Entertainment",today-3*dayInMilis,hourInMilis*3,5,1,dayInMilis))
        list.add(Stats("Youtube","Entertainment",today-2*dayInMilis,hourInMilis*2,3,1,dayInMilis))
        list.add(Stats("Youtube","Entertainment",today-1*dayInMilis,hourInMilis*5,0,1,dayInMilis))
        list.add(Stats("Youtube","Entertainment",today-0*dayInMilis,hourInMilis*1,0,0,dayInMilis))

        list.add(Stats("Instagram","Entertainment",today-6*dayInMilis,hourInMilis*5,1,3,dayInMilis))
        list.add(Stats("Instagram","Entertainment",today-5*dayInMilis,hourInMilis*6,3,3,dayInMilis))
        list.add(Stats("Instagram","Entertainment",today-4*dayInMilis,hourInMilis*3,1,0,dayInMilis))
        list.add(Stats("Instagram","Entertainment",today-3*dayInMilis,hourInMilis*1,1,3,dayInMilis))
        list.add(Stats("Instagram","Entertainment",today-2*dayInMilis,hourInMilis*3,1,5,dayInMilis))
        list.add(Stats("Instagram","Entertainment",today-1*dayInMilis,hourInMilis*3,0,3,dayInMilis))
        list.add(Stats("Instagram","Entertainment",today-0*dayInMilis,hourInMilis*4,0,0,dayInMilis))

        list.add(Stats("WorkApp","Work",today-6*dayInMilis,hourInMilis*5,0,0,dayInMilis))
        list.add(Stats("WorkApp","Work",today-5*dayInMilis,hourInMilis*6,0,0,dayInMilis))
        list.add(Stats("WorkApp","Work",today-4*dayInMilis,hourInMilis*3,0,0,dayInMilis))
        list.add(Stats("WorkApp","Work",today-3*dayInMilis,hourInMilis*1,0,0,dayInMilis))
        list.add(Stats("WorkApp","Work",today-2*dayInMilis,hourInMilis*3,0,0,dayInMilis))
        list.add(Stats("WorkApp","Work",today-1*dayInMilis,hourInMilis*3,0,0,dayInMilis))
        list.add(Stats("WorkApp","Work",today-0*dayInMilis,hourInMilis*4,0,0,dayInMilis))

        list.add(Stats("Settings","System",today-6*dayInMilis,hourInMilis*1,0,0,dayInMilis))
        list.add(Stats("Settings","System",today-5*dayInMilis,hourInMilis*2,0,0,dayInMilis))
        list.add(Stats("Settings","System",today-4*dayInMilis,hourInMilis*2,0,0,dayInMilis))
        list.add(Stats("Settings","System",today-3*dayInMilis,hourInMilis*2,0,0,dayInMilis))
        list.add(Stats("Settings","System",today-2*dayInMilis,hourInMilis*2,0,0,dayInMilis))
        list.add(Stats("Settings","System",today-1*dayInMilis,hourInMilis*1,0,0,dayInMilis))
        list.add(Stats("Settings","System",today-0*dayInMilis,hourInMilis*1,0,0,dayInMilis))

        for(el in list)
            Database.db.dao().insertAll(el)
    }



}