package com.chimera.ui.util

/**
 * Maps internal [chapterTag] values to human-readable chapter display strings.
 */
object ChapterDisplayStrings {
    fun tagToTitle(tag: String): String = when (tag.lowercase()) {
        "prologue"                     -> "Prologue — The Hollow Threshold"
        "hollow_approach_complete"      -> "Act II — The Ashen Reaches"
        "the_ashen_transition", "ashen_transition" -> "Act III — The Shattered Coast"
        "act1", "act_1"               -> "Act I — The Hollow"
        "act2", "act_2", "ashen"      -> "Act II — The Ashen Reaches"
        "act3", "act_3", "coast"      -> "Act III — The Shattered Coast"
        "epilogue"                     -> "Epilogue"
        else -> tag.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    fun tagToShort(tag: String): String = when (tag.lowercase()) {
        "prologue"                     -> "Prologue"
        "hollow_approach_complete"      -> "Act II"
        "the_ashen_transition", "ashen_transition" -> "Act III"
        "act1", "act_1"               -> "Act I"
        "act2", "act_2", "ashen"      -> "Act II"
        "act3", "act_3", "coast"      -> "Act III"
        "epilogue"                     -> "Epilogue"
        else -> tag.replaceFirstChar { it.uppercase() }
    }

    /** Uppercase label shown above the act title on the interstitial screen (e.g. "ACT II"). */
    fun tagToActLabel(tag: String): String = when (tag.lowercase()) {
        "prologue"                     -> "PROLOGUE"
        "hollow_approach_complete"     -> "ACT II"
        "the_ashen_transition", "ashen_transition" -> "ACT III"
        "act1", "act_1"               -> "ACT I"
        "act2", "act_2", "ashen"      -> "ACT II"
        "act3", "act_3", "coast"      -> "ACT III"
        "epilogue"                     -> "EPILOGUE"
        else -> tag.uppercase().replace("_", " ")
    }

    /** Flavour quote shown on the act-transition interstitial. */
    fun tagToQuote(tag: String): String = when (tag.lowercase()) {
        "hollow_approach_complete" ->
            "\"The Hollow opens before you. Every shadow is a door.\nThe Warden watches from the threshold.\""
        "the_ashen_transition", "ashen_transition" ->
            "\"The Ashen Reaches breathe with the memory of fire.\nWhat was broken here chose to remain.\""
        "act1", "act_1"    ->
            "\"The Hollow remembers every wound.\nWalk carefully — it has not forgotten yours.\""
        "act2", "act_2", "ashen" ->
            "\"The Hollow does not remember the living.\nOnly the ash remembers.\""
        "act3", "act_3", "coast" ->
            "\"At the Shattered Coast the sea and the ruin speak the same language.\nLearn it before the tide does.\""
        "epilogue"         ->
            "\"What survives the Hollow survives everything.\nWhat the Hollow keeps, it keeps forever.\""
        else               -> "\"The path forward is the only path.\""
    }

    /** Attribution line beneath the quote. Empty string if no attribution. */
    fun tagToQuoteSource(tag: String): String = when (tag.lowercase()) {
        "hollow_approach_complete"      -> "Whispered at the Hollow's Edge"
        "the_ashen_transition", "ashen_transition" -> "Scorched stone, Ashen Reaches"
        "act1", "act_1"              -> "Etched at the Hollow Gate"
        "act2", "act_2", "ashen"     -> "Inscription at the Gate of Embers"
        "act3", "act_3", "coast"     -> "Tide-worn stone, Shattered Coast"
        "epilogue"                   -> "The Archivist's Final Entry"
        else                         -> ""
    }
}
