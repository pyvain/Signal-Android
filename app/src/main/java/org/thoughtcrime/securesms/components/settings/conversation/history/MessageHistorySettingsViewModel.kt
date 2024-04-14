package org.thoughtcrime.securesms.components.settings.conversation.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.livedata.Store

class MessageHistorySettingsViewModel(
  val recipientId: RecipientId?,
) : ViewModel() {

  private val store = Store(MessageHistorySettingsState())
  val state: LiveData<MessageHistorySettingsState> = store.stateLiveData

  init {
    if (recipientId != null) {
      store.update(Recipient.live(recipientId).liveData) { recipient, state ->
        state.copy(
          delay = recipient.historyTrimDelay,
          length = recipient.historyTrimLength
        )
      }
    } else {
      refresh()
    }
  }

  fun refresh() {
    if (recipientId == null) {
      store.update {
        it.copy(
          delay = SignalStore.settings().universalHistoryTrimDelay,
          length = SignalStore.settings().universalHistoryTrimLength
        )
      }
    }
  }

  class Factory(
    private val recipientId: RecipientId?,
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return requireNotNull(modelClass.cast(MessageHistorySettingsViewModel(recipientId)))
    }
  }
}
