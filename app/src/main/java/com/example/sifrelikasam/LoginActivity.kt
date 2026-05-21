package com.example.sifrelikasam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sifrelikasam.databinding.ActivityLoginBinding
import com.example.sifrelikasam.ui.login.LoginUpActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {


    private val RC_SIGN_IN = 9001
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOturumac.isEnabled = false
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Load Remember Me Preference
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val isRemembered = sharedPreferences.getBoolean("remember_me", false)
        if (isRemembered) {
            val savedEmail = sharedPreferences.getString("email", "")
            val savedPassword = sharedPreferences.getString("password", "")
            binding.rememberMeCheckbox.isChecked = true
            binding.username.setText(savedEmail)
            binding.password.setText(savedPassword)
            binding.btnOturumac.isEnabled = true
        }

        // EditText1 üzerindeki metin değişikliklerini dinle7
        binding.username.addTextChangedListener(getTextWatcher())

        // EditText2 üzerindeki metin değişikliklerini dinle
        binding.password.addTextChangedListener(getTextWatcher())



        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<View>(R.id.google_sign_in_button).setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        binding.btnOturumac.setOnClickListener {
            val email = binding.username.text.toString()
            val password = binding.password.text.toString()

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                    override fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful) {
                            // Giriş başarılı ise, bilgileri kaydet
                            val editor = sharedPreferences.edit()
                            if (binding.rememberMeCheckbox.isChecked) {
                                editor.putBoolean("remember_me", true)
                                editor.putString("email", email)
                                editor.putString("password", password)
                            } else {
                                editor.clear()
                            }
                            editor.apply()

                            Toast.makeText(applicationContext, "Giriş Başarılı", Toast.LENGTH_SHORT)
                                .show()

                            // Örneğin, ana ekranı açabilirsiniz.
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()  // Giriş ekranını kapat
                        } else {
                            // Giriş sırasında bir hata oluştu.
                            // Hata mesajını almak için task.exception kullanabilirsiniz.
                            Toast.makeText(
                                applicationContext,
                                "Giriş Başarısız",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        }

        binding.btnUyeol.setOnClickListener {
            intent = Intent(this, LoginUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSifre.setOnClickListener {
            intent = Intent(this, PasswordForgetActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, /*accessToken=*/ null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@LoginActivity, "Authentication Failed.", Toast.LENGTH_SHORT)
                        .show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

    override fun onBackPressed() {
        // Kullanıcı geri tuşuna bastığında diğer Activity'e geçiş yap
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    public override fun onStart() {
        super.onStart()
        // Uygulamayı açtığında beni hatırla aktif ise otomatik giriş yapar
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val isRemembered = sharedPreferences.getBoolean("remember_me", false)
        val currentUser = mAuth.currentUser
        if (currentUser != null && isRemembered) {
            updateUI(currentUser)
        }
    }

    private fun getTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Değişiklik öncesi
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Değişiklik anında
            }

            override fun afterTextChanged(s: Editable?) {
                //binding.username.setAlpha(1.0f);

                binding.btnOturumac.setBackgroundColor(resources.getColor(R.color.myButtonColor))
                // Değişiklik sonrası
                val text1 = binding.username.text.toString()
                val text2 = binding.password.text.toString()

                // İki EditText de dolu ise düğmeyi etkinleştir
                binding.btnOturumac.isEnabled = text1.isNotEmpty() && text2.isNotEmpty()
            }
        }
    }
}