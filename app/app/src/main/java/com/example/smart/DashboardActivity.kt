package com.example.smart

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var taskList: LinearLayout
    private lateinit var calendarRow: LinearLayout
    private lateinit var addTaskOverlay: FrameLayout
    private lateinit var formAddTask: LinearLayout

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("MMMM d, yyyy (EEEE)", Locale("id"))
    private val calendarFormat = SimpleDateFormat("d\nMMM", Locale("id"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("USER_ID", -1)
        Log.d("DEBUG_USER_ID", "User ID dari SharedPreferences: $userId")

        if (userId == -1) {
            Toast.makeText(this, "Gagal mendapatkan ID pengguna. Silakan login ulang.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        formAddTask = findViewById(R.id.formAddTask)
        addTaskOverlay = findViewById(R.id.addTaskOverlay)
        val btnDate = findViewById<Button>(R.id.btnDate)
        val btnTime = findViewById<Button>(R.id.btnTime)
        val btnReminder = findViewById<Button>(R.id.btnReminder)
        val btnConfirmAdd = findViewById<Button>(R.id.btnConfirmAdd)
        val editTaskName = findViewById<EditText>(R.id.editTaskName)
        val editDescription = findViewById<EditText>(R.id.editDescription)
        val textDate = findViewById<TextView>(R.id.textDate)
        textDate.text = displayFormat.format(Date())

        taskList = findViewById(R.id.taskList)
        calendarRow = findViewById(R.id.calendarRow)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)

        findViewById<LinearLayout>(R.id.navToday).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navUpcoming).setOnClickListener {
            startActivity(Intent(this, UpcomingActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }

        btnAddTask.setOnClickListener {
            addTaskOverlay.visibility = View.VISIBLE
            formAddTask.visibility = View.VISIBLE
        }

        addTaskOverlay.setOnClickListener {
            addTaskOverlay.visibility = View.GONE
            formAddTask.visibility = View.GONE

            // Reset isi form
            findViewById<EditText>(R.id.editTaskName).text.clear()
            findViewById<EditText>(R.id.editDescription).text.clear()
            findViewById<Button>(R.id.btnDate).text = "Today"
            findViewById<Button>(R.id.btnTime).text = "Time"
        }


        formAddTask.setOnClickListener {
            // Supaya klik dalam form tidak menutup overlay
        }

        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                btnDate.text = "$day/${month + 1}/$year"
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            datePicker.datePicker.minDate = c.timeInMillis // Tidak bisa pilih tanggal sebelum hari ini
            datePicker.show()
        }


        btnTime.setOnClickListener {
            val c = Calendar.getInstance()
            val now = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }

                // Cek apakah tanggal adalah hari ini
                val selectedDateText = btnDate.text.toString()
                val isToday = try {
                    val parts = selectedDateText.split("/")
                    val selectedDate = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, parts[0].toInt())
                        set(Calendar.MONTH, parts[1].toInt() - 1)
                        set(Calendar.YEAR, parts[2].toInt())
                    }
                    selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            selectedDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                } catch (e: Exception) {
                    false
                }

                if (isToday && selected.before(now)) {
                    Toast.makeText(this, "Waktu tidak boleh di masa lalu", Toast.LENGTH_SHORT).show()
                } else {
                    btnTime.text = String.format("%02d:%02d", hour, minute)
                }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        btnReminder.setOnClickListener {
            Toast.makeText(this, "Reminder clicked!", Toast.LENGTH_SHORT).show()
        }

        btnConfirmAdd.setOnClickListener {
            val taskName = editTaskName.text.toString().trim()
            if (taskName.isNotEmpty()) {
                val rawDate = btnDate.text.toString()
                val dateParts = rawDate.split("/")
                val date = if (dateParts.size == 3) "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}" else dateFormat.format(Date())
                val time = btnTime.text.toString()

                if (userId <= 0) {
                    Toast.makeText(this, "ID pengguna tidak valid, silakan login ulang", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                RetrofitClient.instance.addTask(taskName, editDescription.text.toString(), date, time, userId)
                    .enqueue(object : Callback<DefaultResponse> {
                        override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                            if (response.isSuccessful) {
                                Log.d("ADD_TASK", "Response: ${response.body()?.message}")


                                logUserActivity("Menambahkan task: $taskName ($date $time)")

                                loadTasks()
                                addTaskOverlay.visibility = View.GONE
                                formAddTask.visibility = View.GONE
                                editTaskName.text.clear()
                                editDescription.text.clear()
                                btnDate.text = "Today"
                                btnTime.text = "Time"
                                logUserActivity("Menambahkan task: $taskName ($date $time)")
                            } else {
                                Log.e("ADD_TASK", "Gagal! Code: ${response.code()} - ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                            Log.e("ADD_TASK", "Failure: ${t.message}")
                        }
                    })
            }
        }

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

        loadTasks()
    }

    private fun loadTasks() {
        taskList.removeAllViews()
        calendarRow.removeAllViews()

        RetrofitClient.instance.getTasks(userId).enqueue(object : Callback<GetTaskResponse> {
            override fun onResponse(call: Call<GetTaskResponse>, response: Response<GetTaskResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val tasks = response.body()?.tasks ?: emptyList()
                    val groupedTasks = tasks.groupBy { it.due_date }

                    for ((dateStr, tasksOnDate) in groupedTasks) {
                        val date = dateFormat.parse(dateStr)
                        val displayDate = displayFormat.format(date!!)

                        val dateHeader = TextView(this@DashboardActivity).apply {
                            text = "$displayDate • ${tasksOnDate.size} task(s)"
                            setTextColor(Color.BLACK)
                            textSize = 16f
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, 24, 0, 8)
                        }
                        taskList.addView(dateHeader)

                        val calendarLabel = calendarFormat.format(date)
                        val calendarTextView = TextView(this@DashboardActivity).apply {
                            text = calendarLabel
                            textSize = 12f
                            setTextColor(Color.WHITE)
                            gravity = Gravity.CENTER
                            setTypeface(null, Typeface.BOLD)
                            setBackgroundResource(R.drawable.date_circle_background)

                            val params = LinearLayout.LayoutParams(120, 120)
                            params.setMargins(12, 8, 12, 8)
                            layoutParams = params

                            // Set tag agar bisa tahu tanggal yang dipilih
                            tag = dateStr

                            // Saat diklik, filter task berdasarkan tanggal ini
                            setOnClickListener {
                                val selectedDate = tag as String
                                filterTasksByDate(selectedDate)
                            }
                        }




                        calendarRow.addView(calendarTextView)

                        for (task in tasksOnDate) {
                            val container = LinearLayout(this@DashboardActivity).apply {
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

                            val view = LayoutInflater.from(this@DashboardActivity).inflate(R.layout.checkbox_task, container, false)
                            val checkBox = view.findViewById<CheckBox>(R.id.taskCheckBox)
                            val editBtn = view.findViewById<ImageView>(R.id.editIcon)

                            checkBox.text = "${task.title} • ${task.due_time}"
                            checkBox.tag = task.id_task

                            checkBox.setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) {
                                    AlertDialog.Builder(this@DashboardActivity)
                                        .setTitle("Konfirmasi")
                                        .setMessage("Task akan hilang jika sudah dicentang. Apakah kamu yakin ingin menyelesaikannya?")
                                        .setPositiveButton("Ya") { _, _ ->
                                            container.animate().alpha(0f).setDuration(500).withEndAction {
                                                taskList.removeView(container)
                                            }
                                            RetrofitClient.instance.deleteTask(task.id_task).enqueue(object : Callback<DefaultResponse> {
                                                override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                                                    Toast.makeText(this@DashboardActivity, "Task dihapus", Toast.LENGTH_SHORT).show()
                                                    logUserActivity("Menyelesaikan task: ${task.title} (${task.due_date} ${task.due_time})")
                                                    loadTasks()
                                                }

                                                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                                                    Toast.makeText(this@DashboardActivity, "Gagal hapus: ${t.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                        }
                                        .setNegativeButton("Batal") { _, _ ->
                                            checkBox.isChecked = false
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                            }

                            editBtn.setOnClickListener {
                                showEditDialog(task)
                            }

                            container.addView(view)
                            taskList.addView(container)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetTaskResponse>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Gagal ambil task: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditDialog(task: DashboardTask) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_task, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.editTaskTitle)
        val editDesc = dialogView.findViewById<EditText>(R.id.editTaskDescription)
        val btnEditDate = dialogView.findViewById<Button>(R.id.btnEditDate)
        val btnEditTime = dialogView.findViewById<Button>(R.id.btnEditTime)

        editTitle.setText(task.title)
        editDesc.setText(task.description)

        // Inisialisasi tanggal dan waktu dari task
        val currentDate = task.due_date // Format: yyyy-MM-dd
        val currentTime = task.due_time // Format: HH:mm
        btnEditDate.text = currentDate
        btnEditTime.text = currentTime

        val selectedDate = Calendar.getInstance()
        val selectedTime = Calendar.getInstance()

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            selectedDate.time = sdf.parse(currentDate) ?: Date()
        } catch (e: Exception) {}

        try {
            val timeParts = currentTime.split(":")
            if (timeParts.size == 2) {
                selectedTime.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                selectedTime.set(Calendar.MINUTE, timeParts[1].toInt())
            }
        } catch (e: Exception) {}

        // Date picker
        btnEditDate.setOnClickListener {
            val y = selectedDate.get(Calendar.YEAR)
            val m = selectedDate.get(Calendar.MONTH)
            val d = selectedDate.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                selectedDate.set(year, month, day)
                btnEditDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)
            }, y, m, d)
            datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
            datePicker.show()
        }

        // Time picker
        btnEditTime.setOnClickListener {
            val h = selectedTime.get(Calendar.HOUR_OF_DAY)
            val min = selectedTime.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, hour, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedTime.set(Calendar.MINUTE, minute)
                btnEditTime.text = String.format("%02d:%02d", hour, minute)
            }, h, min, true).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newTitle = editTitle.text.toString()
                val newDesc = editDesc.text.toString()
                val newDate = btnEditDate.text.toString()
                val newTime = btnEditTime.text.toString()

                RetrofitClient.instance.updateTask(
                    task.id_task, newTitle, newDesc, newDate, newTime
                ).enqueue(object : Callback<DefaultResponse> {
                    override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            Toast.makeText(this@DashboardActivity, "Task diperbarui", Toast.LENGTH_SHORT).show()
                            logUserActivity("Mengedit task: ${task.title} → $newTitle ($newDate $newTime)")
                            loadTasks()
                        } else {
                            Toast.makeText(this@DashboardActivity, "Gagal memperbarui task", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                        Toast.makeText(this@DashboardActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun logUserActivity(activity: String) {
        RetrofitClient.instance.logActivity(userId, activity)
            .enqueue(object : Callback<DefaultResponse> {
                override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                    Log.d("ActivityLog", "Log terkirim: $activity")
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    Log.e("ActivityLog", "Gagal kirim log: ${t.message}")
                }
            })
    }
    private fun filterTasksByDate(selectedDate: String) {
        taskList.removeAllViews()

        RetrofitClient.instance.getTasks(userId).enqueue(object : Callback<GetTaskResponse> {
            override fun onResponse(call: Call<GetTaskResponse>, response: Response<GetTaskResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val allTasks = response.body()?.tasks ?: emptyList()
                    val tasksForSelectedDate = allTasks.filter { it.due_date == selectedDate }

                    val date = dateFormat.parse(selectedDate)
                    val displayDate = displayFormat.format(date!!)

                    val dateHeader = TextView(this@DashboardActivity).apply {
                        text = "$displayDate • ${tasksForSelectedDate.size} task(s)"
                        setTextColor(Color.BLACK)
                        textSize = 16f
                        setTypeface(null, Typeface.BOLD)
                        setPadding(0, 24, 0, 8)
                    }
                    taskList.addView(dateHeader)

                    for (task in tasksForSelectedDate) {
                        val container = LinearLayout(this@DashboardActivity).apply {
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

                        val view = LayoutInflater.from(this@DashboardActivity)
                            .inflate(R.layout.checkbox_task, container, false)
                        val checkBox = view.findViewById<CheckBox>(R.id.taskCheckBox)
                        val editBtn = view.findViewById<ImageView>(R.id.editIcon)

                        checkBox.text = "${task.title} • ${task.due_time}"
                        checkBox.tag = task.id_task

                        container.addView(view)
                        taskList.addView(container)
                    }
                }
            }

            override fun onFailure(call: Call<GetTaskResponse>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Gagal filter task: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}