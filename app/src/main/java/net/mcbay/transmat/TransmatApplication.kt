package net.mcbay.transmat

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import net.mcbay.transmat.data.CalloutData
import net.mcbay.transmat.data.CalloutDatabase
import net.mcbay.transmat.data.CalloutDisplayType
import net.mcbay.transmat.data.DataInitializer
import java.io.File
import java.lang.Exception
import java.lang.NumberFormatException

class TransmatApplication : Application() {
    companion object {
        const val DATABASE_INITIALIZED = "DATABASE_INITIALIZED"
        const val DATABASE_NAME = "callouts.db"
        const val DEFAULT_PAGE_ID = 1L
        lateinit var INSTANCE: TransmatApplication
    }

    private lateinit var bluetoothUtil: BluetoothUtil
    private lateinit var database: CalloutDatabase
    private var currentActivity: AppCompatActivity? = null

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        bluetoothUtil = BluetoothUtil()

        database = Room.databaseBuilder(
            applicationContext,
            CalloutDatabase::class.java, DATABASE_NAME
        ).build()

        DataInitializer.populateIfEmpty(database, this)
    }

    fun setCurrentActivity(activity: AppCompatActivity) {
        currentActivity = activity
    }

    fun getCurrentActivity(): AppCompatActivity? {
        return currentActivity
    }

    fun getBluetoothUtil(): BluetoothUtil {
        return bluetoothUtil
    }

    fun getDatabase(): CalloutDatabase {
        return database
    }
}

// Extension function to draw callout icon image based on current type
// to avoid having to duplicate this code in various different adapters
fun AppCompatImageView.drawFrom(data: CalloutData): Int {
    var currentColor = ContextCompat.getColor(context, R.color.tm_primary)

    when (data.type) {
        CalloutDisplayType.COLOR -> {
            try {
                currentColor = data.data?.toInt() ?: 0
            } catch (nfe: NumberFormatException) {

            }

            setImageDrawable(null)
            setBackgroundColor(currentColor)
        }
        CalloutDisplayType.BITMAP -> {
            setBackgroundColor(0)
            data.data?.let {
                Picasso.get().load(File(it)).into(this, object : Callback {
                    override fun onSuccess() {

                    }

                    override fun onError(e: Exception?) {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.tm_primary))
                    }

                })
            }
        }
        CalloutDisplayType.DRAWABLE -> {
            setBackgroundColor(0)
            context?.let { ctx ->
                setImageDrawable(
                    AppCompatResources.getDrawable(
                        ctx,
                        ctx.resources.getIdentifier(
                            data.data,
                            "drawable", ctx.packageName
                        )
                    )
                )
            }
        }
    }

    return currentColor
}
