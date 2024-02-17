/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.google.android.gms.R
import org.microg.gms.ui.settings.SettingsProvider
import org.microg.gms.ui.settings.getAllSettingsProviders
import org.microg.tools.ui.ResourceSettingsFragment

class SettingsFragment : ResourceSettingsFragment() {
    private val createdPreferences = mutableListOf<Preference>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        findPreference<Preference>(PREF_ABOUT)!!.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                findNavController().navigate(requireContext(), R.id.openAbout)
                true
            }
            summary = getString(org.microg.tools.ui.R.string.about_version_str, AboutFragment.getSelfVersion(context))
        }

        for (entry in getAllSettingsProviders(requireContext()).flatMap { it.getEntriesStatic(requireContext()) }) {
            entry.createPreference()
        }
    }

    private fun SettingsProvider.Companion.Entry.createPreference(): Preference? {
        val preference = Preference(requireContext()).fillFromEntry(this)
        try {
            if (findPreference<PreferenceCategory>(when (group) {
                    SettingsProvider.Companion.Group.HEADER -> "prefcat_header"
                    SettingsProvider.Companion.Group.GOOGLE -> "prefcat_google_services"
                    SettingsProvider.Companion.Group.OTHER -> "prefcat_other_services"
                    SettingsProvider.Companion.Group.FOOTER -> "prefcat_footer"
                })?.addPreference(preference) == true) {
                createdPreferences.add(preference)
                return preference
            } else {
                Log.w(TAG, "Preference not added $key")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed adding preference $key", e)
        }
        return null
    }

    private fun Preference.fillFromEntry(entry: SettingsProvider.Companion.Entry): Preference {
        key = entry.key
        title = entry.title
        summary = entry.summary
        icon = entry.icon
        isPersistent = false
        isVisible = true
        setOnPreferenceClickListener {
            findNavController().navigate(requireContext(), entry.navigationId)
            true
        }
        return this
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenResumed {
            val entries = getAllSettingsProviders(requireContext()).flatMap { it.getEntriesDynamic(requireContext()) }
            for (preference in createdPreferences) {
                if (!entries.any { it.key == preference.key }) preference.isVisible = false
            }
            for (entry in entries) {
                val preference = createdPreferences.find { it.key == entry.key }
                if (preference != null) preference.fillFromEntry(entry)
                else entry.createPreference()
            }
        }
    }

    companion object {
        const val PREF_ABOUT = "pref_about"
    }

    init {
        preferencesResource = R.xml.preferences_start
    }
}
