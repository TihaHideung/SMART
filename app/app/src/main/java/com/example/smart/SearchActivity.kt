package com.example.smart

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var editSearch: EditText
    private lateinit var btnSearch: TextView
    private lateinit var recentSearchContainer: LinearLayout
    private lateinit var recentVisitedContainer: LinearLayout
    private lateinit var clearRecent: TextView
    private lateinit var resultContainer: LinearLayout

    private val recentSearches = mutableListOf<String>()
    private val recentVisited = mutableListOf<String>()

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Ambil userId dari SharedPreferences
        userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "User ID tidak ditemukan, silakan login ulang", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        resultContainer = findViewById(R.id.resultContainer)
        editSearch = findViewById(R.id.editSearch)
        btnSearch = findViewById(R.id.btnSearch)
        recentSearchContainer = findViewById(R.id.recentSearchContainer)
        recentVisitedContainer = findViewById(R.id.recentVisitedContainer)
        clearRecent = findViewById(R.id.textClearRecent)
        val menuButton = findViewById<ImageButton>(R.id.menuButton)

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

        btnSearch.setOnClickListener {
            val query = editSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchTask(query)
                addRecentSearch(query)
                editSearch.text.clear()
            }
        }

        clearRecent.setOnClickListener {
            recentSearches.clear()
            recentSearchContainer.removeAllViews()
        }

        findViewById<LinearLayout>(R.id.navToday).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navUpcoming).setOnClickListener {
            startActivity(Intent(this, UpcomingActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navSearch).setOnClickListener {
            // Stay on this page
        }
    }

    private fun searchTask(query: String) {
        resultContainer.removeAllViews()

        RetrofitClient.instance.searchTask(query, userId)
            .enqueue(object : Callback<SearchResponse> {
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    resultContainer.removeAllViews()
                    Log.d("SearchDebug", "isSuccessful: ${response.isSuccessful}")
                    Log.d("SearchDebug", "HTTP Code: ${response.code()}")

                    if (response.isSuccessful && response.body()?.status == "success") {
                        val tasks = response.body()?.tasks ?: emptyList()
                        if (tasks.isEmpty()) {
                            val tv = TextView(this@SearchActivity).apply {
                                text = "Tidak ada hasil yang cocok"
                                textSize = 16f
                                setPadding(8, 8, 8, 8)
                            }
                            resultContainer.addView(tv)
                        } else {
                            for (task in tasks) {
                                val container = LinearLayout(this@SearchActivity).apply {
                                    orientation = LinearLayout.VERTICAL
                                    setPadding(32, 24, 32, 24)
                                    setBackgroundResource(R.drawable.card_background)
                                    val params = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    params.setMargins(0, 0, 0, 24)
                                    layoutParams = params
                                    isClickable = true
                                    isFocusable = true
                                    setOnClickListener { showTaskDetail(task) }
                                }

                                val title = TextView(this@SearchActivity).apply {
                                    text = task.title
                                    textSize = 18f
                                    setTextColor(resources.getColor(android.R.color.black))
                                    setTypeface(null, android.graphics.Typeface.BOLD)
                                }

                                val due = TextView(this@SearchActivity).apply {
                                    text = "${task.due_date} ${task.due_time}"
                                    textSize = 14f
                                    setTextColor(resources.getColor(android.R.color.darker_gray))
                                }

                                container.addView(title)
                                container.addView(due)
                                resultContainer.addView(container)
                            }
                        }
                    } else {
                        Toast.makeText(this@SearchActivity, "Error mengambil data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    Toast.makeText(this@SearchActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showTaskDetail(task: Task) {
        AlertDialog.Builder(this)
            .setTitle(task.title)
            .setMessage("Deskripsi:\n${task.description}\n\nTanggal: ${task.due_date}\nWaktu: ${task.due_time}")
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun addRecentSearch(query: String) {
        if (!recentSearches.contains(query)) {
            recentSearches.add(0, query)
            val textView = TextView(this).apply {
                text = query
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }
            recentSearchContainer.addView(textView, 0)
        }
    }

    private fun addVisitedPage(name: String) {
        if (!recentVisited.contains(name)) {
            recentVisited.add(0, name)
            val textView = TextView(this).apply {
                text = name
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }
            recentVisitedContainer.addView(textView, 0)
        }
    }
}
