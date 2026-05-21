package com.example.sifrelikasam

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import com.example.sifrelikasam.databinding.ActivityPasswordForgetBinding
import com.example.sifrelikasam.ui.login.LoginUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class PasswordForgetActivity : AppCompatActivity() {
    private var question: String = ""
    private var correctAnswer: Int = 0
    private lateinit var binding: ActivityPasswordForgetBinding
    private val debounceInterval: Long = 1
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordForgetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.username.visibility = View.INVISIBLE
        binding.answerEditText.visibility = View.INVISIBLE
        binding.resultTextView.visibility = View.INVISIBLE
        binding.username.isEnabled = false
        binding.btnMailGonder.isEnabled = false
        binding.btnDogrula.visibility = View.INVISIBLE
        binding.username.setAlpha(0.5f);
        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        generateQuestion()

        binding.checkBox.setOnClickListener {
            binding.username.visibility = View.INVISIBLE
            binding.btnMailGonder.visibility = View.INVISIBLE
            binding.answerEditText.visibility = View.VISIBLE
            binding.btnDogrula.visibility = View.VISIBLE
            binding.resultTextView.visibility = View.VISIBLE
            binding.imageView1.visibility = View.INVISIBLE
            binding.btnDogrula.visibility = View.VISIBLE
            binding.checkBox.isEnabled = false
        }

        binding.btnDogrula.setOnClickListener {

            val userAnswer = binding.answerEditText.text.toString()
            if (userAnswer.isNotEmpty() && userAnswer.toIntOrNull() == correctAnswer) {
                Toast.makeText(this, "Doğru cevap! Lütfen Mail Adresinizi Giriniz", Toast.LENGTH_SHORT).show()
                binding.resultTextView.text = "Doğru cevap! İşlem devam ediyor."
                binding.answerEditText.text.clear()
                binding.btnDogrula.visibility = View.INVISIBLE
                binding.btnMailGonder.visibility = View.VISIBLE
                binding.username.visibility = View.VISIBLE
                binding.answerEditText.visibility = View.INVISIBLE
                binding.username.isEnabled = true
                binding.btnMailGonder.isEnabled = true
                binding.username.setAlpha(1f);
                binding.resultTextView.visibility = View.INVISIBLE
                binding.imageView1.visibility = View.VISIBLE
            } else {
                binding.answerEditText.text.clear()
                binding.resultTextView.text = "Yanlış cevap. Lütfen tekrar deneyin."
                generateQuestion()
            }
        }

        binding.btngeridon.setOnClickListener {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            //992021
        }

        binding.btnUyeol.setOnClickListener {
            intent = Intent(this, LoginUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnMailGonder.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val email = binding.username.text.toString()

            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Mail Gönderildi Konrtol Et", Toast.LENGTH_SHORT
                    ).show()
                    performAction()
                    binding.btnMailGonder.isEnabled = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.btnMailGonder.isEnabled = true
                    }, debounceInterval)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Mail Gönderilmedi Başarısız", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun generateQuestion() {
        // Rastgele iki sayı seç
        val number1 = Random.nextInt(1, 10)
        val number2 = Random.nextInt(1, 10)

        // Soruyu oluştur ve doğru cevabı hesapla
        question = "$number1 + $number2 = ?"
        correctAnswer = number1 + number2

        // Soruyu ekranda göster
        val questionTextView: TextView = findViewById(R.id.resultTextView)
        questionTextView.text = question

    }

    private fun performAction() {

    }

    override fun onBackPressed() {
        // Kullanıcı geri tuşuna bastığında diğer Activity'e geçiş yap
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun btnSifremiUnuttum(view: android.view.View) {

    }
}