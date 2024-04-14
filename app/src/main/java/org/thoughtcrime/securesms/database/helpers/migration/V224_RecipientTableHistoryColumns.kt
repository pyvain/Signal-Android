package org.thoughtcrime.securesms.database.helpers.migration

import android.app.Application
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.thoughtcrime.securesms.recipients.Recipient

@Suppress("ClassName")
object V224_RecipientTableHistoryColumns : SignalDatabaseMigration {
  override fun migrate(context: Application, db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("ALTER TABLE recipient ADD COLUMN history_trim_delay INTEGER DEFAULT ${Recipient.HISTORY_TRIM_UNIVERSAL}")
    db.execSQL("ALTER TABLE recipient ADD COLUMN history_trim_length INTEGER DEFAULT ${Recipient.HISTORY_TRIM_UNIVERSAL}")
  }
}
