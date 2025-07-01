package com.example.smart

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var logContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log) // pastikan file ini sesuai

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // kembali ke halaman sebelumnya
        }

        logContainer = findViewById(R.id.logContainer)

        // Ambil userId dari SharedPreferences
        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            .getInt("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Login dulu!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadLogs()
    }

    private fun loadLogs() {
        RetrofitClient.instance.getLogs(userId).enqueue(object : Callback<LogResponse> {
            override fun onResponse(call: Call<LogResponse>, response: Response<LogResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val logs = response.body()?.logs ?: return

                    for (log in logs) {
                        val logItem = LinearLayout(this@LogActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setBackgroundResource(R.drawable.task_background) // gunakan background yang sama seperti task
                            setPadding(24, 16, 24, 16)

                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(0, 8, 0, 8)
                            layoutParams = params
                        }

                        val logText = TextView(this@LogActivity).apply {
                            text = "• ${log.activity}\n${log.timestamp}"
                            textSize = 14f
                            setTextColor(Color.BLACK)
                        }
                        logItem.addView(logText)
                        logContainer.addView(logItem)
                    }
                } else {
                    Toast.makeText(this@LogActivity, "Gagal memuat log", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LogResponse>, t: Throwable) {
                Toast.makeText(this@LogActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
