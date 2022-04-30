package net.mcbay.transmat.data

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mcbay.transmat.R
import net.mcbay.transmat.TransmatApplication

object DataInitializer {
    // Programmatically populate Vow of the Disciple callouts so the app
    // always has some callout list to use as a default.  Do this here with code rather than
    // using a pre-populated SQLite file so we can (eventually) take advantage of Android string
    // localization more easily to have the default labels be localized.
    fun populateIfEmpty(database: CalloutDatabase, ctx: Context) {
        val dbJob = CoroutineScope(Dispatchers.IO).launch {
            val dataDao = database.calloutDataDao()
            val data = dataDao.getPage(1)

            if (data.isEmpty()) {
                val calloutPage = CalloutPage(
                    name = ctx.getString(R.string.vow_of_the_disciple),
                    displayOrder = 1
                )
                val id = database.calloutPageDao().insertPage(calloutPage)
                var displayOrder: Long = 1
                val builtinList = getBuiltinList(ctx)

                for (builtin in builtinList) {
                    dataDao.insertData(
                        CalloutData(
                            pageId = id,
                            displayOrder = displayOrder++,
                            label = builtin.label,
                            callout = builtin.callout,
                            type = CalloutDisplayType.DRAWABLE,
                            data = builtin.drawableName
                        )
                    )
                }
            }
        }

        dbJob.invokeOnCompletion {
            LocalBroadcastManager.getInstance(ctx)
                .sendBroadcast(Intent(TransmatApplication.DATABASE_INITIALIZED))
        }
    }

    fun getBuiltinList(ctx: Context): Array<BuiltinData> {
        val resources = ctx.resources

        return arrayOf(
            BuiltinData(
                ctx.getString(R.string.next),
                ctx.getString(R.string.next_separator),
                resources.getResourceName(R.drawable.redacted)
            ),
            BuiltinData(
                ctx.getString(R.string.ascendant_plane),
                ctx.getString(R.string.ascendant_plane),
                resources.getResourceName(R.drawable.ascendant_plane)
            ),
            BuiltinData(
                ctx.getString(R.string.black_garden),
                ctx.getString(R.string.black_garden),
                resources.getResourceName(R.drawable.black_garden)
            ),
            BuiltinData(
                ctx.getString(R.string.black_heart),
                ctx.getString(R.string.black_heart),
                resources.getResourceName(R.drawable.black_heart)
            ),
            BuiltinData(
                ctx.getString(R.string.commune),
                ctx.getString(R.string.commune),
                resources.getResourceName(R.drawable.commune)
            ),
            BuiltinData(
                ctx.getString(R.string.darkness),
                ctx.getString(R.string.darkness),
                resources.getResourceName(R.drawable.darkness)
            ),
            BuiltinData(
                ctx.getString(R.string.drink),
                ctx.getString(R.string.drink),
                resources.getResourceName(R.drawable.drink)
            ),
            BuiltinData(
                ctx.getString(R.string.earth),
                ctx.getString(R.string.earth),
                resources.getResourceName(R.drawable.earth)
            ),
            BuiltinData(
                ctx.getString(R.string.enter),
                ctx.getString(R.string.enter),
                resources.getResourceName(R.drawable.enter)
            ),
            BuiltinData(
                ctx.getString(R.string.fleet),
                ctx.getString(R.string.fleet),
                resources.getResourceName(R.drawable.fleet)
            ),
            BuiltinData(
                ctx.getString(R.string.give),
                ctx.getString(R.string.give),
                resources.getResourceName(R.drawable.give)
            ),
            BuiltinData(
                ctx.getString(R.string.grieve),
                ctx.getString(R.string.grieve),
                resources.getResourceName(R.drawable.grieve)
            ),
            BuiltinData(
                ctx.getString(R.string.guardian),
                ctx.getString(R.string.guardian),
                resources.getResourceName(R.drawable.guardian)
            ),
            BuiltinData(
                ctx.getString(R.string.hive),
                ctx.getString(R.string.hive),
                resources.getResourceName(R.drawable.hive)
            ),
            BuiltinData(
                ctx.getString(R.string.kill),
                ctx.getString(R.string.kill),
                resources.getResourceName(R.drawable.kill)
            ),
            BuiltinData(
                ctx.getString(R.string.light),
                ctx.getString(R.string.light),
                resources.getResourceName(R.drawable.light)
            ),
            BuiltinData(
                ctx.getString(R.string.love),
                ctx.getString(R.string.love),
                resources.getResourceName(R.drawable.love)
            ),
            BuiltinData(
                ctx.getString(R.string.pyramid),
                ctx.getString(R.string.pyramid),
                resources.getResourceName(R.drawable.pyramid)
            ),
            BuiltinData(
                ctx.getString(R.string.remember),
                ctx.getString(R.string.remember),
                resources.getResourceName(R.drawable.remember)
            ),
            BuiltinData(
                ctx.getString(R.string.savathun),
                ctx.getString(R.string.savathun),
                resources.getResourceName(R.drawable.savathun)
            ),
            BuiltinData(
                ctx.getString(R.string.scorn),
                ctx.getString(R.string.scorn),
                resources.getResourceName(R.drawable.scorn)
            ),
            BuiltinData(
                ctx.getString(R.string.stop),
                ctx.getString(R.string.stop),
                resources.getResourceName(R.drawable.stop)
            ),
            BuiltinData(
                ctx.getString(R.string.tower),
                ctx.getString(R.string.tower),
                resources.getResourceName(R.drawable.tower)
            ),
            BuiltinData(
                ctx.getString(R.string.traveler),
                ctx.getString(R.string.traveler),
                resources.getResourceName(R.drawable.traveler)
            ),
            BuiltinData(
                ctx.getString(R.string.witness),
                ctx.getString(R.string.witness),
                resources.getResourceName(R.drawable.witness)
            ),
            BuiltinData(
                ctx.getString(R.string.worm),
                ctx.getString(R.string.worm),
                resources.getResourceName(R.drawable.worm)
            ),
            BuiltinData(
                ctx.getString(R.string.worship),
                ctx.getString(R.string.worship),
                resources.getResourceName(R.drawable.worship)
            )
        )
    }
}
