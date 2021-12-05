package com.trime.f4r4w4y.autorr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.trime.f4r4w4y.autorr.gql.QueryViewModel
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private lateinit var qViewModel: QueryViewModel
    private var loginButton: Button? = null
    private var emailTextField: TextInputLayout? = null
    private var passwordTextField: TextInputLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupUI()
        qViewModel = ViewModelProvider(this)[QueryViewModel::class.java]

        loginButton?.setOnClickListener {
            if (emailTextField?.editText?.text.toString() == "") {
                emailTextField?.error = "You need to fill in the email"
                return@setOnClickListener
            }

            if (passwordTextField?.editText?.text.toString() == "") {
                passwordTextField?.error = "You need to fill in the password"
                return@setOnClickListener
            }

            if (!isOnline()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "No internet connections available :(",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            qViewModel.login(
                emailTextField?.editText?.text.toString(),
                passwordTextField?.editText?.text.toString()
            ).observe(this,
                { token ->
                    if (token != "nope" && token != "Password is wrong" && token != "User with that email is not found") {
                        val editor = getSharedPreferences("autorr_pref", MODE_PRIVATE).edit()
                        editor.putString("login_token", token)
                        editor.apply()

                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    } else if (token == "Password is wrong" || token == "User with that email is not found") {
                        // if you read this code, yeah i know, i should have abstracted this in the backend also,
                        // but due to the fact that i have no time left to write these stuff, so just don't tell this to anyone
                        // that could not read the code okay XD
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Email or Password is incorrect",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else if (token == "nope") {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error happened, please contact me (fakhrip@protonmail.com)",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error happened: $token, please contact me (fakhrip@protonmail.com)",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }

    fun isOnline(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return exitValue == 0
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }

    private fun setupUI() {
        loginButton = findViewById(R.id.login_button)
        emailTextField = findViewById(R.id.emailTextField)
        passwordTextField = findViewById(R.id.passwordTextField)

        emailTextField?.editText?.doOnTextChanged { _, _, _, _ ->
            emailTextField?.error = ""
        }

        passwordTextField?.editText?.doOnTextChanged { _, _, _, _ ->
            passwordTextField?.error = ""
        }
    }
}