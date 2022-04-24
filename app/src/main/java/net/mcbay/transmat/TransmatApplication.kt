package net.mcbay.transmat

import android.app.Application
import androidx.room.Room
import net.mcbay.transmat.data.CalloutDatabase
import net.mcbay.transmat.data.InitialData

class TransmatApplication : Application() {
    companion object {
        const val DATABASE_NAME = "callouts.db"
        const val DEFAULT_PAGE_ID = 1L
        lateinit var INSTANCE: TransmatApplication
    }

    private lateinit var bluetoothUtil: BluetoothUtil
    private lateinit var database: CalloutDatabase

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        bluetoothUtil = BluetoothUtil()

        database = Room.databaseBuilder(
            applicationContext,
            CalloutDatabase::class.java, DATABASE_NAME
        ).build()

        InitialData.populateIfEmpty(database, this)
    }

    fun getBluetoothUtil(): BluetoothUtil {
        return bluetoothUtil
    }

    fun getDatabase(): CalloutDatabase {
        return database
    }
}
