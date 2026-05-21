package com.example.sifrelikasam

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.sifrelikasam.databinding.ActivityKategoriBinding


class KategoriActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKategoriBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKategoriBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.wbInooster.getSettings().setJavaScriptEnabled(true); // JavaScript'i etkinleştir
        binding.wbInooster.loadUrl("https://oto-galeri11.vercel.app/"); // Gösterilecek web sayfasının URL'si


        binding.btnKategori1.visibility = View.VISIBLE

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
}