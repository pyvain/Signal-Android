package org.thoughtcrime.securesms.components.settings.conversation.history.length

import org.thoughtcrime.securesms.recipients.Recipient

data class MessageHistoryLengthSettingsState(
  val length: Long = Recipient.HISTORY_TRIM_UNIVERSAL,
  val universalLength: Long = Long.MAX_VALUE,
  val isForRecipient: Boolean = false
)