package com.example.ui.components

import java.util.Locale
import java.util.regex.Pattern

/**
 * Нормализатор названий футбольных клубов:
 * - декодирует HTML-сущности (&#231; -> ç, &#248; -> ø, &#225; -> á, &amp; и т.д.)
 * - убирает служебные приставки (FC, CF, FK, SK, GNK, PFC, NK, ФК)
 * - приводит к lowercase ASCII-подобному ключу для матчинга алиасов
 */
object TeamNameNormalizer {

    private val htmlNumericPattern = Pattern.compile("&#(\\d+);")

    fun unescapeHtml(raw: String): String {
        var text = raw
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace("&eacute;", "é")
            .replace("&ccedil;", "ç")
            .replace("&oslash;", "ø")
            .replace("&uuml;", "ü")
            .replace("&aacute;", "á")

        val matcher = htmlNumericPattern.matcher(text)
        val sb = StringBuffer()
        while (matcher.find()) {
            val codePoint = matcher.group(1)?.toIntOrNull()
            if (codePoint != null) {
                matcher.appendReplacement(sb, codePoint.toChar().toString())
            }
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    fun clean(raw: String): String {
        val unescaped = unescapeHtml(raw)
        return unescaped.lowercase(Locale.ROOT)
            .replace("ø", "o")
            .replace("ç", "c")
            .replace("ü", "u")
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ö", "o")
            .replace("ä", "a")
            .replace("ñ", "n")
            .replace("š", "s")
            .replace("č", "c")
            .replace("ž", "z")
            .replace("đ", "d")
            .replace(Regex("""\b(fc|cf|fk|sk|gnk|afc|bsc|pfc|nk|фк)\b"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
