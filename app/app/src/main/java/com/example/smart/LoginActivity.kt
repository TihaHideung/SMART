package com.example.smart

import LoginResponse
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val inputEmail = findViewById<EditText>(R.id.username)
        val inputPassword = findViewById<EditText>(R.id.input_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val signupLink = findViewById<TextView>(R.id.signup)

        signupLink.setOnClickListener {
            val intent = Intent(this, ActivitySignup::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (email.isEmpty()) {
                inputEmail.error = "Email harus diisi"
                inputEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.error = "Format email tidak valid"
                inputEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                inputPassword.error = "Password harus diisi"
                inputPassword.requestFocus()
                return@setOnClickListener
            }

            // Lanjutkan ke proses login
            RetrofitClient.instance.login(email, password).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success" && body.user != null) {
                            val userId = body.user.id_user
                            val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putInt("USER_ID", userId)
                                apply()
                            }

                            Toast.makeText(this@LoginActivity, "Login berhasil: ID $userId", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Login gagal: data user kosong atau tidak lengkap", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Login gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
