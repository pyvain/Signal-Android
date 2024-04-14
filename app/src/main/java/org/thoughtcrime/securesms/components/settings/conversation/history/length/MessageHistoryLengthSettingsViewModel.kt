package org.thoughtcrime.securesms.components.settings.conversation.history.length

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.livedata.Store

class MessageHistoryLengthSettingsViewModel(val config: Config, private val repository: MessageHistoryLengthSettingsRepository) : ViewModel() {

  private val store = Store(MessageHistoryLengthSettingsState())
  private val recipientId: RecipientId? = config.recipientId

  val state: LiveData<MessageHistoryLengthSettingsState> = store.stateLiveData

  init {
    if (recipientId != null) {
      store.update(Recipient.live(recipientId).liveData) { r, s -> s.copy(
        length = r.historyTrimLength,
        universalLength = repository.getUniversalHistoryLength(),
        isForRecipient = true
      ) }
    } else {
      store.update { it.copy(length = repository.getUniversalHistoryLength()) }
    }
  }

  fun isShorterThanCurrentlySelected(newLength: Long): Boolean {
    return getComparableLength(newLength) < getComparableLength(store.state.length)
  }

  fun getComparableLength(length: Long): Long {
    return if (length == Recipient.HISTORY_TRIM_UNIVERSAL) store.state.universalLength else length
  }

  fun isForRecipient(): Boolean {
    return recipientId != null
  }

  fun select(length: Long) {
    store.update { it.copy(length = length) }

    if (recipientId != null) {
      repository.setHistoryTrimLength(recipientId, length)
    } else {
      repository.setUniversalHistoryTrimLength(length)
    }
  }

  class Factory(context: Context, private val config: Config) : ViewModelProvider.Factory {
    val repository = MessageHistoryLengthSettingsRepository(context.applicationContext)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return requireNotNull(modelClass.cast(MessageHistoryLengthSettingsViewModel(config, repository)))
    }
  }

  data class Config(
    val recipientId: RecipientId? = null,
  )
}
