package net.mcbay.transmat

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.kbeanie.multipicker.api.entity.ChosenAudio
import com.kbeanie.multipicker.api.entity.ChosenFile
import com.kbeanie.multipicker.api.entity.ChosenImage
import com.kbeanie.multipicker.api.entity.ChosenVideo
import com.noelchew.multipickerwrapper.library.MultiPickerWrapper.PickerUtilListener
import com.noelchew.multipickerwrapper.library.ui.MultiPickerWrapperAppCompatActivity
import com.yalantis.ucrop.UCrop
import net.mcbay.transmat.databinding.ActivityMainBinding
import net.mcbay.transmat.fragments.DataFragment


class MainActivity : MultiPickerWrapperAppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var scanningSnackbar: Snackbar
    private var onImagePathReturned:(path: String) -> Unit = { }

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
                TransmatApplication.INSTANCE.getBluetoothUtil().showBluetoothStatus(
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
            if (!TransmatApplication.INSTANCE.getBluetoothUtil().isDeviceConnected()) {
                TransmatApplication.INSTANCE.getBluetoothUtil().scanDevices(this, binding.root)
            }
        } else {
            TransmatApplication.INSTANCE.getBluetoothUtil().showBluetoothStatus(
                binding.root,
                getString(R.string.bluetooth_required)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransmatApplication.INSTANCE.getBluetoothUtil().onScanning = {
            invalidateOptionsMenu()

            if (!this::scanningSnackbar.isInitialized) {
                scanningSnackbar = Snackbar.make(
                    binding.root, R.string.bluetooth_scanning,
                    Snackbar.LENGTH_INDEFINITE
                )
            }

            scanningSnackbar.show()
        }

        TransmatApplication.INSTANCE.getBluetoothUtil().onScanTimeout = {
            invalidateOptionsMenu()

            if (this::scanningSnackbar.isInitialized) {
                scanningSnackbar.dismiss()
            }

            val timeoutSnackbar = Snackbar.make(
                binding.root, R.string.bluetooth_timeout,
                Snackbar.LENGTH_LONG
            )
            timeoutSnackbar.show()
        }

        TransmatApplication.INSTANCE.getBluetoothUtil().onConnected = { address ->
            invalidateOptionsMenu()

            if (this::scanningSnackbar.isInitialized) {
                scanningSnackbar.dismiss()
            }

            TransmatApplication.INSTANCE.getBluetoothUtil().showBluetoothStatus(
                binding.root, getString(R.string.connected_to, address)
            )
        }

        TransmatApplication.INSTANCE.getBluetoothUtil().onDisconnected = {
            invalidateOptionsMenu()

            if (this::scanningSnackbar.isInitialized) {
                scanningSnackbar.dismiss()
            }

            TransmatApplication.INSTANCE.getBluetoothUtil().showBluetoothStatus(
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

    override fun onResume() {
        super.onResume()
        TransmatApplication.INSTANCE.setCurrentActivity(this)
        hideSystemUI()
    }

    fun hideSystemUI() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_app, menu)
        return true
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

    fun toggleBluetooth() {
        val bluetoothUtil = TransmatApplication.INSTANCE.getBluetoothUtil()

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

                val bluetoothUtil = TransmatApplication.INSTANCE.getBluetoothUtil()

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

    fun onDatabaseUpdate(scrollToEnd: Boolean = false) {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_content_main
        )

        navHostFragment?.let { navHostFrag ->
            val fragment = navHostFrag.childFragmentManager.fragments[0]

            fragment?.let { dataFrag ->
                (dataFrag as DataFragment).updateView(scrollToEnd)
            }
        }
    }

    private var pickerWrapperListener: PickerUtilListener = object : PickerUtilListener {
        override fun onPermissionDenied() {
            Snackbar.make(
                binding.root,
                getString(R.string.storage_access_permission_denied),
                Snackbar.LENGTH_LONG
            ).show()
        }

        override fun onImagesChosen(list: List<ChosenImage>) {
            onImagePathReturned(list[0].originalPath)
        }

        override fun onVideosChosen(list: List<ChosenVideo>) {

        }

        override fun onAudiosChosen(list: MutableList<ChosenAudio>?) {

        }

        override fun onFilesChosen(list: MutableList<ChosenFile>?) {

        }

        override fun onError(s: String) {
            Snackbar.make(binding.root, s, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun getMultiPickerWrapperListener(): PickerUtilListener {
        return pickerWrapperListener
    }

    fun pickCroppedImage(onComplete:(path: String) -> Unit) {
        this.onImagePathReturned = onComplete
        val options = UCrop.Options()
        options.setToolbarTitle(getString(R.string.choose_image_crop))
        val tmPrimaryColor = ContextCompat.getColor(this, R.color.tm_primary)
        options.setToolbarColor(tmPrimaryColor)
        options.setStatusBarColor(tmPrimaryColor)
        options.setActiveWidgetColor(tmPrimaryColor)
        multiPickerWrapper.getPermissionAndPickSingleImageAndCrop(options, 1f, 1f)
    }
}
