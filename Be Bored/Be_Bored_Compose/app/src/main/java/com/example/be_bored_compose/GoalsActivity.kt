package com.example.be_bored_compose

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.be_bored_compose.database.Database
import com.example.be_bored_compose.database.Goals
import com.example.be_bored_compose.database.Stats
import com.example.be_bored_compose.ui.theme.Be_Bored_ComposeTheme


class GoalsActivity : ComponentActivity() {
    object Model : ViewModel(){
        var goals by mutableStateOf(mutableStateListOf<Goals>())
        
        var adding by mutableStateOf(false)

        var amount by mutableStateOf(0L)
        var stat by mutableStateOf("hours")
        var interval by mutableStateOf("day")
        var app by mutableStateOf(TrackedApps.apps["Work"]?.get(0) ?: "Calendar")
    }
    // wzór
    // less than <X> <entrie/hours> a <day,week,...> in <appname/type>


//    var app = ""

    val stats = listOf("hours","entries","notifications")
    val timeIntervals = listOf("day","week","month","day for a week","day for a month","week for a month")


    private fun getGoalProgress(goal : Goals) : String {
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
//            Log.d("sum","$sum")
//            Log.d("sum","$sum ${goal.upperLimit!!}")
            if(sum < goal.upperLimit!!)
                wins += 1f

            winPercentage += wins/intervalsList.size
        }
        if(intervalsList.size == 0)
            winPercentage = 1f
//        Log.d("tag",intervalsList[0].size.toString())
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
        Model.goals.addAll(Database.db.dao().getAllGoals())
        setContent {
            Be_Bored_ComposeTheme() {
                val context = LocalContext.current
                
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    for(goal in Model.goals){
                        Goal(context,goal)
                    }
                    if(!Model.adding)
                        Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                            onClick = { Model.adding = true }) {
                            Text(text = "+")
                        }
                    else
                        MakeAGoal(context = context)
                }
            }
        }
    }
    
    @Composable
    fun Goal(context: Context?, newGoal: Goals){
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterVertically),textAlign = TextAlign.Center,text = "Less than ${newGoal.upperLimit} ${newGoal.statTracked} a ${newGoal.timeInterval} in ${newGoal.appTypeOrName}")
            Text(modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.CenterVertically) , textAlign = TextAlign.Center,text = "${getGoalProgress(newGoal)}")
            Button(onClick = {
                Model.goals.remove(newGoal)
                Database.db.dao().delete(Database.db.dao().getAGoal(newGoal.statTracked!!,newGoal.upperLimit!!,newGoal.timeInterval!!,newGoal.appTypeOrName!!))
            }) {
                Text(text = "-")
            }
        }
    }
    
    @Composable
    fun MakeAGoal(context:Context?){
        Column( modifier = Modifier.fillMaxWidth()){
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Less Than ", modifier = Modifier.fillMaxWidth(0.3f))
                EditTextField(Modifier.fillMaxWidth(0.5f))
                Button( onClick = {
                    Model.adding = false
                    val goal = Goals(Model.stat,Model.amount,Model.interval,Model.app)
                    Database.db.dao().insertAll(goal)
                    Model.goals.add(goal)
                }) {
                    Text(text = "+")
                }
            }
            Spinner(stats,stats[0],{stat -> Model.stat = stat},Modifier)
            Spinner(timeIntervals,timeIntervals[0],{timeInterval -> Model.interval = timeInterval},Modifier)
            Spinner(TrackedApps.appPackages.keys.toList().plus(TrackedApps.apps.keys.toList()) ,Model.app,{appname -> Model.app = appname},Modifier)
        }
    }


    @Composable
    fun Spinner(
        list: List<String>,
        preselected: String,
        onSelectionChanged: (myData: String) -> Unit,
        modifier: Modifier = Modifier
    ) {

        var selected by remember { mutableStateOf(preselected) }
        var expanded by remember { mutableStateOf(false) } // initial value

        Card(
            modifier = modifier.clickable {
                expanded = !expanded
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {

                Text(
                    text = selected,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Icon(Icons.Outlined.ArrowDropDown, null, modifier = Modifier.padding(8.dp))

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.wrapContentWidth()   // delete this modifier and use .wrapContentWidth() if you would like to wrap the dropdown menu around the content
                ) {
                    list.forEach { listEntry ->

                        DropdownMenuItem(
                            onClick = {
                                selected = listEntry
                                expanded = false
                                onSelectionChanged(selected)
                            },
                            content = {
                                Text(
                                    text = listEntry,
                                    modifier = Modifier
                                        .wrapContentWidth()  //optional instad of fillMaxWidth
                                )
                            },
                        )
                    }
                }

            }
        }
    }

    @Composable
    fun EditTextField(modifier: Modifier = Modifier) {
        val inputvalue = remember { mutableStateOf(TextFieldValue()) }
        TextField(modifier = modifier, value = inputvalue.value, onValueChange = { inputvalue.value = it ;  Model.amount = it.text.toLong() },maxLines = 2, singleLine = true)
    }
}
