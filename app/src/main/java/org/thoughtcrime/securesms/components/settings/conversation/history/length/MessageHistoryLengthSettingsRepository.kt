package org.thoughtcrime.securesms.components.settings.conversation.history.length

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.recipients.RecipientId

class MessageHistoryLengthSettingsRepository(val context: Context) {

  fun setHistoryTrimLength(recipientId: RecipientId, newLength: Long) {
    SignalExecutors.BOUNDED.execute {
      SignalDatabase.recipients.setHistoryTrimLength(recipientId, newLength)
    }
  }

  fun setUniversalHistoryTrimLength(newLength: Long) {
    SignalExecutors.BOUNDED.execute {
      SignalStore.settings().universalHistoryTrimLength = newLength
    }
  }

  fun getUniversalHistoryLength(): Long {
    return SignalStore.settings().universalHistoryTrimLength
  }
}