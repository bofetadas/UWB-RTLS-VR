package de.MaxBauer.UWBRTLSVR

interface Positioning {
    fun startIMU()
    fun stopIMU()
    fun calculateLocation(byteArray: ByteArray)
}