package org.thoughtcrime.securesms.components.settings.conversation.history.delay

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.components.settings.conversation.preferences.Utils.formatHistoryTrimDelay
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter

class MessageHistoryDelaySettingsFragment : DSLSettingsFragment(
  titleId = R.string.MessageHistorySettingsFragment__keep_messages,
  layoutId = R.layout.message_history_delay_settings_fragment
) {
  private lateinit var viewModel: MessageHistoryDelaySettingsViewModel

  override fun bindAdapter(adapter: MappingAdapter) {
    val provider = ViewModelProvider(
      NavHostFragment.findNavController(this).getViewModelStoreOwner(R.id.app_settings_message_history_delay),
      MessageHistoryDelaySettingsViewModel.Factory(requireContext(), arguments.toConfig())
    )
    viewModel = provider.get(MessageHistoryDelaySettingsViewModel::class.java)

    viewModel.state.observe(viewLifecycleOwner) { state ->
      adapter.submitList(getConfiguration(state).toMappingModelList())
    }
  }

  private fun onSelect(delay: Long) {
    if (viewModel.isShorterThanCurrentlySelected(delay)) {
      val actualDelay = viewModel.getComparableDelay(delay)
      val message = if (viewModel.isForRecipient()) {
        resources.getString(R.string.MessageHistoryDelaySettingsFragment__this_will_permanently_delete_message_history_and_media_in_this_chat, actualDelay.formatHistoryTrimDelay(requireContext()))
      } else {
        resources.getString(R.string.MessageHistoryDelaySettingsFragment__this_will_permanently_delete_all_message_history_and_media, actualDelay.formatHistoryTrimDelay(requireContext()))
      }
      MaterialAlertDialogBuilder(requireContext())
        .setMessage(message)
        .setPositiveButton(R.string.MessageHistorySettingsFragment__delete) { dialog, _ -> viewModel.select(delay); dialog.dismiss() }
        .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
        .show()
    } else {
      viewModel.select(delay)
    }
  }

  private fun onCustomizeClicked(customLength: Long?) {
    MaterialAlertDialogBuilder(requireContext())
      .setMessage("TODO")
      .setPositiveButton(R.string.MessageHistorySettingsFragment__delete) { _, _ ->  }
      .setNegativeButton(android.R.string.cancel) { _, _ -> }
      .show()
  }

  private fun getConfiguration(state: MessageHistoryDelaySettingsState): DSLConfiguration {
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
          summary = DSLSettingsText.from(state.universalDelay.formatHistoryTrimDelay(requireContext())),
          isChecked = state.delay == Recipient.HISTORY_TRIM_UNIVERSAL,
          onClick = { onSelect(Recipient.HISTORY_TRIM_UNIVERSAL) }
        )
        hasCustomValue = state.delay != Recipient.HISTORY_TRIM_UNIVERSAL
      }

      radioPref(
        title = DSLSettingsText.from(R.string.MessageHistoryDelaySettingsFragment__forever),
        isChecked = state.delay == Long.MAX_VALUE,
        onClick = { onSelect(Long.MAX_VALUE) }
      )
      hasCustomValue = hasCustomValue && state.delay != Long.MAX_VALUE

      val values: Array<Int> = resources.getIntArray(R.array.MessageHistoryDelaySettingsFragment__values).toTypedArray()
      val labels: Array<String> = resources.getStringArray(R.array.MessageHistoryDelaySettingsFragment__labels)

      labels.zip(values).forEach { (label, delaySeconds) ->
        val delayMillis = delaySeconds.toLong() * 1000L
        radioPref(
          title = DSLSettingsText.from(label),
          isChecked = state.delay == delayMillis,
          onClick = { onSelect(delayMillis) }
        )
        hasCustomValue = hasCustomValue && state.delay != delayMillis
      }

      radioPref(
        title = DSLSettingsText.from(R.string.MessageHistoryDelaySettingsFragment__custom_duration),
        summary = if (hasCustomValue) DSLSettingsText.from(state.delay.formatHistoryTrimDelay(requireContext())) else null,
        isChecked = hasCustomValue,
        onClick = { onCustomizeClicked(if (hasCustomValue) state.delay else null) }
      )
    }
  }
}

private fun Bundle?.toConfig(): MessageHistoryDelaySettingsViewModel.Config {
  if (this == null) {
    return MessageHistoryDelaySettingsViewModel.Config()
  }

  val safeArguments: MessageHistoryDelaySettingsFragmentArgs = MessageHistoryDelaySettingsFragmentArgs.fromBundle(this)
  return MessageHistoryDelaySettingsViewModel.Config(
    recipientId = safeArguments.recipientId
  )
}
