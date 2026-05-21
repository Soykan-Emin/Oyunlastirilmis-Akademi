package com.example.sifrelikasam

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sifrelikasam.GunlukBonusActivity
import com.example.sifrelikasam.KategoriActivity
import com.example.sifrelikasam.LoginActivity
import com.example.sifrelikasam.MainActivity
import com.example.sifrelikasam.databinding.ActivityProfilMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener


class profilMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfilMenuBinding

    private lateinit var elmasCountRef: DatabaseReference
    private lateinit var database1: FirebaseDatabase
    private var elmasCount = 0
    private lateinit var auth: FirebaseAuth

    companion object {
        lateinit var mAuth: FirebaseAuth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()

        var database: FirebaseDatabase = FirebaseDatabase.getInstance()
        //val reference: DatabaseReference = database.reference.child("Users")
        val reference1: DatabaseReference = database.reference
        auth = FirebaseAuth.getInstance()
        database1 = FirebaseDatabase.getInstance()

        binding.userDetails.text = updateData()

        reference1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val realName: String = snapshot.child("Users").child("surum").value.toString()
                binding.txtSurum.text = realName

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        binding.edtcopylink.setOnClickListener {
            shareText("https://play.google.com/store/apps/details?id=com.valvesoftware.android.steam.community&pcampaignid=web_share")
        }
        binding.btnShare.setOnClickListener {
            shareText("https://play.google.com/store/apps/details?id=com.valvesoftware.android.steam.community&pcampaignid=web_share")
        }

        val user: FirebaseUser? = auth.currentUser
        user?.let {
            val userId = it.uid
            elmasCountRef = database1.getReference("hamsterkombatELMAS").child(userId).child("elmas")

            // Tıklama sayısını veritabanından al
            elmasCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        elmasCount = snapshot.getValue(Int::class.java) ?: 0
                    }
                    updateClickCount()
                    updateButton2Enabled()

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        binding.btnsepet1.setOnClickListener {
            elmasCount -= 100
            Toast.makeText(this, "İşlem Başarılı Erken Çıkabilirsiniz IK Mail Gönderildi", Toast.LENGTH_SHORT).show()
            if (elmasCount < 0) {
                elmasCount = 0
            }
            updateClickCount()
            updateButton2Enabled()

        }

        binding.lytbos.setOnClickListener {
            binding.lytFeedback.visibility = View.GONE
            binding.lytDavet.visibility = View.GONE
            binding.btnfeedback.visibility = View.VISIBLE
            binding.txtgeribildirim.visibility = View.VISIBLE
            binding.btndavet.visibility = View.VISIBLE
            binding.txtdavet.visibility = View.VISIBLE
            binding.btnmagaza.visibility = View.VISIBLE
            binding.txtmagaza.visibility = View.VISIBLE
            binding.btnsss.visibility = View.VISIBLE
            binding.txtsss.visibility  = View.VISIBLE
            binding.lytMagaza.visibility = View.GONE
        }
        binding.btnfeedback.setOnClickListener {
            Toast.makeText(this, "Şikayet Veya Önerilerinizi iletebilirsiniz", Toast.LENGTH_SHORT).show()
            binding.lytFeedback.visibility = View.VISIBLE
            binding.btnfeedback.visibility = View.GONE
            binding.txtgeribildirim.visibility = View.GONE
            binding.btndavet.visibility = View.GONE
            binding.txtdavet.visibility = View.GONE
            binding.btnmagaza.visibility = View.GONE
            binding.txtmagaza.visibility = View.GONE
            binding.btnsss.visibility = View.GONE
            binding.txtsss.visibility  = View.GONE
        }
        binding.txtgeribildirim.setOnClickListener {
            Toast.makeText(this, "Şikayet Veya Önerilerinizi iletebilirsiniz", Toast.LENGTH_SHORT).show()
            binding.lytFeedback.visibility = View.VISIBLE
            binding.btnfeedback.visibility = View.GONE
            binding.txtgeribildirim.visibility = View.GONE
            binding.btndavet.visibility = View.GONE
            binding.txtdavet.visibility = View.GONE
            binding.btnmagaza.visibility = View.GONE
            binding.txtmagaza.visibility = View.GONE
            binding.btnsss.visibility = View.GONE
            binding.txtsss.visibility  = View.GONE
        }

        binding.txtdavet.setOnClickListener {
            Toast.makeText(this, "Paylaş butonuna yada linke tıklamanız yeterli", Toast.LENGTH_SHORT).show()
            binding.lytDavet.visibility = View.VISIBLE
            binding.btnfeedback.visibility = View.GONE
            binding.txtgeribildirim.visibility = View.GONE
            binding.btndavet.visibility = View.GONE
            binding.txtdavet.visibility = View.GONE
            binding.btnmagaza.visibility = View.GONE
            binding.txtmagaza.visibility = View.GONE
            binding.btnsss.visibility = View.GONE
            binding.txtsss.visibility  = View.GONE
        }

        binding.btndavet.setOnClickListener {
            Toast.makeText(this, "Paylaş butonuna yada linke tıklamanız yeterli", Toast.LENGTH_SHORT).show()
            binding.lytDavet.visibility = View.VISIBLE
            binding.btnfeedback.visibility = View.GONE
            binding.txtgeribildirim.visibility = View.GONE
            binding.btndavet.visibility = View.GONE
            binding.txtdavet.visibility = View.GONE
            binding.btnmagaza.visibility = View.GONE
            binding.btnmagaza.visibility = View.GONE
            binding.btnsss.visibility = View.GONE
            binding.txtsss.visibility  = View.GONE
        }

        binding.btnGeriBildirim.setOnClickListener {
            var auth = FirebaseAuth.getInstance()
            var database = FirebaseDatabase.getInstance().reference
            val user = auth.currentUser
            val inputText = binding.edtGeriBildirim.text.toString()

            if (user != null) {
                val email = user.email
                val userId = user.uid

                val messageData = mapOf(
                    "email" to email,
                    "message" to inputText
                )

                database.child("FeedBack").child(userId).push().setValue(messageData)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding.btnGeriBildirim.isEnabled = false
                            Toast.makeText(this, "Mesajınız ulaşıldı lütfen mailinizi kontrol edin", Toast.LENGTH_SHORT).show()
                            Handler(Looper.getMainLooper()).postDelayed({executeTask()
                            }, 3000)
                        } else {
                            binding.btnGeriBildirim.isEnabled = false
                            Toast.makeText(this, "HATA", Toast.LENGTH_SHORT).show()
                            Handler(Looper.getMainLooper()).postDelayed({executeTask()
                            }, 3000)
                        }
                    }
            }
        }

        binding.btnmagaza.setOnClickListener {
            binding.lytMagaza.visibility = View.VISIBLE
            binding.lytFeedback.visibility = View.GONE
            binding.btnfeedback.visibility = View.GONE
            binding.txtgeribildirim.visibility = View.GONE
            binding.btndavet.visibility = View.GONE
            binding.txtdavet.visibility = View.GONE
            binding.btnmagaza.visibility = View.GONE
            binding.txtmagaza.visibility = View.GONE
            binding.btnsss.visibility = View.GONE
            binding.txtsss.visibility  = View.GONE
        }
        binding.txtmagaza.setOnClickListener {
            binding.lytMagaza.visibility = View.VISIBLE
            binding.lytFeedback.visibility = View.GONE
            binding.btnfeedback.visibility = View.GONE
            binding.txtgeribildirim.visibility = View.GONE
            binding.btndavet.visibility = View.GONE
            binding.txtdavet.visibility = View.GONE
            binding.btnmagaza.visibility = View.GONE
            binding.txtmagaza.visibility = View.GONE
            binding.btnsss.visibility = View.GONE
            binding.txtsss.visibility  = View.GONE
        }

        binding.lytMagaza.setOnClickListener {

        }

        binding.btnProfil1.visibility = View.VISIBLE

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

        binding.btnGunluk.setOnClickListener {
            intent = Intent(this, GunlukBonusActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.txtGunluk.setOnClickListener {
            intent = Intent(this, GunlukBonusActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.signIn.setOnClickListener {
            mAuth.signOut()
            binding.userDetails.text = updateData()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }


    private fun updateButton2Enabled() {
        if (elmasCount <= 99) {
            binding.btnsepet1.isEnabled = false
            binding.textView22.setTextColor(Color.RED)
        }else{
            binding.btnsepet1.isEnabled = true
            binding.textView22.setTextColor(Color.GREEN)
        }
    }


    private fun updateClickCount() {
        binding.txtelmas1.text = "$elmasCount"

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

    private fun shareText(text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)

    }

    private fun executeTask() {
        binding.btnGeriBildirim.isEnabled = true
    }

    private fun updateData(): String {
        return " : ${mAuth.currentUser?.email}"
    }

    override fun onBackPressed() {
        // Kullanıcı geri tuşuna bastığında diğer Activity'e geçiş yap
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}