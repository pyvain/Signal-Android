package org.thoughtcrime.securesms.components.settings.conversation.preferences

import android.content.Context
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.DateUtils
import java.util.Locale

object Utils {

  fun Long.formatMutedUntil(context: Context): String {
    return if (this == Long.MAX_VALUE) {
      context.getString(R.string.ConversationSettingsFragment__conversation_muted_forever)
    } else {
      context.getString(
        R.string.ConversationSettingsFragment__conversation_muted_until_s,
        DateUtils.getTimeString(context, Locale.getDefault(), this)
      )
    }
  }

  fun Long.formatHistoryTrimDelay(context: Context): String {
    return if (this == Recipient.HISTORY_TRIM_UNIVERSAL) {
      val universalDelay = SignalStore.settings().universalHistoryTrimDelay
      context.getString(R.string.MessageHistorySettingsFragment__universal, universalDelay.formatHistoryTrimDelay(context))
    } else if (this == Long.MAX_VALUE) {
      context.getString(R.string.MessageHistoryDelaySettingsFragment__forever)
    } else {
      val values: Array<Int> = context.resources.getIntArray(R.array.MessageHistoryDelaySettingsFragment__values).toTypedArray()
      val labels: Array<String> = context.resources.getStringArray(R.array.MessageHistoryDelaySettingsFragment__labels)
      labels.zip(values).forEach { (label, delaySeconds) ->
        val delayMillis = delaySeconds.toLong() * 1000L
        if (this == delayMillis) {
          return label
        }
      }
      "${this} milliseconds" // TODO
    }
  }

  fun Long.formatHistoryTrimLength(context: Context): String {
    return if (this == Recipient.HISTORY_TRIM_UNIVERSAL) {
      val universalLength = SignalStore.settings().universalHistoryTrimLength
      context.getString(R.string.MessageHistorySettingsFragment__universal, universalLength.formatHistoryTrimLength(context))
    } else if (this == Long.MAX_VALUE) {
      context.getString(R.string.MessageHistoryLengthSettingsFragment__unlimited)
    } else {
      val quantityAsInt: Int = if (this > Int.MAX_VALUE.toLong()) Int.MAX_VALUE else this.toInt()
      context.resources.getQuantityString(R.plurals.MessageHistoryLengthSettingsFragment__s_messages_plural, quantityAsInt, this)
    }
  }
}
