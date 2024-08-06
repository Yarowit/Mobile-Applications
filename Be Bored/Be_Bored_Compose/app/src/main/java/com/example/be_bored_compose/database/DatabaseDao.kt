package com.example.be_bored_compose.database

import androidx.room.*

@Dao
interface DatabaseDao {
    // =============================== STATS ===============================
    // w bazie przechowywane dzienne statystyki
    @Query("SELECT * FROM Stats")
    fun getAll(): List<Stats>

//    @Query("SELECT * FROM event WHERE uid IN (:eventIds)")
//    fun loadAllByIds(eventIds: IntArray): List<Event>


//    @Query("SELECT * FROM Stats WHERE startTime >= :Start - 86400000 AND startTime < :Start  AND probingInterval=86400000 AND (app = :name OR appType = :name)")
    @Query("SELECT * FROM Stats WHERE startTime >= :Start - 900000 AND startTime < :Start  AND probingInterval=86400000 AND (app = :name OR appType = :name)")
    fun getDataByDayAndNameOrType(Start: Long, name : String) : List<Stats>
//    fun getDataByWeekAndType() : List<Stats>
//    fun getDataByWeekAndName() : List<Stats>
//    fun getDataByAndType() : List<Stats>
//    fun getDataByWeekAndName() : List<Stats>



    // do wykresu
    @Query("SELECT SUM(useTime) FROM Stats WHERE startTime >= :Start - 900000 AND startTime < :Start  AND probingInterval=86400000 AND appType = :type")
    fun getDailyUseTimeByDateAndType(Start: Long, type : String): Long

    // do kołowego
    @Query("SELECT * FROM Stats WHERE startTime >= :Start - 900000 AND startTime < :Start  AND probingInterval=86400000 AND appType = :type")
    fun getMoreInfo(Start: Long, type : String): List<Stats>

    // Do Celów
//    @Query("SELECT SUM(useTime) FROM Stats WHERE startTime >= :Start AND startTime < :Start + 604800000 AND probingInterval<=604800000 AND appType = :type")
//    fun getWeeklyUseTimeByDateAndType(Start: Long, type : String): Long

//    @Query("SELECT SUM(timesEntered) FROM Stats WHERE startTime >= :Start AND startTime < :Start + 86400000 AND probingInterval=86400000 AND appType = :type")
//    fun getDailyEntriesByDateAndType(Start: Long, type : String): Long

//    @Query("SELECT SUM(useTime) FROM Stats WHERE startTime >= :Start AND startTime < :Start + 604800000 AND probingInterval<=604800000 AND appType = :type")
//    fun getWeeklyUseTimeByDateAndName(Start: Long, type : String): Long

//    @Query("SELECT SUM(useTime) FROM Stats WHERE startTime >= :Start AND startTime+useTime <= :End AND app = :name")
//    fun getUseTimeByDateAndName(Start: Double, End: Double, name : String): Double

//    @Query("SELECT * FROM event WHERE startTime > :Beg AND startTime < End")
//    fun findByDate(Beg : Double, End : Double): List<Stats>
//
//    @Query("SELECT * FROM event WHERE appType = :cat")
//    fun loadByType(cat : String): List<Stats>
//
//    @Query("SELECT * FROM event WHERE app = :app")
//    fun loadByApp(app : String): List<Stats>
//
//    //TODO
//    @Query("SELECT  FROM event WHERE app = :app")
//    fun findMaxUsed(): List<Stats>

    @Insert
    fun insertAll(vararg events: Stats)

    @Delete
    fun delete(event: Stats)

    @Query("DELETE FROM Stats")
    fun nukeTable()

    // =============================== GOALS ===============================
    @Query("SELECT * FROM GOALS WHERE statTracked=:statTracked AND upperLimit=:upperLimit AND timeInterval=:timeInterval AND appTypeOrName=:appTypeOrName LIMIT 1")
    fun getAGoal(statTracked : String, upperLimit: Long, timeInterval: String, appTypeOrName : String) : Goals

    @Query("SELECT * FROM Goals")
    fun getAllGoals(): List<Goals>

    @Insert
    fun insertAll(vararg events: Goals)

    @Delete
    fun delete(event: Goals)

    @Query("DELETE FROM Goals")
    fun nukeGoals()

    // ============================= OGÓLNE ================================

    @Query("SELECT DISTINCT app from Stats")
    fun getAppNames() : List<String>

    @Query("SELECT DISTINCT appType from Stats")
    fun getAppTypes() : List<String>
}