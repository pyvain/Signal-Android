package org.thoughtcrime.securesms.migrations

import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.keyvalue.SignalStore
import java.util.concurrent.TimeUnit

internal class MessageHistoryPerConvMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(parameters) {

  companion object {
    const val KEY = "MessageHistoryPerConv"

    val TAG = Log.tag(MessageHistoryPerConvMigrationJob::class.java)
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    if (SignalStore.settings().isTrimByLengthEnabled) {
      SignalStore.settings().universalHistoryTrimLength = SignalStore.settings().threadTrimLength.toLong()
    }

    val delay = when (SignalStore.settings().keepMessagesDurationId) {
      1 -> TimeUnit.DAYS.toMillis(365)
      2 -> TimeUnit.DAYS.toMillis(183)
      3 -> TimeUnit.DAYS.toMillis(30)
      else -> Long.MAX_VALUE
    }
    if (delay != Long.MAX_VALUE) {
      SignalStore.settings().universalHistoryTrimDelay = delay;
    }

    SignalStore.settings().clearOldTrimSettings()
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<MessageHistoryPerConvMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): MessageHistoryPerConvMigrationJob {
      return MessageHistoryPerConvMigrationJob(parameters)
    }
  }
}