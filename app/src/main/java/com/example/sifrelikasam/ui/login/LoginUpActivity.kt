package com.example.sifrelikasam.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.example.sifrelikasam.LoginActivity
import com.example.sifrelikasam.PasswordForgetActivity
import com.example.sifrelikasam.databinding.ActivityLoginUpBinding

import com.example.sifrelikasam.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class LoginUpActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        val username = binding.username
        val password = binding.password
        val login = binding.btnUyeol
        val loading = binding.loading


        binding.btnSifre?.setOnClickListener {
            intent = Intent(this, PasswordForgetActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btngeridon?.setOnClickListener {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginUpActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login!!.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginUpActivity, Observer {
            val loginResult = it ?: return@Observer
            val email = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                val email = binding.username.text.toString().trim()
                val password = binding.password.text.toString().trim()
                Toast.makeText(
                    this@LoginUpActivity,
                    "Geçerli bir e-posta adresi girin",
                    Toast.LENGTH_SHORT
                ).show()
                return@Observer
            }

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                val email = binding.username.text.toString()
                val pass = binding.password.text.toString()
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Kayıt Başarılı Giriş Yapınız", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    } else {
                        Toast.makeText(this, "Kayıt Başarısız", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            setResult(Activity.RESULT_OK)
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login!!.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "Hoşgeldiniz Kayıt Başarılı Giriş Yapınız",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.btnUyeol?.setBackgroundColor(resources.getColor(R.color.myButtonColor))
            }
        })
    }
}