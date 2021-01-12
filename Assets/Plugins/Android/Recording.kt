package de.MaxBauer.UWBRTLSVR

interface Recording {
    fun createRecordingMovementFile(): Boolean
    fun writeToFile(line: String)
}