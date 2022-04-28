package net.mcbay.transmat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CalloutPageDao {
    @Query("SELECT * FROM calloutpage ORDER BY id ASC")
    suspend fun getAll(): List<CalloutPage>

    @Query("SELECT COUNT(*) FROM calloutdata WHERE pageId = :pageId ORDER BY id ASC")
    suspend fun getCalloutCount(pageId: Long): Int

    @Query("SELECT * FROM calloutdata WHERE type = :type AND pageId = :pageId ORDER BY id ASC")
    suspend fun getCalloutsOfType(pageId: Long, type: CalloutDisplayType): List<CalloutData>

    @Query("SELECT name FROM calloutpage WHERE id = :pageId")
    suspend fun getName(pageId: Long): String

    @Query("UPDATE calloutpage SET name = :name WHERE id = :pageId")
    suspend fun setName(pageId: Long, name: String)

    @Insert
    suspend fun insertPage(calloutPage: CalloutPage): Long

    @Query("DELETE FROM calloutpage WHERE id = :pageId")
    suspend fun delete(pageId: Long)

    @Query("SELECT displayOrder FROM calloutpage ORDER BY displayOrder DESC LIMIT 1")
    suspend fun getMaxDisplayOrder(): Long?
}
