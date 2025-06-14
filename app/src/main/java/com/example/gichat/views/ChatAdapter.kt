package com.example.gichat.views


import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gichat.R
import com.example.gichat.db.ChatModel

class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    fun bind(chatModel: ChatModel){
        val tvChatUser: TextView = itemView.findViewById(R.id.tvChatUser)
        val tvChatAI: TextView = itemView.findViewById(R.id.tvChatAI)
        val llChatUser: LinearLayout = itemView.findViewById(R.id.llChatUser)
        val llChatAI: LinearLayout = itemView.findViewById(R.id.llChatAI)

        if (chatModel.isUser) {
            llChatUser.visibility = View.VISIBLE
            llChatAI.visibility = View.GONE
            tvChatUser.text = chatModel.message
        } else {
            llChatAI.visibility = View.VISIBLE
            llChatUser.visibility = View.GONE
            tvChatAI.text = chatModel.message
        }
    }
}

class ChatAdapter: RecyclerView.Adapter<ChatViewHolder>() {

    private var chats: List<ChatModel> = emptyList()
    fun updateChatList(chats: List<ChatModel>){
        this.chats = chats
        notifyDataSetChanged()  // re-renders if any change made in the notes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount(): Int {
        return chats.size       //size of current note list
    }

}