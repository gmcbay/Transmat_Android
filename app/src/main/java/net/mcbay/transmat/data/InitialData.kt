package net.mcbay.transmat.data

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mcbay.transmat.R

object InitialData {
    const val DATABASE_INITIALIZED = "DATABASE_INITIALIZED"

    // Programmatically populate Vow of the Disciple callouts so the app
    // always has some callout list to use as a default.  Do this here with code rather than
    // using a pre-populated sqllite file so we can (eventually) take advantage of Android string
    // localization more easily to have the default labels be localized.
    fun populateIfEmpty(database: CalloutDatabase, ctx: Context) {
        val dbJob = CoroutineScope(Dispatchers.IO).launch {
            val data = database.calloutDataDao().getPage(1)

            if (data.isEmpty()) {
                val calloutPage = CalloutPage(name = ctx.getString(R.string.vow_of_the_disciple),
                    displayOrder = 1)
                val id = database.calloutPageDao().insertPage(calloutPage)
                val dataDao = database.calloutDataDao()
                var displayOrder: Long = 1

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.next),
                        callout = ctx.getString(R.string.next_separator),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.next)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.ascendant_plane),
                        callout = ctx.getString(R.string.ascendant_plane),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.ascendant_plane)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.black_garden),
                        callout = ctx.getString(R.string.black_garden),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.black_garden)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.black_heart),
                        callout = ctx.getString(R.string.black_heart),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.black_heart)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.commune),
                        callout = ctx.getString(R.string.commune),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.commune)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.darkness),
                        callout = ctx.getString(R.string.darkness),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.darkness)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.drink),
                        callout = ctx.getString(R.string.drink),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.drink)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.earth),
                        callout = ctx.getString(R.string.earth),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.earth)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.enter),
                        callout = ctx.getString(R.string.enter),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.enter)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.fleet),
                        callout = ctx.getString(R.string.fleet),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.fleet)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.give),
                        callout = ctx.getString(R.string.give),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.give)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.grieve),
                        callout = ctx.getString(R.string.grieve),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.grieve)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.guardian),
                        callout = ctx.getString(R.string.guardian),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.guardian)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.hive),
                        callout = ctx.getString(R.string.hive),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.hive)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.kill),
                        callout = ctx.getString(R.string.kill),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.kill)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.knowledge),
                        callout = ctx.getString(R.string.knowledge),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.knowledge)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.light),
                        callout = ctx.getString(R.string.light),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.light)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.love),
                        callout = ctx.getString(R.string.love),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.love)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.pyramid),
                        callout = ctx.getString(R.string.pyramid),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.pyramid)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.savathun),
                        callout = ctx.getString(R.string.savathun),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.savathun)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.scorn),
                        callout = ctx.getString(R.string.scorn),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.scorn)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.stop),
                        callout = ctx.getString(R.string.stop),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.stop)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.tower),
                        callout = ctx.getString(R.string.tower),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.tower)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.traveler),
                        callout = ctx.getString(R.string.traveler),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.traveler)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.witness),
                        callout = ctx.getString(R.string.witness),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.witness)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder++,
                        label = ctx.getString(R.string.worm),
                        callout = ctx.getString(R.string.worm),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.worm)
                    )
                )

                dataDao.insertData(
                    CalloutData(
                        pageId = id,
                        displayOrder = displayOrder,
                        label = ctx.getString(R.string.worship),
                        callout = ctx.getString(R.string.worship),
                        type = CalloutDisplayType.DRAWABLE,
                        data = ctx.resources.getResourceName(R.drawable.worship)
                    )
                )
            }
        }

        dbJob.invokeOnCompletion {
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(Intent(DATABASE_INITIALIZED))
        }
    }
}
