package net.mcbay.transmat.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CalloutPage(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var displayOrder: Long, // can't currently change display order in app, but planned
    var name: String?
)
