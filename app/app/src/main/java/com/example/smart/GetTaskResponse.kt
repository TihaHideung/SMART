package com.example.smart

data class GetTaskResponse(
    val status: String,
    val tasks: List<DashboardTask>
)

data class DashboardTask(
    val id_task: Int,
    val title: String,
    val description: String,
    val due_date: String,
    val due_time: String
)

