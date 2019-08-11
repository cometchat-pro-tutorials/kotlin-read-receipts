package com.vucko.cometchatdemo


object GeneralConstants{
    const val API_KEY = "40699368e14447dfe1f45d5168fae2850473ce7e"
    const val APP_ID = "18299f79cc590e"
    const val AVATARS_URL = "https://ui-avatars.com/api/?name="
    const val MY_MESSAGE: Int = 0
    const val OTHERS_MESSAGE: Int = 1
}

enum class MessageInfo{
    DELIVERED,
    READ
}