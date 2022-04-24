package net.mcbay.transmat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CalloutPageDao {
    @Query("SELECT * FROM calloutpage ORDER BY id ASC")
    suspend fun getAll(): List<CalloutPage>

    @Query("SELECT name FROM calloutpage WHERE id = :pageId")
    suspend fun getName(pageId: Long): String

    @Query("UPDATE calloutpage SET name = :name WHERE id = :pageId")
    suspend fun setName(pageId: Long, name: String)

    @Insert
    suspend fun insertPage(calloutPage: CalloutPage): Long

    @Query("DELETE FROM calloutpage WHERE id = :pageId")
    suspend fun delete(pageId: Long)

    @Query("SELECT id FROM calloutpage ORDER BY id DESC LIMIT 1")
    suspend fun getMaxId(): Long

    @Query("SELECT displayOrder FROM calloutpage ORDER BY displayOrder DESC LIMIT 1")
    suspend fun getMaxDisplayOrder(): Long
}