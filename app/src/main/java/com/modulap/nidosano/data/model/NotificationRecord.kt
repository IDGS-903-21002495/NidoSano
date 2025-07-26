package com.modulap.nidosano.data.model

import com.google.firebase.firestore.ServerTimestamp
import com.google.type.DateTime
import java.util.Date

data class NotificationRecord(
    val title: String = "",
    val description: String = "",
    val type: String = "",
    val topic: String = "",
    val payload: String = "",
    val destinationRoute: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp
    val timestamp: Date? = null,
    val userId: String? = null,
    val chickenCoopId: String? = null
)