package de.MaxBauer.UWBRTLSVR

interface KalmanFilterOutputListener {
    fun onNewStateVectorEstimate(uwbLocationData: LocationData, locationData: LocationData, rawAccelerationData: AccelerationData, filteredAccelerationData: AccelerationData)
}