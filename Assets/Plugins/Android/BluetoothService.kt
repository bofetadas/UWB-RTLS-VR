package de.MaxBauer.UWBRTLSVR

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.app.Activity
import android.widget.Toast
import de.MaxBauer.UWBRTLSVR.Positioning
import de.MaxBauer.UWBRTLSVR.PositioningImpl
import java.util.*

private const val TAG_MAC = "F0:74:2F:98:DE:90"
private const val GET_LOCATION_CHARACTERISTIC = "003BBDF2-C634-4B3D-AB56-7EC889B89A37"
private const val SET_LOCATION_MODE_CHARACTERISTIC = "A02B947E-DF97-4516-996A-1882521E0EAD"
private const val DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb"
private const val POSITION_LOCATION_BYTE_ARRAY_SIZE = 14
private val POSITION_MODE = byteArrayOf(0x00)

class BluetoothService {

    private var context: Activity? = null
    private var tagConnection: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var tagIsConnected = false
    private lateinit var positioningImpl: Positioning

    fun setContext(context: Activity){
        println("Setting context")
        this.context = context
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        positioningImpl = PositioningImpl(context)
        positioningImpl.startIMU()
    }

    fun terminate(): Boolean{
        if (tagIsConnected){
            tagConnection?.disconnect()
            tagConnection?.close()
            tagConnection = null
            return true
        }
        return false
    }

    fun connectToTag(){
        val device = bluetoothAdapter!!.getRemoteDevice(TAG_MAC)
        if (device == null) {
            println("Device not found")
            connectToTag()
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        tagConnection = device.connectGatt(context, false, gattCallback)
        tagConnection?.discoverServices()
    }

    // Various callback methods defined by the BLE API.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    println("Connected to tag")
                    tagConnection = gatt
                    tagIsConnected = true
                    tagConnection!!.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    tagIsConnected = false
                    tagConnection = null
                    println("Disconnected")
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    println("Services discovered")
                    // Set location mode to 0 (Position only mode)
                    val setLocationModeCharacteristic = gatt.services[2].getCharacteristic(UUID.fromString(SET_LOCATION_MODE_CHARACTERISTIC))
                    setLocationModeCharacteristic.value = POSITION_MODE
                    val success = gatt.writeCharacteristic(setLocationModeCharacteristic)
                    if (!success){
                        println("Connection Establishment failed")
                    }
                }
                else -> {
                    // Unsuccessful service discovery
                    println("No services discovered")
                }
            }
        }

        // Check if the position mode set in 'onServicesDiscovered' was successful
        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int){
            if (characteristic?.uuid == UUID.fromString(SET_LOCATION_MODE_CHARACTERISTIC) && status == BluetoothGatt.GATT_SUCCESS){
                if (characteristic?.value!!.contentEquals(POSITION_MODE)) {
                    println("Connection successfully established")
                    showToast("Successfully connected to Tag!")
                    enableNotifications()
                }
            }
        }

        // Remote characteristic changes handling
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            val args = characteristic!!.value as ByteArray
            positioningImpl.calculateLocation(args)
        }
    }

    private fun showToast(message: String){
        println("Showing Toast message ...")
        context?.runOnUiThread{
            Toast.makeText(context!!, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableNotifications(){
        if (tagIsConnected){
            val characteristic = tagConnection!!.services[2].getCharacteristic(UUID.fromString(GET_LOCATION_CHARACTERISTIC))
            tagConnection?.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(UUID.fromString(DESCRIPTOR))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            tagConnection?.writeDescriptor(descriptor)
        }
        else{
            connectToTag()
            enableNotifications()
        }
    }

    fun disableNotifications(){
        if (tagIsConnected){
            val characteristic = tagConnection!!.services[2].getCharacteristic(UUID.fromString(GET_LOCATION_CHARACTERISTIC))
            tagConnection?.setCharacteristicNotification(characteristic, false)
            val descriptor = characteristic.getDescriptor(UUID.fromString(DESCRIPTOR))
            descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            tagConnection?.writeDescriptor(descriptor)
        }
    }
}