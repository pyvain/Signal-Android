package org.thoughtcrime.securesms.components.settings.conversation.history

import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.components.settings.conversation.preferences.Utils.formatHistoryTrimDelay
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.navigation.safeNavigate
import org.thoughtcrime.securesms.components.settings.conversation.preferences.Utils.formatHistoryTrimLength

class MessageHistorySettingsFragment : DSLSettingsFragment(
  titleId = R.string.MessageHistorySettingsFragment__message_history
) {

  private val viewModel: MessageHistorySettingsViewModel by viewModels(
    factoryProducer = {
      val recipientId = MessageHistorySettingsFragmentArgs.fromBundle(requireArguments()).recipientId

      MessageHistorySettingsViewModel.Factory(recipientId)
    }
  )

  override fun onResume() {
    super.onResume()
    viewModel.refresh()
  }

  override fun bindAdapter(adapter: MappingAdapter) {
    viewModel.state.observe(viewLifecycleOwner) {
      adapter.submitList(getConfiguration(it).toMappingModelList())
    }
  }

  private fun getConfiguration(state: MessageHistorySettingsState): DSLConfiguration {
    return configure {
      clickPref(
        title = DSLSettingsText.from(R.string.MessageHistorySettingsFragment__keep_messages),
        //icon = DSLSettingsIcon.from(R.drawable.),
        summary = DSLSettingsText.from(state.delay.formatHistoryTrimDelay(requireContext())),
        onClick = {
          val recipientId = MessageHistorySettingsFragmentArgs.fromBundle(requireArguments()).recipientId
          val action = MessageHistorySettingsFragmentDirections.actionMessageHistorySettingsFragmentToMessageHistoryDelaySettingsFragment(recipientId)
          Navigation.findNavController(requireView()).safeNavigate(action)
        }
      )

      clickPref(
        title = DSLSettingsText.from(R.string.MessageHistorySettingsFragment__conversation_length_limit),
        //icon = DSLSettingsIcon.from(R.drawable.),
        summary = DSLSettingsText.from(state.length.formatHistoryTrimLength(requireContext())),
        onClick = {
          val recipientId = MessageHistorySettingsFragmentArgs.fromBundle(requireArguments()).recipientId
          val action = MessageHistorySettingsFragmentDirections.actionMessageHistorySettingsFragmentToMessageHistoryLengthSettingsFragment(recipientId)
          Navigation.findNavController(requireView()).safeNavigate(action)
        }
      )
    }
  }
}
