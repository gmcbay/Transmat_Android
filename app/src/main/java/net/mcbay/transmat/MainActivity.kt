package net.mcbay.transmat

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import net.mcbay.transmat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothUtil: BluetoothUtil
    private lateinit var scanningSnackbar: Snackbar
    private var actionMenu: Menu? = null

    private val requestMultiplePermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var count = 0

            permissions.entries.forEach { perm ->
                if (perm.value) {
                    count++
                }
            }

            if (count < 2) {
                bluetoothUtil.showBluetoothStatus(
                    binding.root,
                    getString(R.string.bluetooth_permissions_required)
                )
            } else {
                requestBluetoothEnable()
            }
        }

    private var requestBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (!bluetoothUtil.isDeviceConnected()) {
                bluetoothUtil.scanDevices(this, binding.root)
            }
        } else {
            bluetoothUtil.showBluetoothStatus(
                binding.root,
                getString(R.string.bluetooth_required)
            )
        }
    }

    fun getBluetoothUtil(): BluetoothUtil {
        return bluetoothUtil
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothUtil = BluetoothUtil()

        bluetoothUtil.onScanning = {
            invalidateOptionsMenu()

            if (!this::scanningSnackbar.isInitialized) {
                scanningSnackbar = Snackbar.make(binding.root, R.string.bluetooth_scanning,
                    Snackbar.LENGTH_INDEFINITE)
            }

            scanningSnackbar.show()
        }

        bluetoothUtil.onScanTimeout = {
            invalidateOptionsMenu()

            if (this::scanningSnackbar.isInitialized) {
                scanningSnackbar.dismiss()
            }

            val timeoutSnackbar = Snackbar.make(binding.root, R.string.bluetooth_timeout,
                Snackbar.LENGTH_LONG)
            timeoutSnackbar.show()

            setBluetoothIconDisconnected()
        }

        bluetoothUtil.onConnected = { address ->
            invalidateOptionsMenu()

            if (this::scanningSnackbar.isInitialized) {
                scanningSnackbar.dismiss()
            }

            setBluetoothIconConnected()

            bluetoothUtil.showBluetoothStatus(
                binding.root, getString(R.string.connected_to, address)
            )
        }

        bluetoothUtil.onDisconnected = {
            invalidateOptionsMenu()

            if (this::scanningSnackbar.isInitialized) {
                scanningSnackbar.dismiss()
            }

            setBluetoothIconDisconnected()

            bluetoothUtil.showBluetoothStatus(
                binding.root, getString(R.string.bluetooth_disconnected)
            )
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setBluetoothIconConnected() {

    }

    private fun setBluetoothIconDisconnected() {

    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun hideSystemUI() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        actionMenu = menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_bluetooth -> {
                toggleBluetooth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        requestBluetooth.launch(enableBtIntent)
    }

    private fun toggleBluetooth() {
        val bluetoothManager: BluetoothManager = getSystemService(
            BluetoothManager::class.java
        )

        if (bluetoothManager.adapter == null) {
            bluetoothUtil.showBluetoothStatus(
                binding.root,
                getString(R.string.bluetooth_unsupported)
            )
        } else {
            if (bluetoothManager.adapter?.isEnabled == false) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestMultiplePermissions.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        )
                    )
                } else {
                    requestBluetoothEnable()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestMultiplePermissions.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        )
                    )
                } else {
                    if (!bluetoothUtil.isDeviceConnected()) {
                        bluetoothUtil.scanDevices(this, binding.root)
                    } else {
                        bluetoothUtil.disconnect()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            BluetoothUtil.BLUETOOTH_SCAN_REQUEST -> {
                var allowed = true

                grantResults.forEach {
                    if (it == PackageManager.PERMISSION_DENIED) {
                        allowed = false
                    }
                }

                if (allowed) {
                    bluetoothUtil.startScan(this, binding.root)
                } else {
                    bluetoothUtil.showBluetoothStatus(
                        binding.root,
                        getString(R.string.bluetooth_permissions_required)
                    )
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (bluetoothUtil.isDeviceConnected()) {
            actionMenu?.getItem(0)?.icon = ContextCompat.getDrawable(
                this,
                R.drawable.ic_bluetooth_connected
            )
            CalloutItemViewModel.getInstance().setConnected(true)
        } else {
            actionMenu?.getItem(0)?.icon = ContextCompat.getDrawable(
                this,
                R.drawable.ic_bluetooth_disconnected
            )
            CalloutItemViewModel.getInstance().setConnected(false)
        }

        return super.onPrepareOptionsMenu(menu)
    }
}
