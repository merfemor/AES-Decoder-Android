package com.merfemor.aesencryptor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var inputEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var resultEditText: EditText
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        actionButton = findViewById(R.id.action_button)
        inputEditText = findViewById(R.id.input_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        resultEditText = findViewById(R.id.result_edit_text)

        actionButton.setOnClickListener { onActionButtonClicked() }
    }

    private fun onActionButtonClicked() {
        val password = passwordEditText.text.toString()
        val textToDecode = inputEditText.text.toString()
        val encodedText = decodeText(textToDecode, password)
        resultEditText.setText(encodedText)
    }

    private fun decodeText(textToEncode: String, password: String): String {
        return textToEncode
    }
}