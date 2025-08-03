package com.modulap.nidosano.data.model

data class FeedingSchedule(
    val id: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val duration: Int = 0,
    val frequency: String = "",
)
