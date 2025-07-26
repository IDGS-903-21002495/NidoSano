package com.modulap.nidosano.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val name: String = "",
    val email: String = "",
    //val password: String = "",
    val last_name: String = "",
    val phone_number: String = "",
    val status: Boolean = true,
    @ServerTimestamp
    val date: Date? = null
)
