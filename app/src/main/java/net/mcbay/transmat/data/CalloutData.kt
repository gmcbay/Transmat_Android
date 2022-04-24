package net.mcbay.transmat.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CalloutData(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val pageId: Long,
    val displayOrder: Long, // can't currently change display order in app, but planned
    val label: String?,
    val callout: String?,
    val type: CalloutDisplayType,
    val data: String? // Used for Uri for bitmaps/color value for colors/drawable name for drawables
)
