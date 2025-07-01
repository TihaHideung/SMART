package com.example.smart

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class account : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val imageProfile = findViewById<ImageView>(R.id.imageProfile)
        val nameText = findViewById<TextView>(R.id.textName)
        val emailText = findViewById<TextView>(R.id.textEmail)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        val userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            .getInt("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "User ID tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        RetrofitClient.instance.getUser(userId)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val user = response.body()?.user
                        nameText.text = user?.name ?: "-"
                        emailText.text = user?.email ?: "-"

                        // Cek dan load gambar profil
                        if (!user?.profile_picture.isNullOrEmpty()) {
                            val fullImageUrl = RetrofitClient.BASE_URL + user?.profile_picture
                            Glide.with(this@account)
                                .load(fullImageUrl)
                                .placeholder(R.drawable.user)
                                .error(R.drawable.user)
                                .into(imageProfile)
                        }
                    } else {
                        Toast.makeText(this@account, "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(this@account, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
