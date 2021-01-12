package de.MaxBauer.UWBRTLSVR

import android.content.Context
import kotlin.system.exitProcess
import com.unity3d.player.UnityPlayer;

private const val POSITION_BYTE_ARRAY_SIZE = 14

// Entry class for handling positioning logic
class PositioningImpl(context: Context): Positioning, KalmanFilterOutputListener {

    private val converter = ByteArrayToLocationDataConverter()
    private val imu = IMU(context)
    private val kalmanFilterImpl = KalmanFilterImpl(this)
    private val kalmanFilterImplStrategies = KalmanFilterImplStrategies()
    private var kalmanFilterImplStrategy: (uwbLocationData: LocationData, accelerationData: AccelerationData, orientationData: OrientationData) -> Unit = kalmanFilterImplStrategies.configureStrategy
    private val recordingImpl: Recording = RecordingImpl(context)
    private var previousCompassDirection: Directions? = null
    private var applyCameraRotationDriftOffset = false

    override fun startIMU() {
        if (!recordingImpl.createRecordingMovementFile()){
            exitProcess(1)
        }
        imu.start()
    }

    override fun stopIMU() {
        imu.stop()
    }

    override fun calculateLocation(byteArray: ByteArray) {
        if (byteArray.size != POSITION_BYTE_ARRAY_SIZE) return
        val uwbLocation = converter.getLocationFromByteArray(byteArray)
        val imuData = imu.getIMUData()
        val imuAcceleration = imuData.accelerationData
        val imuOrientation = imuData.orientationData
        val currentCompassDirection = CompassUtil.getCompassDirection(imuOrientation.yaw)
        if (currentCompassDirection != previousCompassDirection && currentCompassDirection != null){
            applyCameraRotationDriftOffset = true
            previousCompassDirection = currentCompassDirection
        }
        kalmanFilterImplStrategy.invoke(uwbLocation, imuAcceleration, imuOrientation)
    }

    // Kalman Filter callback
    override fun onNewStateVectorEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData, rawAccelerationData: AccelerationData, filteredAccelerationData: AccelerationData) {
        // To enable recording of position and accelerometer data, uncomment the line below
        recordingImpl.writeToFile("$uwbLocationData | $filteredLocationData | $rawAccelerationData | $filteredAccelerationData")
        val message = buildMessage(filteredLocationData)
        UnityPlayer.UnitySendMessage("BluetoothLE", "onMessageReceived", message)
    }

    private fun buildMessage(filteredLocationData: LocationData): String {
        var message = "$filteredLocationData"
        if (applyCameraRotationDriftOffset){
            message += ", $previousCompassDirection"
            applyCameraRotationDriftOffset = false
        }
        return message
    }

    private inner class KalmanFilterImplStrategies {
        val configureStrategy: (uwbLocationData: LocationData, accData: AccelerationData, orientationData: OrientationData) -> Unit = { uwbLocationData, _, _ ->
            kalmanFilterImpl.configure(uwbLocationData)
            kalmanFilterImplStrategy = estimateStrategy
        }

        val estimateStrategy: (uwbLocationData: LocationData, accelerationData: AccelerationData, orientationData: OrientationData) -> Unit = { uwbLocationData, accelerationData, orientationData ->
            kalmanFilterImpl.predict(accelerationData)
            kalmanFilterImpl.update(uwbLocationData, accelerationData, orientationData)
        }
    }
}