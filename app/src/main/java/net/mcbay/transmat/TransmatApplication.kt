package net.mcbay.transmat

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import net.mcbay.transmat.data.CalloutDatabase
import net.mcbay.transmat.data.DataInitializer

class TransmatApplication : Application() {
    companion object {
        const val DATABASE_INITIALIZED = "DATABASE_INITIALIZED"
        const val DATABASE_NAME = "callouts.db"
        const val DEFAULT_PAGE_ID = 1L
        lateinit var INSTANCE: TransmatApplication
    }

    private lateinit var bluetoothUtil: BluetoothUtil
    private lateinit var database: CalloutDatabase
    private var currentActivity: AppCompatActivity? = null

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        bluetoothUtil = BluetoothUtil()

        database = Room.databaseBuilder(
            applicationContext,
            CalloutDatabase::class.java, DATABASE_NAME
        ).build()

        DataInitializer.populateIfEmpty(database, this)
    }

    fun setCurrentActivity(activity: AppCompatActivity) {
        currentActivity = activity
    }

    fun getCurrentActivity(): AppCompatActivity? {
        return currentActivity
    }

    fun getBluetoothUtil(): BluetoothUtil {
        return bluetoothUtil
    }

    fun getDatabase(): CalloutDatabase {
        return database
    }
}
