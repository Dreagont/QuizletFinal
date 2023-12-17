package com.example.quizletfinal

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quizletfinal.models.Card
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class CsvReaderActivity : AppCompatActivity() {

    private lateinit var fileName: TextView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var cancelButton: Button

    private val READ_REQUEST_CODE = 123
    private val cardList = ArrayList<Card>()
    private var username: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csv_reader)
        fileName = findViewById(R.id.fileName)
        cancelButton = findViewById(R.id.button_cancel)

        username = intent.getStringExtra("username")

        findViewById<Button>(R.id.button_loadCsv).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/*"
            startActivityForResult(intent, READ_REQUEST_CODE)
        }

        findViewById<Button>(R.id.button_cancel).setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                val selectedFileUri: Uri = resultData.data!!
                try {
                        for (cardData in readCSV(selectedFileUri)) {
                            val english = cardData.split(",")[0]
                            val vietnamese = cardData.split(",")[1]

                            val card = Card(english, vietnamese)
                            cardList.add(card)
                        }

                        findViewById<Button>(R.id.button_saveCsv).setOnClickListener {
                            addTopics(cardList)
                            Toast.makeText(this, "Add cards successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    val selectedFileName = getFileName(selectedFileUri)
                    fileName.text = selectedFileName
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    private fun addTopics(cardList: List<Card>) {
        val intent = Intent(this, AddTopicActivity::class.java)
        intent.putParcelableArrayListExtra("csvReadCardList",ArrayList(cardList))
        intent.putExtra("username",username)
        startActivity(intent)
        finish()

    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result
    }

    private fun readCSV(uri: Uri): List<String> {
        val csvFile = contentResolver.openInputStream(uri)
        val isr = InputStreamReader(csvFile)
        return readLines(BufferedReader(isr))
    }

    private fun readLines(reader: BufferedReader): List<String> {
        return try {
            IOUtils.readLines(reader)
        } finally {
            reader.close()
        }
    }
}
