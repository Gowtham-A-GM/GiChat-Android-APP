package com.example.gichat.views

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gichat.R
import com.example.gichat.databinding.ActivityMainBinding
import com.example.gichat.db.ChatModel
import com.example.gichat.viewModels.ChatViewModel
import java.util.Locale
import java.util.jar.Manifest

private var currentChatNo: Long = System.currentTimeMillis()

class MainActivity : AppCompatActivity(){
    lateinit var binding: ActivityMainBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    // for Speech-To-Text
    val permissionsArray = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    lateinit var speechRecognizer: SpeechRecognizer
    lateinit var speechRecognizerIntent: Intent
    // for Text-To-Speech
    private var isListening = false
    private var isMuted = false
    // for toggling APP theme
    private var appTheme = "light"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adapter for Recycler
        chatAdapter = ChatAdapter()
        binding.rvChatMsgList.adapter = chatAdapter
        binding.rvChatMsgList.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        // Re-Render if any changes in List of Data
        chatViewModel.allChats.observe(this) { chats ->
            val currentChatList = chats.filter { it.chatNo == currentChatNo }

            Log.d("MainActivity", "Current Chat Messages: $currentChatList")
            chatAdapter.updateChatList(currentChatList)

            // Prevent crash if list is empty
            if (currentChatList.isNotEmpty()) {
                binding.rvChatMsgList.smoothScrollToPosition(currentChatList.size - 1)
            }
            binding.tvWelcomeTxt.visibility =
                if (currentChatList.isEmpty()) View.VISIBLE else View.GONE
        }

