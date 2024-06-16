package com.adityaa0108.quizassignment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.adityaa0108.quizassignment.databinding.ActivityQuestionsBinding
import com.adityaa0108.quizassignment.model.Question
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader


class QuestionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionsBinding
    private lateinit var questions: List<Question>
    private var currentQuestionIndex: Int = 0
    private var timeLeftInMillis: Long = 600000 // 10 minutes in milliseconds
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var sharedPreferences: SharedPreferences
    private var score:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("quizApp", Context.MODE_PRIVATE)
        currentQuestionIndex = sharedPreferences.getInt("currentQuestionIndex", 0)
        timeLeftInMillis = sharedPreferences.getLong("timeLeftInMillis", 600000)

        loadQuestions()
        displayQuestion()
        startTimer()

        binding.nextQuestion.setOnClickListener {
            saveAnswer()
           nextQuestion()
        }
    }

    private fun loadQuestions() {
        val inputStream = resources.openRawResource(R.raw.quiz_questions)
        val reader = InputStreamReader(inputStream)
        val questionType = object : TypeToken<List<Question>>() {}.type
        questions = Gson().fromJson(reader, questionType)
        reader.close()
    }

    private fun displayQuestion() {
        binding.quiz.visibility = View.VISIBLE
        binding.startAgain.visibility = View.GONE
        binding.score.visibility = View.GONE
        binding.questionNo.text = "Question ${currentQuestionIndex+1}/10"
        val currentQuestion = questions[currentQuestionIndex]
        binding.question.text = currentQuestion.question
        binding.option1.text = currentQuestion.options[0]
        binding.option2.text = currentQuestion.options[1]
        binding.option3.text = currentQuestion.options[2]
        binding.option4.text = currentQuestion.options[3]


        //saving the state of the
        val savedAnswerIndex = sharedPreferences.getInt("answer$currentQuestionIndex", -1)
        if (savedAnswerIndex != -1) {
            binding.options.check(savedAnswerIndex)
        } else {
            binding.options.clearCheck()
        }
    }

    private fun saveAnswer() {
        val selectedOptionId = binding.options.checkedRadioButtonId
        with(sharedPreferences.edit()) {
            putInt("answer$currentQuestionIndex", selectedOptionId)
            apply()
        }
    }

    private fun nextQuestion() {

        val selectedOptionId = binding.options.checkedRadioButtonId
        val selectedRadioButton = findViewById<RadioButton>(selectedOptionId)
        val currentQuestion = questions[currentQuestionIndex]

        if (selectedRadioButton != null) {
            val selectedAnswer = selectedRadioButton.text.toString()
            if (selectedAnswer == currentQuestion.answer) {
                score++
            }
        }

        currentQuestionIndex++
        if (currentQuestionIndex >= questions.size) {
            endQuiz()
        } else {
            with(sharedPreferences.edit()) {
                putInt("currentQuestionIndex", currentQuestionIndex)
                apply()
            }
            displayQuestion()
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis  = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                endQuiz()
            }
        }.start()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.timer.text = "Time remaining: ${String.format("%02d:%02d", minutes, seconds)}"
    }

    private fun endQuiz() {
       countDownTimer.cancel()
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        binding.quiz.visibility = View.GONE
        binding.questionNo.text = "Quiz Over!"
        binding.score.visibility = View.VISIBLE
        binding.score.text = "Score is : $score"
        binding.options.clearCheck()
        binding.question.text = ""
        binding.question.text = ""
        binding.question.text = ""
        binding.question.text = ""
        binding.timer.text = ""
        binding.startAgain.visibility = View.VISIBLE

        binding.startAgain.setOnClickListener {
            currentQuestionIndex = 0
            timeLeftInMillis = 600000
            startTimer()
            displayQuestion()
        }
    }

    override fun onPause() {
        super.onPause()
        // Save the remaining time and current question index
        with(sharedPreferences.edit()) {
            putLong("timeLeftInMillis", timeLeftInMillis)
            putInt("currentQuestionIndex", currentQuestionIndex)
            apply()
        }
        countDownTimer.cancel()
    }

    override fun onRestart() {
        super.onRestart()
        startTimer()
    }
}
