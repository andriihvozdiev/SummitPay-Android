package com.glance.streamline.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class LoginRecordInfo(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val pinHash: String = "",
    val salt: String = "",
    val role: String = "",
    val assignedTo: String = "",
    val expiryDate: String = ""
)

