package com.sap.codelab.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a memo.
 */
@Entity(tableName = "memo")
internal data class Memo(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        var id: Long,
        @ColumnInfo(name = "title")
        var title: String,
        @ColumnInfo(name = "description")
        var description: String,
        @ColumnInfo(name = "reminderDate")
        var reminderDate: Long,
        @ColumnInfo(name = "reminderLatitude")
        var reminderLatitude: Double,
        @ColumnInfo(name = "reminderLongitude")
        var reminderLongitude: Double,
        @ColumnInfo(name = "isDone")
        var isDone: Boolean = false
)
