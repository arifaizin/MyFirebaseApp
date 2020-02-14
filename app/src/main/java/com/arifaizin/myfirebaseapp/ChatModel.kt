package com.arifaizin.myfirebaseapp

data class ChatModel(
    var text: String? = "",
    var name: String? = "",
    var photoUrl: String? = "",
    var timestamp: Long? = 0
)
