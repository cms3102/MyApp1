package com.csergio.myapp1.model

class ChatRoom {

    var chatroom_id = ""
    var member_count = 0
    val memberList = mutableListOf<User>()
    val messageList = mutableListOf<Message>()

    inner class Message{
    }

}