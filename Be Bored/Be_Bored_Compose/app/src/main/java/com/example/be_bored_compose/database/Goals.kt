package com.example.be_bored_compose.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Goals(
    @ColumnInfo(name = "statTracked") val statTracked: String?,
    @ColumnInfo(name = "upperLimit") val upperLimit: Long?,
    @ColumnInfo(name = "timeInterval") val timeInterval: String?, // dzień, dni w tygodniu, miesiąc
    @ColumnInfo(name = "appTypeOrName") val appTypeOrName: String?
){
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    override fun toString(): String {
        return ""
    }
}
