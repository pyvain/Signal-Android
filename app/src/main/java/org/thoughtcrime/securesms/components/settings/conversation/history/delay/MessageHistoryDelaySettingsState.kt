package org.thoughtcrime.securesms.components.settings.conversation.history.delay

import org.thoughtcrime.securesms.recipients.Recipient

data class MessageHistoryDelaySettingsState(
  val delay: Long = Recipient.HISTORY_TRIM_UNIVERSAL,
  val universalDelay: Long = Long.MAX_VALUE,
  val isForRecipient: Boolean = false
)