package com.example.model

enum class Tournament(val title: String, val shortName: String, val hexColor: Long) {
    CHAMPIONS_LEAGUE("Лига Чемпионов УЕФА", "UCL", 0xFF00D4FF),
    EUROPA_LEAGUE("Лига Европы УЕФА", "UEL", 0xFFFFA000)
}
