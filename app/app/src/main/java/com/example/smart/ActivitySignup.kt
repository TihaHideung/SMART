package com.example.smart
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivitySignup : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val inputName = findViewById<EditText>(R.id.input_name)
        val inputEmail = findViewById<EditText>(R.id.input_email)
        val inputPassword = findViewById<EditText>(R.id.input_password)
        val inputConfirmPassword = findViewById<EditText>(R.id.input_confirm_password)
        val signUpButton = findViewById<Button>(R.id.btn_signup)
        val backToLogin = findViewById<TextView>(R.id.link_login)

        backToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        signUpButton.setOnClickListener {
            val name = inputName.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            val confirmPassword = inputConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.error = "Alamat email tidak valid"
                inputEmail.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kirim data registrasi ke server
            RetrofitClient.instance.register(name, email, password)
                .enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(
                        call: Call<RegisterResponse>,
                        response: Response<RegisterResponse>
                    ) {
                        if (response.isSuccessful) {
                            val result = response.body()
                            if (result?.status == "success") {
                                val userId = result.id_user ?: -1
                                if (userId != -1) {
                                    // Simpan user ID ke SharedPreferences
                                    getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit()
                                        .putInt("USER_ID", userId)
                                        .apply()

                                    Toast.makeText(
                                        this@ActivitySignup,
                                        result.message,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Kirim nama ke ActivityCreateProfil
                                    val intent = Intent(
                                        this@ActivitySignup,
                                        ActivityCreateProfil::class.java
                                    )
                                    intent.putExtra("name", name)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@ActivitySignup,
                                        "Gagal mendapatkan ID user",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@ActivitySignup,
                                    result?.message ?: "Registrasi gagal",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@ActivitySignup,
                                "Server error: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("SIGNUP", "Response not successful: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        Toast.makeText(
                            this@ActivitySignup,
                            "Gagal terhubung: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("SIGNUP", "onFailure: ${t.message}", t)
                    }
                })
        }
    }
}
