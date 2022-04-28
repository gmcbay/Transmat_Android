package net.mcbay.transmat.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CalloutData(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var pageId: Long,
    var displayOrder: Long, // can't currently change display order in app, but planned
    var label: String?,
    var callout: String?,
    var type: CalloutDisplayType,
    var data: String? // Used for Uri for bitmaps/color value for colors/drawable name for drawables
)
