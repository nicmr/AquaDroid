package io.github.z3r0c00l_2k.aquadroid.recievers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.media.RingtoneManager
import io.github.z3r0c00l_2k.aquadroid.R
import io.github.z3r0c00l_2k.aquadroid.helpers.NotificationHelper
import io.github.z3r0c00l_2k.aquadroid.utils.SharedPrefKeys

class NotifierReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val prefs = context.getSharedPreferences(SharedPrefKeys.USERS_SHARED_PREF, MODE_PRIVATE)
        val notificationsTone = prefs.getString(
            SharedPrefKeys.NOTIFICATION_TONE_URI_KEY, RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            ).toString()
        )

        val title = context.resources.getString(R.string.app_name)
        val messageToShow = prefs.getString(
            SharedPrefKeys.NOTIFICATION_MSG_KEY,
            context.resources.getString(R.string.pref_notification_message_value)
        )

        /* Notify */
        val nHelper = NotificationHelper(context)
        @SuppressLint("ResourceType") val nBuilder = messageToShow?.let {
            nHelper
                .getNotification(title, it, notificationsTone)
        }
        nHelper.notify(1, nBuilder)

    }
}