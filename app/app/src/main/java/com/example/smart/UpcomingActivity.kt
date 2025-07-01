package com.example.smart

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class UpcomingActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var taskContainer: LinearLayout
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("MMM d • EEEE", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming)

        taskContainer = findViewById(R.id.upcomingTaskContainer)
        val calendarRow = findViewById<LinearLayout>(R.id.calendarRow)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)

        // Ambil user ID dari SharedPreferences
        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "User ID tidak ditemukan, silakan login ulang", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Bottom navbar
        findViewById<LinearLayout>(R.id.navToday).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }

        // Menu dropdown
        menuButton.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.menu_dropdown, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_activity_log -> {
                        startActivity(Intent(this, LogActivity::class.java))
                        true
                    }
                    R.id.menu_account -> {
                        startActivity(Intent(this, account::class.java))
                        true
                    }
                    R.id.menu_settings -> {
                        startActivity(Intent(this, settings::class.java))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        loadTasksFromBackend()
        generateHorizontalCalendar(calendarRow)
    }

    private fun loadTasksFromBackend() {
        taskContainer.removeAllViews()

        RetrofitClient.instance.getTasks(userId).enqueue(object : Callback<GetTaskResponse> {
            override fun onResponse(call: Call<GetTaskResponse>, response: Response<GetTaskResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val taskList = response.body()?.tasks ?: return
                    val groupedTasks = taskList.groupBy { it.due_date }

                    for ((dateString, tasksOnDate) in groupedTasks) {
                        val date = dateFormat.parse(dateString)
                        val displayDate = displayFormat.format(date!!)

                        val dateHeader = TextView(this@UpcomingActivity).apply {
                            text = displayDate
                            setTextColor(Color.BLACK)
                            textSize = 16f
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, 24, 0, 8)
                        }
                        taskContainer.addView(dateHeader)

                        for (task in tasksOnDate) {
                            val container = LinearLayout(this@UpcomingActivity).apply {
                                orientation = LinearLayout.VERTICAL
                                setBackgroundResource(R.drawable.task_background)
                                setPadding(24, 16, 24, 16)

                                val params = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                params.setMargins(0, 8, 0, 8)
                                layoutParams = params
                            }

                            val checkbox = CheckBox(this@UpcomingActivity).apply {
                                text = "${task.title} • ${task.due_time}"
                                textSize = 14f
                                setTextColor(Color.BLACK)
                                setOnClickListener {
                                    showTaskDescription(task)
                                    isChecked = false
                                }
                            }

                            container.addView(checkbox)
                            taskContainer.addView(container)
                        }
                    }

                } else {
                    Toast.makeText(this@UpcomingActivity, "Gagal memuat task", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetTaskResponse>, t: Throwable) {
                Toast.makeText(this@UpcomingActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showTaskDescription(task: DashboardTask) {
        AlertDialog.Builder(this)
            .setTitle(task.title)
            .setMessage("Deskripsi: ${task.description}\n\nTanggal: ${task.due_date}\nWaktu: ${task.due_time}")
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun generateHorizontalCalendar(calendarRow: LinearLayout) {
        val calendar = Calendar.getInstance()
        for (i in 0 until 7) {
            val day = calendar.clone() as Calendar
            day.add(Calendar.DAY_OF_MONTH, i)

            val dayLabel = SimpleDateFormat("E", Locale.US).format(day.time)[0].toString()
            val dayNumber = day.get(Calendar.DAY_OF_MONTH)

            val textView = TextView(this).apply {
                text = "$dayLabel\n$dayNumber"
                textSize = 14f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                setBackgroundResource(R.drawable.date_circle_background)

                val params = LinearLayout.LayoutParams(140, 140)
                params.setMargins(12, 8, 12, 8)
                layoutParams = params
            }

            calendarRow.addView(textView)
        }
    }
}
