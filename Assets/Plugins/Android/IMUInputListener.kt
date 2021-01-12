package de.MaxBauer.UWBRTLSVR

interface IMUInputListener {
    fun onAccelerometerUpdate(values: FloatArray)
    fun onGravitySensorUpdate(values: FloatArray)
    fun onMagnetometerUpdate(values: FloatArray)
}