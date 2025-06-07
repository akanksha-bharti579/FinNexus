package com.akanksha.expensecalculator.data.model

data class HelpTopic(
    val title: String,
    val content: String,
    var isExpanded: Boolean = false
) 