        // Speech to Text
        if(checkSelfPermission(permissionsArray[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissionsArray, 200)
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        binding.ivMicInput.setOnClickListener {
            if (!isListening) {
                // Start Listening
                binding.etMsgInputBox.setHint("Listening...")
                speechRecognizer.startListening(speechRecognizerIntent)
                binding.ivMicInput.setImageResource(R.drawable.icon_mic_off_light)
                isListening = true
            } else {
                // Stop Listening
                binding.etMsgInputBox.setHint("Enter your message")
                speechRecognizer.stopListening()
                binding.ivMicInput.setImageResource(R.drawable.icon_mic_light)
                isListening = false
            }
        }
        speechRecognizer.setRecognitionListener(object : RecognitionListener{
            override fun onReadyForSpeech(params: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(rmsdB: Float) {

            }

            override fun onBufferReceived(buffer: ByteArray?) {

            }

            override fun onEndOfSpeech() {
                isListening = false
                binding.ivMicInput.setImageResource(R.drawable.icon_mic_light)
                binding.etMsgInputBox.setHint("Enter your message")
            }

            override fun onError(error: Int) {
                isListening = false
                binding.ivMicInput.setImageResource(R.drawable.icon_mic_light)
                binding.etMsgInputBox.setHint("Enter your message")
            }

            override fun onResults(results: Bundle?) {
                val speechToTextResult = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if(!speechToTextResult.isNullOrEmpty()){
                    val existingText = binding.etMsgInputBox.text.toString()
                    val newText = speechToTextResult[0]
                    binding.etMsgInputBox.setText("$existingText $newText".trim())
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {

            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }

        })

        //Text to Speech Control
        binding.ivTextToSpeech.setOnClickListener {
            isMuted = !isMuted
            chatViewModel.setMuted(isMuted)

            if (isMuted) {
                binding.ivTextToSpeech.setImageResource(R.drawable.icon_text_to_speech_off_light)
                Toast.makeText(this, "Voice Muted", Toast.LENGTH_SHORT).show()
            } else {
                binding.ivTextToSpeech.setImageResource(R.drawable.icon_text_to_speech_light)
                Toast.makeText(this, "Voice Unmuted", Toast.LENGTH_SHORT).show()
            }
        }

        // Send Button Click Event
        binding.imgBtnMsgSend.setOnClickListener {
            val question = binding.etMsgInputBox.text.toString().trim()
            if (question.isNotEmpty()) {
                val chatModel = ChatModel(message = question, isUser = true, chatNo = currentChatNo)
                chatViewModel.create(chatModel)
//                binding.tvWelcomeTxt.visibility = View.GONE
                binding.etMsgInputBox.text.clear()
                chatViewModel.sendMessageToGeminiAI(question, currentChatNo)
            }
        }

       // Toggle the APP Theme (Light <-> Dark)
        binding.ivThemeToggle.setOnClickListener {
            appTheme = if (appTheme == "light") "dark" else "light"

            // changing the components color into dark theme
            if (appTheme == "dark") {
                // changing BG's and Text color
                binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_dark))
                binding.llTopBar.setBackgroundColor(ContextCompat.getColor(this, R.color.top_bar_dark))
                binding.tvAppName.setTextColor(ContextCompat.getColor(this, R.color.app_name_dark))
                binding.viewDividerLine.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_dark))
                binding.tvWelcomeTxt.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
//                binding.llMsgInputBox.setBackgroundColor(ContextCompat.getColor(this, R.color.input_bg_dark))
                binding.etMsgInputBox.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
                binding.etMsgInputBox.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint_dark))

                // if you need to change the icon into different icon
//                binding.ivThemeToggle.setImageResource(R.drawable.icon_theme_dark)

                // changing the drawable
                val drawable_llShapeChatTitle = ContextCompat.getDrawable(this, R.drawable.shape_chat_title_light)?.mutate()
                drawable_llShapeChatTitle?.setTint(ContextCompat.getColor(this, R.color.icon_dark))
                binding.llShapeChatTitle.background = drawable_llShapeChatTitle

                val drawable_llMsgInputBox = ContextCompat.getDrawable(this, R.drawable.shape_chat_input_light)?.mutate()
                drawable_llMsgInputBox?.setTint(ContextCompat.getColor(this, R.color.input_bg_dark))
                binding.llMsgInputBox.background = drawable_llMsgInputBox

                val drawable_imgBtnMsgSend = ContextCompat.getDrawable(this, R.drawable.shape_send_light)?.mutate()
                drawable_imgBtnMsgSend?.setTint(ContextCompat.getColor(this, R.color.icon_dark))
                binding.imgBtnMsgSend.background = drawable_imgBtnMsgSend

                // changing the color of vector assert
                binding.imgBtnMsgSend.setColorFilter(
                    ContextCompat.getColor(this, R.color.icon_send_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                binding.ivTextToSpeech.setColorFilter(
                    ContextCompat.getColor(this, R.color.icon_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                binding.ivThemeToggle.setColorFilter(
                    ContextCompat.getColor(this, R.color.icon_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                binding.ivMicInput.setColorFilter(
                    ContextCompat.getColor(this, R.color.icon_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

            } else {
                // changing BG's and Text color
                binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_light))
                binding.llTopBar.setBackgroundColor(ContextCompat.getColor(this, R.color.top_bar_light))
                binding.tvAppName.setTextColor(ContextCompat.getColor(this, R.color.app_name_light))
                binding.viewDividerLine.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_light))
                binding.tvWelcomeTxt.setTextColor(ContextCompat.getColor(this, R.color.text_light))
//                binding.llMsgInputBox.setBackgroundColor(ContextCompat.getColor(this, R.color.input_bg_light))
                binding.etMsgInputBox.setTextColor(ContextCompat.getColor(this, R.color.text_light))
                binding.etMsgInputBox.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint_light))

                // if you need to change the icon into different icon
//                binding.ivThemeToggle.setImageResource(R.drawable.icon_theme_light)

                // changing the drawable
                val drawable_llShapeChatTitle = ContextCompat.getDrawable(this, R.drawable.shape_chat_title_light)?.mutate()
                drawable_llShapeChatTitle?.setTint(ContextCompat.getColor(this, R.color.icon_light))
                binding.llShapeChatTitle.background = drawable_llShapeChatTitle

                val drawable_llMsgInputBox = ContextCompat.getDrawable(this, R.drawable.shape_chat_input_light)?.mutate()
                drawable_llMsgInputBox?.setTint(ContextCompat.getColor(this, R.color.input_bg_light))
                binding.llMsgInputBox.background = drawable_llMsgInputBox

                val drawable_imgBtnMsgSend = ContextCompat.getDrawable(this, R.drawable.shape_send_light)?.mutate()
                drawable_imgBtnMsgSend?.setTint(ContextCompat.getColor(this, R.color.icon_light))
                binding.imgBtnMsgSend.background = drawable_imgBtnMsgSend

                // changing the color of vector assert
                binding.imgBtnMsgSend.setColorFilter(
                    ContextCompat.getColor(this, R.color.icon_send_light),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                binding.ivTextToSpeech.setColorFilter(
                    ContextCompat.getColor(this, R.color.icon_light),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                binding.ivThemeToggle.setColorFilter(
                    ContextCompat.getColor(this, R.color.icon_light),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                binding.ivMicInput.setColorFilter(
                    ContextCompat.getColor(this, R.color.icon_light),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }


    }
}