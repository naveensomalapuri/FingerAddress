package com.fingeraddress.app.model

data class UserProfile(
    val name: String = "",
    val address: String = "",
    val fatherMobile: String = "",
    val deviceId: String = "",
    val enrolledAt: Long = 0L
)
