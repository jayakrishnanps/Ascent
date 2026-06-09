package com.yourapp.productivity.utils

fun String.formatEnumName(): String {
    return this.split("_")
        .joinToString(" ") { word -> 
            word.lowercase().replaceFirstChar { char -> char.uppercase() }
        }
}
