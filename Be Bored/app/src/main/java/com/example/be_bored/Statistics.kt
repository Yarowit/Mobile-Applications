package com.example.be_bored

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.be_bored.database.Stats
import com.example.be_bored.database.Database
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

class Statistics : AppCompatActivity() {
    data class MoreData(val notSent : Int, val timEnt : Int){}

    val appTypes = arrayOf<String>("Work","Entertainment","System")

    private val weekdays = listOf<String>("Mon","Tue","Wen","Thu","Fri","Sat","Sun")


    private fun LoadDataFromDatabase() : List<FloatArray>{
        val list = ArrayList<FloatArray>()
        val dayInMilis : Long = 1000*60*60*24
        val today = System.currentTimeMillis() - System.currentTimeMillis()%dayInMilis

        for(day in today-dayInMilis*6 .. today step dayInMilis){
            val miniList = ArrayList<Float>()
            for(type in appTypes){
                miniList.add(Database.db.dao().getDailyUseTimeByDateAndType(day,type).toFloat()/1000/60/60)
            }
            list.add(miniList.toFloatArray())
        }
        return list
    }

    private fun loadDataForCircular(dayI : Int, typeI : Int) : List<PieEntry>{
        val list = ArrayList<PieEntry>()
        val dayInMilis : Long = 1000*60*60*24
        val today = System.currentTimeMillis() - System.currentTimeMillis()%dayInMilis
        val type = appTypes[ typeI ]
        val day = today - (6-dayI)*dayInMilis
        val data = Database.db.dao().getMoreInfo(day,type)

        for(el in data){
            val time = el.useTime?.div(60000)?.toFloat()
            if(time != null && el.notificationsSent != null && el.timesEntered != null)
                list.add(PieEntry(time,"${el.app}",MoreData(el.notificationsSent,el.timesEntered)))
        }
        return list
    }

    private fun generatePieChart(){
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        pieChart.description.isEnabled = false
        pieChart.centerText = "minutes spent\nusing app"
        pieChart.animateY(300, Easing.EaseInOutQuad);

        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {

            override fun onNothingSelected() {pieChart.centerText = "minutes spent\nusing app"}

            override fun onValueSelected(e: Entry, h: Highlight) {
                val md = (e.data as MoreData)
                pieChart.centerText = "launches: ${md.timEnt}\nnotifications: ${md.notSent}"
            }
        })

    }
    private fun generateBarChart(){
        val barChart = findViewById<BarChart>(R.id.barChart)
//        val dataList = LoadData()
        val dataList = LoadDataFromDatabase()

        //entries
        val dataSetList = ArrayList<BarDataSet>()
        for(i in 0 until 7) {
            val entries = List(1) { BarEntry(i.toFloat(), dataList[i]) }
            val set = BarDataSet(entries, "$i").apply {
                colors = ColorTemplate.MATERIAL_COLORS.asList()
                valueTextColor = Color.TRANSPARENT
            }

            dataSetList.add(set)
        }

        val data = BarData(dataSetList as List<IBarDataSet>?)
//        barChart.legend.entries =  isEnabled = false


//        Log.d("a",barChart.legend.entries.contentToString())
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(weekdays)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
//            setCenterAxisLabels(true)
        }

        barChart.setFitBars(true)
        barChart.data = data
        barChart.description.isEnabled = false
        barChart.animateY(700)


        val leg = Array<LegendEntry>(appTypes.size){i-> barChart.legend.entries[i].apply { label = appTypes[i] }}
        barChart.legend.setCustom(leg)

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {

                override fun onNothingSelected() {
//                    Toast.makeText(baseContext, "Nothing Selected", Toast.LENGTH_SHORT).show()
                    findViewById<PieChart>(R.id.pieChart).visibility = View.INVISIBLE
                }

                override fun onValueSelected(e: Entry, h: Highlight) {
                    val pieChart = findViewById<PieChart>(R.id.pieChart)
                    pieChart.animateY(300, Easing.EaseInOutQuad);
                    pieChart.centerText = "minutes spent\nusing app"
                    pieChart.visibility = View.VISIBLE
                    val weekDay = e.x
                    val type = h.stackIndex
                    val entries = loadDataForCircular(weekDay.toInt(),type)


                    val pieDataSet = PieDataSet(entries,"").apply{
                        colors = ColorTemplate.COLORFUL_COLORS.asList()
//                        valueTextColor = Color.TRANSPARENT
                        valueTextSize = 18F
                    }

                    val data = PieData(pieDataSet)

                    pieChart.data = data

                    pieChart.invalidate()

//                    val yearWeek = (e as BarEntry).yVals[h.stackIndex]
//                    Toast.makeText(baseContext, "time: $yearWeek", Toast.LENGTH_SHORT).show()
                }
            }
        )

    }

    private fun FakeDatabaseGenerator(){
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics)

        FakeDatabaseGenerator()

        generatePieChart()
        generateBarChart()
    }

}