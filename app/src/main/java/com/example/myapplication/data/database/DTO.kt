package com.example.myapplication.data.database

data class UserWithInfo(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val age: Int,
    val habitation: String?,
    val phoneNumber: String?,
    val createdAt: Long,

    val activeTitle: Int,
    val exp: Int,
    val profileFoto: String?
)