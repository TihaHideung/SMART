package com.example.smart

data class SearchResponse(
    val status: String,
    val tasks: List<Task>
)

data class Task(
    val id_task: Int,
    val title: String,
    val description: String,
    val due_date: String,
    val due_time: String
)



