package org.thoughtcrime.securesms.components.settings.conversation.history.delay

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.recipients.RecipientId

class MessageHistoryDelaySettingsRepository(val context: Context) {

  fun setHistoryTrimDelay(recipientId: RecipientId, newDelay: Long) {
    SignalExecutors.BOUNDED.execute {
      SignalDatabase.recipients.setHistoryTrimDelay(recipientId, newDelay)
    }
  }

  fun setUniversalHistoryTrimDelay(newDelay: Long) {
    SignalExecutors.BOUNDED.execute {
      SignalStore.settings().universalHistoryTrimDelay = newDelay
    }
  }

  fun getUniversalHistoryDelay(): Long {
    return SignalStore.settings().universalHistoryTrimDelay
  }
}