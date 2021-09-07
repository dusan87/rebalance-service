package com.vanguard.commons

object CsvReader {
    fun readWithoutHeader(fileName: String): List<String> = Thread
        .currentThread()
        .contextClassLoader
        .getResourceAsStream(fileName)!!
        .bufferedReader()
        .readLines()
        .filterIndexed { index, _ -> index != 0 }
}
