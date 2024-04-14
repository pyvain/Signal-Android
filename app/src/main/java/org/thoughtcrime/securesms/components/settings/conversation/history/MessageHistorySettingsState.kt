package org.thoughtcrime.securesms.components.settings.conversation.history

import org.thoughtcrime.securesms.recipients.Recipient

data class MessageHistorySettingsState(
  val delay: Long = Recipient.HISTORY_TRIM_UNIVERSAL,
  val length: Long = Recipient.HISTORY_TRIM_UNIVERSAL
)