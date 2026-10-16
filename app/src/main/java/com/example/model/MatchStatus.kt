package com.example.model

enum class MatchStatus(val label: String) {
    SCHEDULED("Предстоит"),
    TODAY("Сегодня"),
    LIVE("Идёт сейчас"),
    FINISHED("Завершён")
}
