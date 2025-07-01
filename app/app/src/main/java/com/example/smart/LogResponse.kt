package com.example.smart

data class LogResponse(
    val status: String,
    val logs: List<LogData>
)

data class LogData(
    val id_log: Int,
    val activity: String,
    val timestamp: String
)

