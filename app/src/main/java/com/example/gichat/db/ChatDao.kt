package com.example.gichat.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class ChatDao {

    @Insert
    abstract suspend fun create(chatModel: ChatModel)

    @Insert
    abstract suspend fun createAndReturnId(chatModel: ChatModel): Long

    @Query("UPDATE chats SET message = :newMessage WHERE id = :id")
    abstract suspend  fun updateCurrentChat(id: Long, newMessage: String)

    @Query("SELECT * FROM chats WHERE chatNo = :chatNo ORDER BY id")
    abstract suspend fun getChatsByChatNo(chatNo: Long): List<ChatModel>

    @Query("DELETE FROM chats WHERE id = :id")
    abstract suspend fun deleteChatById(id: Long)

    @Query("SELECT * FROM chats ORDER BY id")
    abstract fun readAllChats(): LiveData<List<ChatModel>>
}