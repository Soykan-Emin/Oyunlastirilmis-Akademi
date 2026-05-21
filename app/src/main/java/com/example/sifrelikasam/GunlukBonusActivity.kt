package com.example.sifrelikasam

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.sifrelikasam.databinding.ActivityGunlukBonusBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class GunlukBonusActivity : AppCompatActivity() {

    private var enerjiCount = 0
    private var elmasCount = 0
    private lateinit var elmasCountRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val database = Firebase.database
    private lateinit var database1: FirebaseDatabase
    
    private var mathGameTimer: CountDownTimer? = null
    private var mathCorrectAnswer: Int = 0
    private var mathQuestionCount = 0
    private var mathScore = 0

    private var colorGameTimer: CountDownTimer? = null
    private var colorCorrectAnswer: String = ""
    private var colorQuestionCount = 0
    private var colorScore = 0
    private val colorNames = listOf("KIRMIZI", "MAVİ", "YEŞİL", "SARI")
    private val colorValues = listOf(
        android.graphics.Color.RED,
        android.graphics.Color.BLUE,
        android.graphics.Color.GREEN,
        android.graphics.Color.YELLOW
    )
    
    private var iqGameTimer: CountDownTimer? = null
    private var iqCorrectAnswer: String = ""
    private var iqQuestionCount = 0
    private var iqScore = 0
    private val iqQuestions = listOf(
        Pair("1, 4, 9, 16, ?", "25"),
        Pair("2, 6, 12, 20, ?", "30"),
        Pair("1, 1, 2, 3, 5, ?", "8"),
        Pair("3, 9, 27, 81, ?", "243"),
        Pair("10, 22, 46, 94, ?", "190"),
        Pair("8, 64, 216, 512, ?", "1000"),
        Pair("5, 7, 11, 17, ?", "25"),
        Pair("2, 3, 5, 7, 11, ?", "13"),
        Pair("120, 60, 20, 5, ?", "1"),
        Pair("0, 3, 8, 15, ?", "24")
    )

    private lateinit var binding: ActivityGunlukBonusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGunlukBonusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database1 = FirebaseDatabase.getInstance()

        binding.btnhamster1.isEnabled = false
        binding.lythamster.isEnabled = false
        binding.btnGunluk1.visibility = View.VISIBLE //Silme!!

        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null){
            checkBanStatus(currentUser.uid)
        }else{
            signInAnoymously()
        }

        val user: FirebaseUser? = auth.currentUser
        user?.let {
            val userId = it.uid
            elmasCountRef = database.getReference("hamsterkombatELMAS").child(userId).child("elmas")

            // Tıklama sayısını veritabanından al
            elmasCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        elmasCount = snapshot.getValue(Int::class.java) ?: 0
                    }
                    updateClickCount()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        binding.btngunlukgorev.setOnClickListener {
            Toast.makeText(this@GunlukBonusActivity, "Tamamlamak için hamster'a tıkla", Toast.LENGTH_SHORT).show()
            binding.btnhamster1.isEnabled = true
            binding.lythamster.isEnabled = true
            currentUser?.let {
                banUserForOneMinute(it.uid)
            }
        }
        
        setupMathGame()
        setupColorGame()
        setupIqGame()

        binding.lythamster.setOnClickListener {
            binding.btnhamster1.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        binding.btnhamster1.setImageResource(R.drawable.hamster)
                        binding.txtartibir.visibility = View.VISIBLE
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        binding.btnhamster1.setImageResource(R.drawable.hamster1)
                        binding.txtartibir.visibility = View.INVISIBLE

                        elmasCount++
                        updateClickCount()
                        enerjiCount++
                        binding.txtenerji.text = "$enerjiCount/5000"
                        if (enerjiCount >= 5000) {
                            binding.txtartibir.visibility = View.INVISIBLE
                            binding.lythamster.isEnabled = false
                            binding.btnhamster1.visibility = View.INVISIBLE
                            binding.btnhamster3.visibility = View.VISIBLE
                        }
                    }
                }
                true
            }
        }


            binding.btnKategori.setOnClickListener {
                intent = Intent(this, KategoriActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.txtKategori.setOnClickListener {
                intent = Intent(this, KategoriActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.btnAnasayfa.setOnClickListener {
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.txtAnaSayfa.setOnClickListener {
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.btnProfil.setOnClickListener {
                intent = Intent(this, profilMenuActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.txtProfil.setOnClickListener {
                intent = Intent(this, profilMenuActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    private fun updateClickCount() {
        binding.txtelmas.text = "$elmasCount"

        elmasCountRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                mutableData.value = elmasCount
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (error != null) {

                } else {

                }
            }
        })
    }

    private fun banUserForOneMinute(uid: String) {
        val banEndTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)
        val userRef = database.getReference("userbanGunlukBonus/$uid")
        userRef.child("banEndTime").setValue(banEndTime).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateBanStatus(uid)
            } else {
                binding.txtkalansuregunlukbonus.text = "Banlama işlemi başarısız oldu."
            }
        }
    }

    private fun signInAnoymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        checkBanStatus(it.uid)
                    }
                } else {
                    binding.txtkalansuregunlukbonus.text = "Oturum açılamadı."
                }
            }
    }

    private fun checkBanStatus(uid: String) {
        val userRef = database.getReference("userbanGunlukBonus/$uid")
        userRef.child("banEndTime").get().addOnSuccessListener { snapshot ->
            val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
            if (banEndTime > System.currentTimeMillis()){
                updateBanStatus(uid)
            }else{
                binding.txtkalansuregunlukbonus.text = "Bugunkü HamsterKombatı Tamamla"
                binding.lythamster.isEnabled = true
                binding.btngunlukgorev.isEnabled=true
            }
        }
    }

    private fun updateBanStatus(uid: String) {
        val userRef = database.getReference("userbanGunlukBonus/$uid")
        userRef.child("banEndTime").get().addOnSuccessListener { snapshot ->
            val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
            if (banEndTime > System.currentTimeMillis()) {
                val remainingTime = banEndTime - System.currentTimeMillis()
                binding.btngunlukgorev.isEnabled = false

                object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsRemaining = millisUntilFinished / 1000
                        binding.txtkalansuregunlukbonus.text = "$secondsRemaining"
                    }

                    override fun onFinish() {
                        binding.txtkalansuregunlukbonus.text = "Bugunkü HamsterKombatı Tamamla."
                        binding.btngunlukgorev.isEnabled = true
                        binding.lythamster.isEnabled = true
                    }
                }.start()
            } else {
                binding.txtkalansuregunlukbonus.text = "Bugunkü HamsterKombatı Tamamla."
                binding.btngunlukgorev.isEnabled = true
                binding.lythamster.isEnabled = true
            }
        }
    }

    private fun setupMathGame() {
        val user = auth.currentUser
        if (user != null) {
            checkMathBanStatus(user.uid)
        }

        binding.btnMathStart.setOnClickListener {
            mathQuestionCount = 0
            mathScore = 0
            startMathGame()
        }
        
        binding.btnMathOpt1.setOnClickListener {
            checkMathAnswer(binding.btnMathOpt1.text.toString().toIntOrNull() ?: 0)
        }
        
        binding.btnMathOpt2.setOnClickListener {
            checkMathAnswer(binding.btnMathOpt2.text.toString().toIntOrNull() ?: 0)
        }
    }

    private fun startMathGame() {
        binding.btnMathStart.visibility = View.GONE
        binding.lytMathOptions.visibility = View.VISIBLE
        binding.txtMathQuestion.setTextColor(resources.getColor(R.color.white, null))
        
        if (mathQuestionCount >= 5) {
            endMathGame()
            return
        }

        mathQuestionCount++
        
        val num1 = (10..50).random()
        val num2 = (10..50).random()
        mathCorrectAnswer = num1 + num2
        
        val wrongAnswer = mathCorrectAnswer + (-5..5).filter { it != 0 }.random()
        
        binding.txtMathQuestion.text = "Soru $mathQuestionCount/5\n$num1 + $num2 = ?"
        
        if ((0..1).random() == 0) {
            binding.btnMathOpt1.text = mathCorrectAnswer.toString()
            binding.btnMathOpt2.text = wrongAnswer.toString()
        } else {
            binding.btnMathOpt1.text = wrongAnswer.toString()
            binding.btnMathOpt2.text = mathCorrectAnswer.toString()
        }
        
        mathGameTimer?.cancel()
        mathGameTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.txtMathQuestion.text = "Soru $mathQuestionCount/5\n$num1 + $num2 = ?\nSüre: ${millisUntilFinished / 1000}"
            }
            override fun onFinish() {
                startMathGame() // Süre bitince yanlış sayıp sıradaki soruya geç
            }
        }.start()
    }
    
    private fun checkMathAnswer(selected: Int) {
        mathGameTimer?.cancel()
        if (selected == mathCorrectAnswer) {
            mathScore++
        }
        startMathGame()
    }
    
    private fun endMathGame() {
        binding.lytMathOptions.visibility = View.GONE
        val user = auth.currentUser
        val diamondsEarned = mathScore * 2
        
        binding.txtMathQuestion.text = "Oyun Bitti!\n$mathScore/5 Doğru\n+$diamondsEarned Elmas"
        binding.txtMathQuestion.setTextColor(if(mathScore > 0) android.graphics.Color.GREEN else android.graphics.Color.RED)
        
        if (diamondsEarned > 0) {
            elmasCount += diamondsEarned
            updateClickCount()
            Toast.makeText(this, "$diamondsEarned Elmas kazandın!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Hiç doğru bilemedin. Yarın tekrar dene!", Toast.LENGTH_SHORT).show()
        }
        
        user?.let {
            val banEndTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)
            database.getReference("userbanMathGame/${it.uid}/banEndTime").setValue(banEndTime)
            checkMathBanStatus(it.uid)
        }
    }
    
    private fun checkMathBanStatus(uid: String) {
        database.getReference("userbanMathGame/$uid/banEndTime").get().addOnSuccessListener { snapshot ->
            val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
            if (banEndTime > System.currentTimeMillis()) {
                binding.btnMathStart.isEnabled = false
                binding.btnMathStart.text = "Bugünlük hakkın bitti"
                
                if (!binding.txtMathQuestion.text.toString().contains("Elmas")) {
                    binding.txtMathQuestion.text = "Yarın tekrar gel!"
                }
            }
        }
    }

    private fun setupColorGame() {
        val user = auth.currentUser
        if (user != null) {
            checkColorBanStatus(user.uid)
        }

        binding.btnColorStart.setOnClickListener {
            colorQuestionCount = 0
            colorScore = 0
            startColorGame()
        }
        
        binding.btnColorOpt1.setOnClickListener {
            checkColorAnswer(binding.btnColorOpt1.text.toString())
        }
        
        binding.btnColorOpt2.setOnClickListener {
            checkColorAnswer(binding.btnColorOpt2.text.toString())
        }
    }

    private fun startColorGame() {
        binding.btnColorStart.visibility = View.GONE
        binding.lytColorOptions.visibility = View.VISIBLE
        
        if (colorQuestionCount >= 5) {
            endColorGame()
            return
        }

        colorQuestionCount++
        
        val randomNameIndex = (0..3).random()
        val randomColorIndex = (0..3).random()
        
        colorCorrectAnswer = colorNames[randomColorIndex] // Correct answer is the ACTUAL color of the text
        val wrongAnswer = colorNames[(0..3).filter { it != randomColorIndex }.random()]
        
        binding.txtColorQuestion.text = "Soru $colorQuestionCount/5\n" + colorNames[randomNameIndex]
        binding.txtColorQuestion.setTextColor(colorValues[randomColorIndex])
        
        if ((0..1).random() == 0) {
            binding.btnColorOpt1.text = colorCorrectAnswer
            binding.btnColorOpt2.text = wrongAnswer
        } else {
            binding.btnColorOpt1.text = wrongAnswer
            binding.btnColorOpt2.text = colorCorrectAnswer
        }
        
        colorGameTimer?.cancel()
        colorGameTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                 binding.txtColorQuestion.text = "Soru $colorQuestionCount/5\n" + colorNames[randomNameIndex] + "\nSüre: ${millisUntilFinished / 1000}"
            }
            override fun onFinish() {
                startColorGame()
            }
        }.start()
    }

    private fun checkColorAnswer(selected: String) {
        colorGameTimer?.cancel()
        if (selected == colorCorrectAnswer) {
            colorScore++
        }
        startColorGame()
    }

    private fun endColorGame() {
        binding.lytColorOptions.visibility = View.GONE
        val user = auth.currentUser
        val diamondsEarned = colorScore * 2
        
        binding.txtColorQuestion.text = "Oyun Bitti!\n$colorScore/5 Doğru\n+$diamondsEarned Elmas"
        binding.txtColorQuestion.setTextColor(if(colorScore > 0) android.graphics.Color.GREEN else android.graphics.Color.RED)
        
        if (diamondsEarned > 0) {
            elmasCount += diamondsEarned
            updateClickCount()
            Toast.makeText(this, "$diamondsEarned Elmas kazandın!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Hiç doğru bilemedin. Yarın tekrar dene!", Toast.LENGTH_SHORT).show()
        }
        
        user?.let {
            val banEndTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)
            database.getReference("userbanColorGame/${it.uid}/banEndTime").setValue(banEndTime)
            checkColorBanStatus(it.uid)
        }
    }

    private fun checkColorBanStatus(uid: String) {
        database.getReference("userbanColorGame/$uid/banEndTime").get().addOnSuccessListener { snapshot ->
            val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
            if (banEndTime > System.currentTimeMillis()) {
                binding.btnColorStart.isEnabled = false
                binding.btnColorStart.text = "Bugünlük hakkın bitti"
                
                if (!binding.txtColorQuestion.text.toString().contains("Elmas")) {
                    binding.txtColorQuestion.text = "Yarın tekrar gel!"
                    binding.txtColorQuestion.setTextColor(resources.getColor(R.color.white, null))
                }
            }
        }
    }
    
    private fun setupIqGame() {
        val user = auth.currentUser
        if (user != null) {
            checkIqBanStatus(user.uid)
        }

        binding.btnIqStart.setOnClickListener {
            iqQuestionCount = 0
            iqScore = 0
            startIqGame()
        }
        
        binding.btnIqOpt1.setOnClickListener {
            checkIqAnswer(binding.btnIqOpt1.text.toString())
        }
        
        binding.btnIqOpt2.setOnClickListener {
            checkIqAnswer(binding.btnIqOpt2.text.toString())
        }
    }

    private fun startIqGame() {
        binding.btnIqStart.visibility = View.GONE
        binding.lytIqOptions.visibility = View.VISIBLE
        binding.txtIqQuestion.setTextColor(resources.getColor(R.color.white, null))
        
        if (iqQuestionCount >= 5) {
            endIqGame()
            return
        }

        iqQuestionCount++
        
        val randomQ = iqQuestions.random()
        iqCorrectAnswer = randomQ.second
        
        val correctInt = iqCorrectAnswer.toIntOrNull() ?: 0
        val wrongAnswer = if (correctInt > 0) {
            (correctInt + (-5..5).filter { it != 0 }.random()).toString()
        } else {
            "0"
        }
        
        binding.txtIqQuestion.text = "Soru $iqQuestionCount/5\n${randomQ.first}"
        
        if ((0..1).random() == 0) {
            binding.btnIqOpt1.text = iqCorrectAnswer
            binding.btnIqOpt2.text = wrongAnswer
        } else {
            binding.btnIqOpt1.text = wrongAnswer
            binding.btnIqOpt2.text = iqCorrectAnswer
        }
        
        iqGameTimer?.cancel()
        iqGameTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                 binding.txtIqQuestion.text = "Soru $iqQuestionCount/5\n${randomQ.first}\nSüre: ${millisUntilFinished / 1000}"
            }
            override fun onFinish() {
                startIqGame()
            }
        }.start()
    }

    private fun checkIqAnswer(selected: String) {
        iqGameTimer?.cancel()
        if (selected == iqCorrectAnswer) {
            iqScore++
        }
        startIqGame()
    }

    private fun endIqGame() {
        binding.lytIqOptions.visibility = View.GONE
        val user = auth.currentUser
        val diamondsEarned = iqScore * 4
        
        binding.txtIqQuestion.text = "Test Bitti!\n$iqScore/5 Doğru\n+$diamondsEarned Elmas"
        binding.txtIqQuestion.setTextColor(if(iqScore > 0) android.graphics.Color.GREEN else android.graphics.Color.RED)
        
        if (diamondsEarned > 0) {
            elmasCount += diamondsEarned
            updateClickCount()
            Toast.makeText(this, "$diamondsEarned Elmas kazandın!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Hiç doğru bilemedin. Yarın tekrar dene!", Toast.LENGTH_SHORT).show()
        }
        
        user?.let {
            val banEndTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)
            database.getReference("userbanIqGame/${it.uid}/banEndTime").setValue(banEndTime)
            checkIqBanStatus(it.uid)
        }
    }

    private fun checkIqBanStatus(uid: String) {
        database.getReference("userbanIqGame/$uid/banEndTime").get().addOnSuccessListener { snapshot ->
            val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
            if (banEndTime > System.currentTimeMillis()) {
                binding.btnIqStart.isEnabled = false
                binding.btnIqStart.text = "Bugünlük hakkın bitti"
                
                if (!binding.txtIqQuestion.text.toString().contains("Elmas")) {
                    binding.txtIqQuestion.text = "Yarın tekrar gel!"
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mathGameTimer?.cancel()
        colorGameTimer?.cancel()
        iqGameTimer?.cancel()
    }
}
