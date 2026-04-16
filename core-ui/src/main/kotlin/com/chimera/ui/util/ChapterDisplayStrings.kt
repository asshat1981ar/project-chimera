package com.chimera.ui.util

/**
 * Maps internal [chapterTag] values to human-readable chapter display strings.
 */
object ChapterDisplayStrings {
    fun tagToTitle(tag: String): String = when (tag.lowercase()) {
        "prologue"                     -> "Prologue — The Hollow Threshold"
        "act1", "act_1"               -> "Act I — The Hollow"
        "act2", "act_2", "ashen"      -> "Act II — The Ashen Reaches"
        "act3", "act_3", "coast"      -> "Act III — The Shattered Coast"
        "epilogue"                     -> "Epilogue"
        else -> tag.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    fun tagToShort(tag: String): String = when (tag.lowercase()) {
        "prologue"                     -> "Prologue"
        "act1", "act_1"               -> "Act I"
        "act2", "act_2", "ashen"      -> "Act II"
        "act3", "act_3", "coast"      -> "Act III"
        "epilogue"                     -> "Epilogue"
        else -> tag.replaceFirstChar { it.uppercase() }
    }
}
