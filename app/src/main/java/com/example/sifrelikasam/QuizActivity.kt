package com.example.sifrelikasam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Color
import android.content.res.ColorStateList
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.sifrelikasam.databinding.ActivityQuizBinding
import com.example.sifrelikasam.databinding.ScoreDialogBinding
import kotlin.math.min
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

class QuizActivity : AppCompatActivity(),View.OnClickListener {

    companion object {
        var questionModelList: List<QuestionModel> = listOf()
        var time: String = ""
        var quizId: String = ""
    }
    lateinit var binding: ActivityQuizBinding
    
    var countDownTimer: CountDownTimer? = null

    var currentQuestionIndex = 0;
    var selectedAnswer = ""
    var score = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            btn0.setOnClickListener(this@QuizActivity)
            btn1.setOnClickListener(this@QuizActivity)
            btn2.setOnClickListener(this@QuizActivity)
            btn3.setOnClickListener(this@QuizActivity)
            nextBtn.setOnClickListener(this@QuizActivity)
        }

        loadQuestions()
        startTimer()
    }

    fun mainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish();

    }

    private fun startTimer() {
        val totalTimeInMillis = time.toInt() * 60 * 1000L
        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                binding.timerIndicatorTextview.text =
                    String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                Toast.makeText(this@QuizActivity, "Süreniz Bitti", Toast.LENGTH_SHORT).show()
                finish()
                mainActivity()
            }

        }.start()
    }

    private fun loadQuestions() {
        selectedAnswer = ""
        if (currentQuestionIndex == questionModelList.size) {
            finishQuiz()
            return
        }

        binding.apply {
            questionIndicatorTextview.text =
                "Soru ${currentQuestionIndex + 1}/ ${questionModelList.size} "
            questionProgressIndicator.progress =
                (currentQuestionIndex.toFloat() / questionModelList.size.toFloat() * 100).toInt()
            questionTextview.text = questionModelList[currentQuestionIndex].question
            btn0.text = questionModelList[currentQuestionIndex].options[0]
            btn1.text = questionModelList[currentQuestionIndex].options[1]
            btn2.text = questionModelList[currentQuestionIndex].options[2]
            btn3.text = questionModelList[currentQuestionIndex].options[3]

            // Reset selection styles for new question
            val unselectedColor = ColorStateList.valueOf(getColor(R.color.glass_white))
            val whiteText = getColor(R.color.white)

            btn0.backgroundTintList = unselectedColor
            btn0.setTextColor(whiteText)
            btn1.backgroundTintList = unselectedColor
            btn1.setTextColor(whiteText)
            btn2.backgroundTintList = unselectedColor
            btn2.setTextColor(whiteText)
            btn3.backgroundTintList = unselectedColor
            btn3.setTextColor(whiteText)
        }
    }

    override fun onClick(view: View?) {

        binding.apply {
            val unselectedColor = ColorStateList.valueOf(getColor(R.color.glass_white))
            val whiteText = getColor(R.color.white)

            btn0.backgroundTintList = unselectedColor
            btn0.setTextColor(whiteText)
            btn1.backgroundTintList = unselectedColor
            btn1.setTextColor(whiteText)
            btn2.backgroundTintList = unselectedColor
            btn2.setTextColor(whiteText)
            btn3.backgroundTintList = unselectedColor
            btn3.setTextColor(whiteText)
        }

        val clickedBtn = view as Button
        if (clickedBtn.id == R.id.next_btn) {
            //next button is clicked
            if (selectedAnswer.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Devam etmek için lütfen cevabı seçin",
                    Toast.LENGTH_SHORT
                ).show()
                return;
            }
            if (selectedAnswer == questionModelList[currentQuestionIndex].correct) {
                score++
                Log.i("Sınavın özü", score.toString())
            }
            currentQuestionIndex++
            loadQuestions()
        } else {
            //options button is clicked
            selectedAnswer = clickedBtn.text.toString()
            clickedBtn.backgroundTintList = ColorStateList.valueOf(getColor(R.color.cyan_accent))
            clickedBtn.setTextColor(getColor(R.color.deep_purple))
        }
    }

    override fun onBackPressed() {
        // Kullanıcı geri tuşuna bastığında diğer Activity'e geçiş yap
        countDownTimer?.cancel()
        val intent = Intent(this, YolculukDagActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun finishQuiz() {
        countDownTimer?.cancel()
        val totalQuestions = questionModelList.size
        val percentage = ((score.toFloat() / totalQuestions.toFloat()) * 100).toInt()
        
        // Elmas hesaplama: Doğru başına 10 elmas
        val diamondsEarned = score * 10

        val dialogBinding = ScoreDialogBinding.inflate(layoutInflater)
        dialogBinding.apply {
            scoreProgressIndicator.progress = percentage
            scoreProgressText.text = "$percentage %"
            if (percentage > 60) {
                scoreTitle.text = "Tebrikler! Geçtin"
                scoreTitle.setTextColor(Color.BLUE)
            } else {
                scoreTitle.text = "Hata! Başarısız oldun"
                scoreTitle.setTextColor(Color.RED)
            }
            scoreSubtitle.text = "$score Soruların $totalQuestions Doğru\nKazanılan Elmas: $diamondsEarned"
            finishBtn.setOnClickListener {
                finish()
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()

        // Firebase Güncellemesi: Quiz'i tamamlandı işaretle ve elmas ekle
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && quizId.isNotEmpty()) {
            val db = FirebaseDatabase.getInstance()
            
            // Quiz'i tamamlandı olarak işaretle
            db.getReference("CompletedQuizzes").child(user.uid).child(quizId).setValue(true)
            
            // Elmas ekle
            if (diamondsEarned > 0) {
                val elmasRef = db.getReference("hamsterkombatELMAS").child(user.uid).child("elmas")
                elmasRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        val currentElmas = mutableData.getValue(Int::class.java) ?: 0
                        mutableData.value = currentElmas + diamondsEarned
                        return Transaction.success(mutableData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {}
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Aktivite yok edilirken, eğer henüz çalışmamışsa bu işlemi iptal et
        countDownTimer?.cancel()
    }
}