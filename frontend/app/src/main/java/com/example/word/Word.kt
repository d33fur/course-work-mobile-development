package com.example.word

import java.io.Serializable

data class Word(val id: Int, val word: String, val about: String) : Serializable
