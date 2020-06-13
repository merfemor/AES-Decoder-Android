package com.merfemor.aesencryptor

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.merfemor.aesencryptor.crypto.EncodeAlgorithm
import com.merfemor.aesencryptor.crypto.OpenSSLAesController
import com.merfemor.aesencryptor.util.KAssert
import com.merfemor.aesencryptor.util.KLog

class MainActivity : AppCompatActivity() {
    private lateinit var inputEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var resultEditText: EditText
    private lateinit var actionButton: Button
    private lateinit var copyToClipboardButton: Button
    private lateinit var algorithmChooseSpinner: AdapterView<ArrayAdapter<EncodeAlgorithm>>
    private lateinit var chooseFileButton: Button

    private lateinit var aesController: OpenSSLAesController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aesController = OpenSSLAesController()

        setContentView(R.layout.main_activity)

        algorithmChooseSpinner = findViewById(R.id.algorithm_choose_spinner)
        actionButton = findViewById(R.id.action_button)
        inputEditText = findViewById(R.id.input_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        resultEditText = findViewById(R.id.result_edit_text)
        copyToClipboardButton = findViewById(R.id.copy_to_clipboard_button)
        chooseFileButton = findViewById(R.id.choose_file_button)

        val encodedAlgorithmChooseSpinnerAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, EncodeAlgorithm.values()
        )
        algorithmChooseSpinner.adapter = encodedAlgorithmChooseSpinnerAdapter
        algorithmChooseSpinner.setSelection(
            encodedAlgorithmChooseSpinnerAdapter.getPosition(EncodeAlgorithm.AES_256_CBC)
        )

        actionButton.setOnClickListener { onActionButtonClicked() }
        copyToClipboardButton.setOnClickListener { onCopyToClipboardButtonClicked() }
        chooseFileButton.setOnClickListener { onChooseFileButtonClicked() }
    }

    private fun onActionButtonClicked() {
        resultEditText.setText("")
        val password = passwordEditText.text.toString()
        val textToDecode = inputEditText.text.toString()
        val algorithm = algorithmChooseSpinner.selectedItem as EncodeAlgorithm
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

    private fun onChooseFileButtonClicked() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("text/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
        val chooserIntent = Intent.createChooser(intent, "Select a file")
        startActivityForResult(chooserIntent, REQUEST_CODE_FILE_CHOOSE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FILE_CHOOSE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                KAssert.fail { "Expected non-null data in response to file choose" }
                return
            }
            val fileUri = data.data
            if (fileUri == null) {
                KAssert.fail { "Expected non-null file uri" }
                return
            }
            KLog.d(TAG) { "Choose file $fileUri" }
            val array = readUriContent(fileUri) ?: return
            val contentString = String(array)
            inputEditText.setText(contentString)
        }
    }

    private fun readUriContent(uri: Uri): ByteArray? {
        val stream = contentResolver.openInputStream(uri)
        if (stream == null) {
            KAssert.fail { "Expected non-null stream" }
            return null
        }
        return stream.buffered().readBytes()
    }

    private companion object {
        private const val TAG = "MainActivity"
        private const val CLIP_DATA_LABEL = "Decode result"
        private const val REQUEST_CODE_FILE_CHOOSE = 0
    }
}