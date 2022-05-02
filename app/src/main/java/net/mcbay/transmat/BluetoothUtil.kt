package net.mcbay.transmat

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import java.util.*

class BluetoothUtil {
    var onScanning: () -> Unit = {}
    var onScanTimeout: () -> Unit = {}
    var onConnected: (address: String) -> Unit = {}
    var onDisconnected: () -> Unit = {}

    private var stopScan: () -> Unit = {}
    private var connectedDevice: BluetoothGatt? = null
    private var uartTx: BluetoothGattCharacteristic? = null

    var scanTimeoutRunnable = Runnable {
        onScanTimeout()
        stopScan()
    }
    var scanTimeoutHandler: Handler = Handler(Looper.getMainLooper())

    companion object {
        const val BLUETOOTH_SCAN_REQUEST = 1
        // UUIDs used for UART service and the TX Characteristic on
        // Adafruit Bluefruit LE devices, as defined here:
        // https://learn.adafruit.com/introducing-adafruit-ble-bluetooth-low-energy-friend/uart-service
        val UART_UUID: ParcelUuid =
            ParcelUuid.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        val TX_UUID: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    }

    fun isDeviceConnected(): Boolean {
        return connectedDevice != null
    }

    fun scanDevices(activity: Activity, view: View) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                BLUETOOTH_SCAN_REQUEST
            )
        } else {
            startScan(activity, view)
        }
    }

    fun startScan(activity: Activity, view: View) {
        stopScan()

        val bluetoothManager: BluetoothManager = activity.getSystemService(
            BluetoothManager::class.java
        )

        val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                result.scanRecord?.let { scanRecord ->
                    scanRecord.serviceUuids?.forEach { uuid ->
                        if (uuid.equals(UART_UUID)) {
                            try {
                                bluetoothManager.adapter.bluetoothLeScanner.stopScan(this)

                                result.device.connectGatt(
                                    activity, false,
                                    object : BluetoothGattCallback() {
                                        override fun onConnectionStateChange(
                                            gatt: BluetoothGatt,
                                            status: Int,
                                            newState: Int
                                        ) {
                                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                                gatt.discoverServices()
                                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                                disconnect()
                                            }
                                        }

                                        override fun onServicesDiscovered(
                                            gatt: BluetoothGatt,
                                            status: Int
                                        ) {
                                            val uartService = gatt.getService(UART_UUID.uuid)

                                            uartTx = uartService?.getCharacteristic(TX_UUID)

                                            if (uartTx != null) {
                                                scanTimeoutHandler.removeCallbacks(
                                                    scanTimeoutRunnable
                                                )
                                                connectedDevice = gatt
                                                onConnected(gatt.device.address)
                                            }
                                        }
                                    })
                            } catch (ex: SecurityException) {
                                disconnect()
                                showBluetoothStatus(
                                    view,
                                    activity.getString(R.string.bluetooth_permissions_required)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (bluetoothManager.adapter != null) {
            try {
                disconnect()

                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()

                stopScan = {
                    bluetoothManager.adapter.bluetoothLeScanner.stopScan(scanCallback)
                }

                scanTimeoutHandler.removeCallbacks(scanTimeoutRunnable)
                scanTimeoutHandler.postDelayed(scanTimeoutRunnable, 20000)

                onScanning()

                bluetoothManager.adapter.bluetoothLeScanner.stopScan(scanCallback)
                bluetoothManager.adapter.bluetoothLeScanner.startScan(
                    null,
                    scanSettings,
                    scanCallback
                )
            } catch (ex: SecurityException) {
                disconnect()

                showBluetoothStatus(
                    view,
                    activity.getString(R.string.bluetooth_permissions_required)
                )
            }
        } else {
            showBluetoothStatus(view, activity.getString(R.string.bluetooth_required))
        }
    }

    fun send(message: String) {
        val dataByteArray: ByteArray = (message + "\n").toByteArray()

        uartTx?.value = dataByteArray

        try {
            connectedDevice?.writeCharacteristic(uartTx)
        } catch (ex: SecurityException) {
            disconnect()
        }
    }

    fun disconnect() {
        var wasConnected = false

        connectedDevice?.let {
            try {
                wasConnected = true
                it.disconnect()
            } catch (ex: SecurityException) {

            }
        }

        connectedDevice = null
        uartTx = null

        if (wasConnected) {
            onDisconnected()
        }
    }

    fun showBluetoothStatus(view: View, err: String) {
        val snackbar = Snackbar.make(
            view,
            err,
            Snackbar.LENGTH_SHORT
        )
        snackbar.show()
    }
}