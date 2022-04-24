package net.mcbay.transmat.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CalloutPage::class, CalloutData::class], version = 1)
abstract class CalloutDatabase : RoomDatabase() {
    abstract fun calloutPageDao(): CalloutPageDao
    abstract fun calloutDataDao(): CalloutDataDao
}
