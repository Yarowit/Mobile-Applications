package com.example.be_bored_compose

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import com.example.be_bored_compose.database.Database
import com.example.be_bored_compose.database.Stats
import com.example.be_bored_compose.ui.theme.Be_Bored_ComposeTheme
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Calendar

class Statistics : ComponentActivity() {
    class PieViewModel : ViewModel(){
        var dayI by mutableStateOf(-1)
        var typeI by mutableStateOf(-1)
        private set
        fun updatePie(d : Int, t : Int){
            dayI = d
            typeI = t
        }
    }
    var first = true

    data class MoreData(val notSent : Int, val timEnt : Int)

    private val appTypes = TrackedApps.apps.keys.toList()
//        arrayOf<String>("Work","Entertainment","System")

    private val baseWeekdays = listOf<String>("Mon","Tue","Wen","Thu","Fri","Sat","Sun")


    private fun LoadDataFromDatabase() : List<FloatArray>{
        val list = ArrayList<FloatArray>()
        val dayInMilis : Long = 1000*60*60*24
        val today = System.currentTimeMillis()
        val space = 90000L
//        for(day in )
        for(day in today-dayInMilis*7 .. today-dayInMilis step dayInMilis){
            val miniList = ArrayList<Float>()
            for(type in appTypes){
                miniList.add(Database.db.dao().getDailyUseTimeByDateAndType(day,type).toFloat()/1000/60/60)
                Log.d("ttt",type)
                Log.d("ttt",Database.db.dao().getDailyUseTimeByDateAndType(day,type).toString())
            }
            list.add(miniList.toFloatArray())
        }
        return list
    }

    private fun genPieChartData(dayI : Int, typeI : Int) : PieData {
        val entries = loadDataForCircular(dayI,typeI)
        val pieDataSet = PieDataSet(entries,"").apply{
            colors = ColorTemplate.COLORFUL_COLORS.asList()
            valueTextSize = 18F
        }

        return PieData(pieDataSet)
    }

    private fun loadDataForCircular(dayI : Int, typeI : Int) : List<PieEntry>{
        val list = ArrayList<PieEntry>()
        val dayInMilis : Long = 1000*60*60*24
        val today = System.currentTimeMillis()
        val type = appTypes[ typeI ]
        val day = today - (7-dayI)*dayInMilis
        val data = Database.db.dao().getMoreInfo(day,type)

        for(el in data){
            val time = el.useTime?.div(60000)?.toFloat()
            if(time != null && el.notificationsSent != null && el.timesEntered != null)
                if(time > 0)
                    list.add(PieEntry(time,"${el.app}",MoreData(el.notificationsSent,el.timesEntered)))
        }
        return list
    }


    private fun genBarChartData(): BarData {
        val dataList = LoadDataFromDatabase()

        //entries
        val dataSetList = ArrayList<BarDataSet>()
        for (i in 0 until 7) {
            val entries = List(1) { BarEntry(i.toFloat(), dataList[i]) }
            val set = BarDataSet(entries, "$i").apply {
                colors = ColorTemplate.MATERIAL_COLORS.asList()
                valueTextColor = Color.TRANSPARENT
            }

            dataSetList.add(set)
        }

        return BarData(dataSetList as List<IBarDataSet>?)
    }

    private fun FakeDatabaseGenerator(){
//        Database.db.dao().nukeTable()
//        Database.db.dao().nukeGoals()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        FakeDatabaseGenerator()
        val viewModel = PieViewModel()
        setContent {
            Be_Bored_ComposeTheme(){
                val context = LocalContext.current
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GenBarGraph(context = context, updatePie = viewModel::updatePie )
                    GenPieGraph(context = context, dayI = viewModel.dayI, typeI = viewModel.typeI)

                }
            }
        }
    }

    @Composable
    fun GenPieGraph(context: Context?, dayI: Int, typeI: Int){
        AndroidView(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            factory = {context ->
                PieChart(context)
            },
            update = { pieChart ->
                pieChart.animateY(300, Easing.EaseInOutQuad);

                pieChart.description.isEnabled = false
                pieChart.highlightValues(null)
                Log.d("ffff","$dayI $typeI")
                if(dayI < 0 || typeI < 0) {
                    pieChart.visibility = View.INVISIBLE
                    pieChart.centerText = ""
                }else{
                    pieChart.centerText = "minutes spent\nusing app"
                    pieChart.visibility = View.VISIBLE
                    pieChart.data = genPieChartData(dayI, typeI)
                }

                pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {

                    override fun onNothingSelected() {pieChart.centerText = "minutes spent\nusing app"}

                    override fun onValueSelected(e: Entry, h: Highlight) {
                        val md = (e.data as MoreData)
                        pieChart.centerText = "launches: ${md.timEnt}\nnotifications: ${md.notSent}"
                    }
                })

                pieChart.invalidate()
            }
        )
    }


    @Composable
    fun GenBarGraph(context: Context?, updatePie: (Int,Int) -> Unit){
        AndroidView(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
            factory = {
                BarChart(context)
            },
            update = { barChart ->
                val data = genBarChartData()
                val weekdays = ArrayList<String>()
                val calendar = Calendar.getInstance()
//                Log.d("day",calendar.get(Calendar.DAY_OF_WEEK).toString())
                for(i in 1 .. 7){
                    weekdays.add(baseWeekdays[ (calendar.get(Calendar.DAY_OF_WEEK) - 1 + i)%7])
                }

                barChart.data = data
//                barChart.invalidate()
                barChart.xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(weekdays)
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                }

                barChart.setFitBars(true)
                barChart.description.isEnabled = false
                if(first){
                    barChart.animateY(700)
                    first = false
                }

                val leg = Array<LegendEntry>(appTypes.size){i-> barChart.legend.entries[i].apply { label = appTypes[i] }}
                barChart.legend.setCustom(leg)

                barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        updatePie(-1,-1)
                    }

                    override fun onValueSelected(e: Entry, h: Highlight) {
                        val weekDay = e.x
                        val type = h.stackIndex
                        updatePie(weekDay.toInt(),type)
                    }
                })
                barChart.invalidate()
            }
        )
    }
}