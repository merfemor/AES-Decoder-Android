package com.merfemor.aesencryptor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.merfemor.aesencryptor.crypto.EncodeAlgorithm
import com.merfemor.aesencryptor.crypto.OpenSSLAesController
import com.merfemor.aesencryptor.util.KLog

class MainActivity : AppCompatActivity() {
    private lateinit var inputEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var resultEditText: EditText
    private lateinit var actionButton: Button
    private lateinit var copyToClipboardButton: Button

    private lateinit var aesController: OpenSSLAesController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aesController = OpenSSLAesController()

        setContentView(R.layout.main_activity)

        actionButton = findViewById(R.id.action_button)
        inputEditText = findViewById(R.id.input_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        resultEditText = findViewById(R.id.result_edit_text)
        copyToClipboardButton = findViewById(R.id.copy_to_clipboard_button)

        actionButton.setOnClickListener { onActionButtonClicked() }
        copyToClipboardButton.setOnClickListener { onCopyToClipboardButtonClicked() }
    }

    private fun onActionButtonClicked() {
        val password = passwordEditText.text.toString()
        val textToDecode = inputEditText.text.toString()
        val algorithm = EncodeAlgorithm.AES_256_CBC
        val decodedText = aesController.decode(textToDecode, password, algorithm)
        if (decodedText.isRight()) {
            val cause = decodedText.getRightSure().message
            KLog.d(TAG) { "Failed to decode text, cause: $cause" }
            Toast.makeText(this, "Failed to decode", Toast.LENGTH_SHORT).show()
            return
        }
        resultEditText.setText(decodedText.getLeftSure())
    }

    private fun onCopyToClipboardButtonClicked() {
        val text = resultEditText.text
        val clipData = ClipData.newPlainText(CLIP_DATA_LABEL, text)
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Copied successfully", Toast.LENGTH_SHORT).show()
    }

    private companion object {
        private const val TAG = "MainActivity"
        private const val CLIP_DATA_LABEL = "Decode result"
    }
}