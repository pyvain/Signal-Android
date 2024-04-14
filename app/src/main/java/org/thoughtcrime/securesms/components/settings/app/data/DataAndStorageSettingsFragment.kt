package org.thoughtcrime.securesms.components.settings.app.data

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.signal.core.util.concurrent.SignalExecutors
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.components.settings.conversation.preferences.Utils.formatHistoryTrimDelay
import org.thoughtcrime.securesms.components.settings.conversation.preferences.Utils.formatHistoryTrimLength
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.mms.SentMediaQuality
import org.thoughtcrime.securesms.util.Util
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.navigation.safeNavigate
import org.thoughtcrime.securesms.webrtc.CallDataMode
import kotlin.math.abs

class DataAndStorageSettingsFragment : DSLSettingsFragment(R.string.preferences__data_and_storage) {

  private val autoDownloadValues by lazy { resources.getStringArray(R.array.pref_media_download_entries) }
  private val autoDownloadLabels by lazy { resources.getStringArray(R.array.pref_media_download_values) }

  private val sentMediaQualityLabels by lazy { SentMediaQuality.getLabels(requireContext()) }

  private val callDataModeLabels by lazy { resources.getStringArray(R.array.pref_data_and_storage_call_data_mode_values) }

  private lateinit var viewModel: DataAndStorageSettingsViewModel

  override fun onResume() {
    super.onResume()
    viewModel.refresh()
  }

  override fun bindAdapter(adapter: MappingAdapter) {
    val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
    val repository = DataAndStorageSettingsRepository()
    val factory = DataAndStorageSettingsViewModel.Factory(preferences, repository)
    viewModel = ViewModelProvider(this, factory)[DataAndStorageSettingsViewModel::class.java]

    viewModel.state.observe(viewLifecycleOwner) {
      adapter.submitList(getConfiguration(it).toMappingModelList())
    }
  }

  fun getConfiguration(state: DataAndStorageSettingsState): DSLConfiguration {
    return configure {
      clickPref(
        title = DSLSettingsText.from(R.string.preferences_data_and_storage__manage_storage),
        summary = DSLSettingsText.from(Util.getPrettyFileSize(state.totalStorageUse)),
        onClick = {
          Navigation.findNavController(requireView()).safeNavigate(R.id.action_dataAndStorageSettingsFragment_to_storagePreferenceFragment)
        }
      )

      dividerPref()

      sectionHeaderPref(R.string.preferences_chats__media_auto_download)

      multiSelectPref(
        title = DSLSettingsText.from(R.string.preferences_chats__when_using_mobile_data),
        listItems = autoDownloadLabels,
        selected = autoDownloadValues.map { state.mobileAutoDownloadValues.contains(it) }.toBooleanArray(),
        onSelected = {
          val resultSet = it.mapIndexed { index, selected -> if (selected) autoDownloadValues[index] else null }.filterNotNull().toSet()
          viewModel.setMobileAutoDownloadValues(resultSet)
        }
      )

      multiSelectPref(
        title = DSLSettingsText.from(R.string.preferences_chats__when_using_wifi),
        listItems = autoDownloadLabels,
        selected = autoDownloadValues.map { state.wifiAutoDownloadValues.contains(it) }.toBooleanArray(),
        onSelected = {
          val resultSet = it.mapIndexed { index, selected -> if (selected) autoDownloadValues[index] else null }.filterNotNull().toSet()
          viewModel.setWifiAutoDownloadValues(resultSet)
        }
      )

      multiSelectPref(
        title = DSLSettingsText.from(R.string.preferences_chats__when_roaming),
        listItems = autoDownloadLabels,
        selected = autoDownloadValues.map { state.roamingAutoDownloadValues.contains(it) }.toBooleanArray(),
        onSelected = {
          val resultSet = it.mapIndexed { index, selected -> if (selected) autoDownloadValues[index] else null }.filterNotNull().toSet()
          viewModel.setRoamingAutoDownloadValues(resultSet)
        }
      )

      dividerPref()

      sectionHeaderPref(R.string.DataAndStorageSettingsFragment__media_quality)

      radioListPref(
        title = DSLSettingsText.from(R.string.DataAndStorageSettingsFragment__sent_media_quality),
        listItems = sentMediaQualityLabels,
        selected = SentMediaQuality.values().indexOf(state.sentMediaQuality),
        onSelected = { viewModel.setSentMediaQuality(SentMediaQuality.values()[it]) }
      )

      textPref(
        summary = DSLSettingsText.from(R.string.DataAndStorageSettingsFragment__sending_high_quality_media_will_use_more_data)
      )

      dividerPref()

      sectionHeaderPref(R.string.MessageHistorySettingsFragment__message_history)

      textPref(
        summary = DSLSettingsText.from(R.string.MessageHistorySettingsFragment__description_universal)
      )

      clickPref(
        title = DSLSettingsText.from(R.string.MessageHistorySettingsFragment__keep_messages),
        summary = DSLSettingsText.from(state.universalHistoryTrimDelay.formatHistoryTrimDelay(requireContext())),
        onClick = {
          Navigation.findNavController(requireView()).safeNavigate(R.id.action_dataAndStorageSettingsFragment_to_messageHistoryDelaySettingsFragment)
        }
      )

      clickPref(
        title = DSLSettingsText.from(R.string.MessageHistorySettingsFragment__conversation_length_limit),
        summary = DSLSettingsText.from(state.universalHistoryTrimLength.formatHistoryTrimLength(requireContext())),
        onClick = {
          Navigation.findNavController(requireView()).safeNavigate(R.id.action_dataAndStorageSettingsFragment_to_messageHistoryLengthSettingsFragment)
        }
      )

      clickPref(
        title = DSLSettingsText.from(R.string.preferences__clear_message_history),
        onClick = {
          MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.preferences_storage__clear_message_history)
            .setMessage(R.string.preferences_storage__this_will_delete_all_message_history_and_media_from_your_device)
            .setPositiveButton(R.string.delete) { _, _ ->
              MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.preferences_storage__are_you_sure_you_want_to_delete_all_message_history)
                .setMessage(R.string.preferences_storage__all_message_history_will_be_permanently_removed_this_action_cannot_be_undone)
                .setPositiveButton(R.string.preferences_storage__delete_all_now) { _, _ ->
                  SignalExecutors.BOUNDED.execute {
                    SignalDatabase.threads.deleteAllConversations();
                    ApplicationDependencies.getMessageNotifier().updateNotification(requireContext());
                  }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show();
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .show();
        }
      )

      dividerPref()

      sectionHeaderPref(R.string.DataAndStorageSettingsFragment__calls)

      radioListPref(
        title = DSLSettingsText.from(R.string.preferences_data_and_storage__use_less_data_for_calls),
        listItems = callDataModeLabels,
        selected = abs(state.callDataMode.code - 2),
        onSelected = {
          viewModel.setCallDataMode(CallDataMode.fromCode(abs(it - 2)))
        }
      )

      textPref(
        summary = DSLSettingsText.from(R.string.preference_data_and_storage__using_less_data_may_improve_calls_on_bad_networks)
      )

      dividerPref()

      sectionHeaderPref(R.string.preferences_proxy)

      clickPref(
        title = DSLSettingsText.from(R.string.preferences_use_proxy),
        summary = DSLSettingsText.from(if (state.isProxyEnabled) R.string.preferences_on else R.string.preferences_off),
        onClick = {
          Navigation.findNavController(requireView()).safeNavigate(R.id.action_dataAndStorageSettingsFragment_to_editProxyFragment)
        }
      )
    }
  }
}
