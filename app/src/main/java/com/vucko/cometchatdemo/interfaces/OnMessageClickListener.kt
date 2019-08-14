package com.vucko.cometchatdemo.interfaces

import com.cometchat.pro.models.TextMessage

interface OnMessageClickListener {
    fun onMessageStatusLongClick(item: TextMessage?)
}