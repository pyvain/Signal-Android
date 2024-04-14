package org.thoughtcrime.securesms.components.settings.conversation.history.delay

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.livedata.Store

class MessageHistoryDelaySettingsViewModel(val config: Config, private val repository: MessageHistoryDelaySettingsRepository) : ViewModel() {

  private val store = Store(MessageHistoryDelaySettingsState())
  private val recipientId: RecipientId? = config.recipientId

  val state: LiveData<MessageHistoryDelaySettingsState> = store.stateLiveData

  init {
    if (recipientId != null) {
      store.update(Recipient.live(recipientId).liveData) { r, s -> s.copy(
        delay = r.historyTrimDelay,
        universalDelay = repository.getUniversalHistoryDelay(),
        isForRecipient = true
      ) }
    } else {
      store.update { it.copy(delay = repository.getUniversalHistoryDelay()) }
    }
  }

  fun isShorterThanCurrentlySelected(newDelay: Long): Boolean {
    return getComparableDelay(newDelay) < getComparableDelay(store.state.delay)
  }

  fun getComparableDelay(delay: Long): Long {
    return if (delay == Recipient.HISTORY_TRIM_UNIVERSAL) store.state.universalDelay else delay
  }

  fun isForRecipient(): Boolean {
    return recipientId != null
  }

  fun select(delay: Long) {
    store.update { it.copy(delay = delay) }

    if (recipientId != null) {
      repository.setHistoryTrimDelay(recipientId, delay)
    } else {
      repository.setUniversalHistoryTrimDelay(delay)
    }
  }

  class Factory(context: Context, private val config: Config) : ViewModelProvider.Factory {
    val repository = MessageHistoryDelaySettingsRepository(context.applicationContext)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return requireNotNull(modelClass.cast(MessageHistoryDelaySettingsViewModel(config, repository)))
    }
  }

  data class Config(
    val recipientId: RecipientId? = null,
  )
}