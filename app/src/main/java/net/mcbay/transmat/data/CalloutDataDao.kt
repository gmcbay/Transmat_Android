package net.mcbay.transmat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CalloutDataDao {
    @Query("SELECT * FROM calloutdata WHERE pageId = :pageId  ORDER BY id ASC")
    suspend fun getPage(pageId: Long): List<CalloutData>

    @Query("SELECT * FROM calloutdata WHERE id = :calloutId  ORDER BY id ASC")
    suspend fun get(calloutId: Long): CalloutData?

    @Query("UPDATE calloutdata SET label = :label WHERE id = :calloutId")
    suspend fun setLabel(calloutId: Long, label: String)

    @Query("UPDATE calloutdata SET data = :data WHERE id = :calloutId")
    suspend fun setData(calloutId: Long, data: String)

    @Query("UPDATE calloutdata SET type = :type WHERE id = :calloutId")
    suspend fun setType(calloutId: Long, type: CalloutDisplayType)

    @Query("UPDATE calloutdata SET callout = :callout WHERE id = :calloutId")
    suspend fun setCallout(calloutId: Long, callout: String)

    @Insert
    suspend fun insertData(calloutData: CalloutData): Long

    @Query("DELETE FROM calloutdata WHERE id = :calloutId")
    suspend fun delete(calloutId: Long)

    @Query("SELECT displayOrder FROM calloutdata WHERE pageId = :pageId ORDER BY displayOrder DESC LIMIT 1")
    suspend fun getMaxDisplayOrder(pageId: Long): Long?
}
