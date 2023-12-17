package com.example.quizletfinal

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.quizletfinal.models.Card
import com.example.quizletfinal.models.extendedCardList
import java.util.Locale

class MultiChoiceActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var closeButton: Button
    private lateinit var btnAnswerA : CardView
    private lateinit var btnAnswerB : CardView
    private lateinit var btnAnswerC : CardView
    private lateinit var btnAnswerD : CardView
    private lateinit var txtAnswerA : TextView
    private lateinit var txtAnswerB : TextView
    private lateinit var txtAnswerC : TextView
    private lateinit var txtAnswerD : TextView
    private lateinit var frontCard : TextView
    private lateinit var backCard : TextView
    private var currentIndex = 0
    private var correctAnswer: String? = null
    private var answerReviewList: MutableList<Int> = mutableListOf()
    private var selectedAnswers: MutableList<String?> = mutableListOf()
    private var correctCount: Int = 0
    private var inCorrectCount: Int = 0;
    private lateinit var textToSpeech: TextToSpeech
    private var isEnglish = 3;
    private var isInstant = 3
    private var isSpeech = 3
    private var username = "22"
    var topicId = ""
    var loginUser: String? = ""

    private var wrongCardList: MutableList<Card> = mutableListOf()

    lateinit var cardList : List<Card>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_choice)

        cardList = intent.getSerializableExtra("cardList") as ArrayList<Card>

        closeButton = findViewById(R.id.btnClose)

        frontCard = findViewById(R.id.frontCard)
        backCard = findViewById(R.id.backCard)

        btnAnswerA = findViewById(R.id.btnAnswerA)
        btnAnswerB = findViewById(R.id.btnAnswerB)
        btnAnswerC = findViewById(R.id.btnAnswerC)
        btnAnswerD = findViewById(R.id.btnAnswerD)

        txtAnswerA = findViewById(R.id.txtAnswerA)
        txtAnswerB = findViewById(R.id.txtAnswerB)
        txtAnswerC = findViewById(R.id.txtAnswerC)
        txtAnswerD = findViewById(R.id.txtAnswerD)

        findViewById<TextView>(R.id.totalTerm).text = cardList.size.toString()
        textToSpeech = TextToSpeech(this, this)

        isEnglish = intent.getIntExtra("isEnglish",3)
        isInstant = intent.getIntExtra("isInstant",3)
        isSpeech = intent.getIntExtra("isSpeech",3)

        username = intent.getStringExtra("username").toString()
        topicId = intent.getStringExtra("topicId").toString()
        loginUser = intent.getStringExtra("loginUser")

        loadTerm(cardList[currentIndex])

        btnAnswerA.setOnClickListener {
            validateAnswer(txtAnswerA.text, cardList[currentIndex])
            moveToNextCard(cardList)
        }

        btnAnswerB.setOnClickListener {
            validateAnswer(txtAnswerB.text, cardList[currentIndex])
            moveToNextCard(cardList)
        }

        btnAnswerC.setOnClickListener {
            validateAnswer(txtAnswerC.text, cardList[currentIndex])
            moveToNextCard(cardList)
        }

        btnAnswerD.setOnClickListener {
            validateAnswer(txtAnswerD.text, cardList[currentIndex])
            moveToNextCard(cardList)
        }

        closeButton.setOnClickListener { finish() }
    }

    private fun validateAnswer(text: CharSequence?, card: Card) {
        if (text != null) {
            val isCorrect = text == correctAnswer
            if (isSpeech == 1)  {
                if (isEnglish == 1) {
                    speakText(card.english)
                } else {
                    speakText(card.vietnamese)
                }
            }
            if (isCorrect) {
                answerReviewList.add(1)
                correctCount ++
            } else {
                wrongCardList.add(card)
                answerReviewList.add(0)
                inCorrectCount ++;
            }
            selectedAnswers.add(text.toString())
            if (isInstant == 1) {
                Toast.makeText(
                    applicationContext,
                    if (isCorrect) "Correct" else "Incorrect",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun speakText(english: String) {
        textToSpeech.speak(english, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    override fun onDestroy() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()

        super.onDestroy()
    }
    fun moveToNextCard(cardList: List<Card>) {
        currentIndex++

        if (currentIndex < cardList.size) {
            loadTerm(cardList[currentIndex])
            findViewById<TextView>(R.id.indexTerm).text = (currentIndex + 1).toString()
        } else {
            var name = intent.getStringExtra("topicName")
            val intent = Intent(this, GameResultActivity::class.java)

            val totalTermText = findViewById<TextView>(R.id.totalTerm).text.toString()
            val totalTerm = totalTermText.toIntOrNull() ?: 0
            intent.putExtra("answerReviewList", answerReviewList.toIntArray())
            intent.putExtra("correctCount", correctCount)
            intent.putExtra("incorrectCount", inCorrectCount)
            intent.putExtra("totalTerm", totalTerm)
            intent.putExtra("topicName", name)
            intent.putExtra("username",username)
            intent.putExtra("topicId",topicId)
            intent.putExtra("loginUser",loginUser)
            intent.putParcelableArrayListExtra("wrongCardList", ArrayList(wrongCardList))
            intent.putStringArrayListExtra("selectedAnswers", ArrayList(selectedAnswers))
            intent.putParcelableArrayListExtra("cardList", ArrayList(cardList))
            intent.putExtra("game", "multi")

            startActivity(intent)
            finish()
        }
    }

    private fun loadTerm(card: Card) {
        val isEnglish = isEnglish == 1
        frontCard.text = if (isEnglish) card.english else card.vietnamese

        if (cardList.size < 4) {
            val incorrectAnswers = extendedCardList.shuffled().take(4 - cardList.size).toMutableList()
            val correctAnswerPosition = (0 until cardList.size).random()

            val answerOptions = mutableListOf<String?>()
            for (i in 0 until 4) {
                val answer = if (i == correctAnswerPosition) {
                    if (isEnglish) card.vietnamese else card.english
                } else {
                    val incorrectAnswer = if (incorrectAnswers.isNotEmpty()) {
                        if (isEnglish) incorrectAnswers.removeAt(0).vietnamese else incorrectAnswers.removeAt(0).english
                    } else {
                        if (isEnglish)  extendedCardList[i].vietnamese else  extendedCardList[i].english
                    }
                    incorrectAnswer
                }
                answerOptions.add(answer)
            }

            val shuffledOptions = answerOptions.shuffled()

            txtAnswerA.text = shuffledOptions[0]
            txtAnswerB.text = shuffledOptions[1]
            txtAnswerC.text = shuffledOptions[2]
            txtAnswerD.text = shuffledOptions[3]

            correctAnswer = if (isEnglish) card.vietnamese else card.english
        } else {
            val incorrectAnswers = cardList
                .filter { it != card }
                .shuffled()
                .take(3)
                .toMutableList()

            val correctAnswerPosition = (0..3).random()

            val answerOptions = mutableListOf<String>()
            for (i in 0 until 4) {
                val answer = if (i == correctAnswerPosition) {
                    if (isEnglish) card.vietnamese else card.english
                } else {
                    val incorrectAnswer = incorrectAnswers.removeAt(0)
                    if (isEnglish) incorrectAnswer.vietnamese else incorrectAnswer.english
                }
                answerOptions.add(answer)
            }

            val shuffledOptions = answerOptions.shuffled()

            txtAnswerA.text = shuffledOptions[0]
            txtAnswerB.text = shuffledOptions[1]
            txtAnswerC.text = shuffledOptions[2]
            txtAnswerD.text = shuffledOptions[3]

            correctAnswer = if (isEnglish) card.vietnamese else card.english

        }
    }


    fun <T> AppCompatActivity.startActivity(activityClass: Class<T>) {
        startActivity(Intent(this, activityClass))
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "Language is not supported")
            }
        } else {
            Log.e("TextToSpeech", "Initialization failed")
        }
    }

}