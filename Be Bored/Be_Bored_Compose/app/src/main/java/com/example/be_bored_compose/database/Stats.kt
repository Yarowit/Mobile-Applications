package com.example.be_bored_compose.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stats(
    @ColumnInfo(name = "app") val app: String?,
    @ColumnInfo(name = "appType") val appType: String?,
    @ColumnInfo(name = "startTime") val startTime: Long?, // czas od 1970 w milisekundach
    @ColumnInfo(name = "useTime") val useTime: Long?,    // milisekundy
    @ColumnInfo(name = "notificationsSent") val notificationsSent: Int?,
    @ColumnInfo(name = "timesEntered") val timesEntered: Int?,
    @ColumnInfo(name = "probingInterval") val probingInterval: Long? // 15 min = 900 000 ms
){
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    override fun toString(): String {
        return "$app ($appType)(Probe: $probingInterval): $startTime : T - $useTime"
    }
}
