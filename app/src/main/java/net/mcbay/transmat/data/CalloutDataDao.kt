package net.mcbay.transmat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CalloutDataDao {
    @Query("SELECT * FROM calloutdata WHERE pageId = :pageId  ORDER BY id ASC")
    suspend fun getPage(pageId: Long): List<CalloutData>

    @Insert
    suspend fun insertData(calloutData: CalloutData): Long
}