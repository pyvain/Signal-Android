package org.thoughtcrime.securesms.components.settings.conversation.history.length

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.signal.core.util.StringUtil
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.components.settings.conversation.preferences.Utils.formatHistoryTrimLength
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter

class MessageHistoryLengthSettingsFragment : DSLSettingsFragment(
  titleId = R.string.MessageHistorySettingsFragment__conversation_length_limit,
  layoutId = R.layout.message_history_length_settings_fragment
) {
  private lateinit var viewModel: MessageHistoryLengthSettingsViewModel

  override fun bindAdapter(adapter: MappingAdapter) {
    val provider = ViewModelProvider(
      NavHostFragment.findNavController(this).getViewModelStoreOwner(R.id.app_settings_message_history_length),
      MessageHistoryLengthSettingsViewModel.Factory(requireContext(), arguments.toConfig())
    )
    viewModel = provider.get(MessageHistoryLengthSettingsViewModel::class.java)

    viewModel.state.observe(viewLifecycleOwner) { state ->
      adapter.submitList(getConfiguration(state).toMappingModelList())
    }
  }

  private fun onSelect(length: Long) {
    if (viewModel.isShorterThanCurrentlySelected(length)) {
      val actualLength = viewModel.getComparableLength(length)
      val actualLengthAsInt = if (actualLength > Int.MAX_VALUE.toLong()) Int.MAX_VALUE else actualLength.toInt()
      val message = if (viewModel.isForRecipient()) {
        resources.getQuantityString(R.plurals.MessageHistoryLengthSettingsFragment__this_will_permanently_trim_this_chat_to_the_d_most_recent_messages, actualLengthAsInt, actualLength)
      } else {
        resources.getQuantityString(R.plurals.MessageHistoryLengthSettingsFragment__this_will_permanently_trim_all_chats_to_the_d_most_recent_messages, actualLengthAsInt, actualLength)
      }
      MaterialAlertDialogBuilder(requireContext())
        .setMessage(message)
        .setPositiveButton(R.string.MessageHistorySettingsFragment__delete) { _, _ -> viewModel.select(length) }
        .setNegativeButton(android.R.string.cancel) { _, _ ->  }
        .show()
    } else {
      viewModel.select(length)
    }
  }

  private fun onCustomizeClicked(customLength: Long?) {
    val dialogView: View = layoutInflater.inflate(R.layout.customizable_setting_edit_text, null, false)
    val selector: EditText = dialogView.findViewById(R.id.customizable_setting_edit_text)

    if (customLength != null) {
      selector.setText(customLength.toString())
    }

    val dialog = MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.MessageHistoryLengthSettingsFragment__custom_length)
      .setView(dialogView)
      .setPositiveButton(R.string.MessageHistorySettingsFragment__set) { _, _ ->
        onSelect(selector.text.toString().toLong())
      }
      .setNegativeButton(android.R.string.cancel, null)
      .create()

    dialog.setOnShowListener { _: DialogInterface? ->
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(selector.getText()))
      selector.requestFocus()
      selector.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(sequence: Editable) {
          val trimmed = StringUtil.trimSequence(sequence)
          if (TextUtils.isEmpty(trimmed)) {
            sequence.replace(0, sequence.length, "")
          } else {
            try {
              trimmed.toString().toInt()
              dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true)
              return
            } catch (e: java.lang.NumberFormatException) {
              val onlyDigits = trimmed.toString().replace("\\D".toRegex(), "")
              if (onlyDigits != trimmed.toString()) {
                sequence.replace(0, sequence.length, onlyDigits)
              }
            }
          }
          dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false)
        }

        override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {}
      })

      selector.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
          val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
          if (positiveButton.isEnabled) {
            positiveButton.performClick()
            return@OnKeyListener true
          }
        }
        false
      })
    }

    dialog.show()
  }

  private fun getConfiguration(state: MessageHistoryLengthSettingsState): DSLConfiguration {
    return configure {
      textPref(
        summary = DSLSettingsText.from(
          if (state.isForRecipient) {
            R.string.MessageHistorySettingsFragment__description_chat
          } else {
            R.string.MessageHistorySettingsFragment__description_universal
          }
        )
      )

      var hasCustomValue = true

      if (state.isForRecipient) {
        radioPref(
          title = DSLSettingsText.from(R.string.MessageHistorySettingsFragment__use_universal_setting),
          summary = DSLSettingsText.from(state.universalLength.formatHistoryTrimLength(requireContext())),
          isChecked = state.length == Recipient.HISTORY_TRIM_UNIVERSAL,
          onClick = { onSelect(Recipient.HISTORY_TRIM_UNIVERSAL) }
        )
        hasCustomValue = state.length != Recipient.HISTORY_TRIM_UNIVERSAL
      }

      radioPref(
        title = DSLSettingsText.from(R.string.MessageHistoryLengthSettingsFragment__unlimited),
        isChecked = state.length == Long.MAX_VALUE,
        onClick = { onSelect(Long.MAX_VALUE) }
      )
      hasCustomValue = hasCustomValue && state.length != Long.MAX_VALUE

      val values: Array<Int> = resources.getIntArray(R.array.MessageHistoryLengthSettingsFragment__values).toTypedArray()
      val labels: Array<String> = values.map { resources.getQuantityString(R.plurals.MessageHistoryLengthSettingsFragment__s_messages_plural, it, it) }.toTypedArray()

      labels.zip(values).forEach { (label, lengthInt) ->
        val length = lengthInt.toLong()
        radioPref(
          title = DSLSettingsText.from(label),
          isChecked = state.length == length,
          onClick = { onSelect(length) }
        )
        hasCustomValue = hasCustomValue && state.length != length
      }

      radioPref(
        title = DSLSettingsText.from(R.string.MessageHistoryLengthSettingsFragment__custom_length),
        summary = if (hasCustomValue) DSLSettingsText.from(resources.getQuantityString(R.plurals.MessageHistoryLengthSettingsFragment__s_messages_plural, if (state.length > Int.MAX_VALUE.toLong()) Int.MAX_VALUE else state.length.toInt(), state.length)) else null,
        isChecked = hasCustomValue,
        onClick = { onCustomizeClicked(if (hasCustomValue) state.length else null) }
      )
    }
  }
}

private fun Bundle?.toConfig(): MessageHistoryLengthSettingsViewModel.Config {
  if (this == null) {
    return MessageHistoryLengthSettingsViewModel.Config()
  }

  val safeArguments: MessageHistoryLengthSettingsFragmentArgs = MessageHistoryLengthSettingsFragmentArgs.fromBundle(this)
  return MessageHistoryLengthSettingsViewModel.Config(
    recipientId = safeArguments.recipientId
  )
}
