package com.chimera.feature.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.chimera.data.AiMode
import com.chimera.data.AppSettings
import com.chimera.data.BillingManager
import com.chimera.data.ChimeraPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: ChimeraPreferences,
    private val billingManager: BillingManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = preferences.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    /** Empty until real products exist in Play Console -- see BillingManager. */
    val availableSupporterProducts: StateFlow<List<ProductDetails>> = billingManager.availableProducts

    fun purchaseSupporterProduct(activity: Activity, productDetails: ProductDetails) {
        billingManager.launchPurchaseFlow(activity, productDetails)
    }

    fun setTextScale(scale: Float) {
        viewModelScope.launch { preferences.setTextScale(scale) }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { preferences.setReduceMotion(enabled) }
    }

    fun toggleAiMode() {
        viewModelScope.launch {
            val current = settings.value.aiMode
            val next = if (current == AiMode.AUTO) AiMode.OFFLINE_ONLY else AiMode.AUTO
            preferences.setAiMode(next)
        }
    }

    fun setAnalyticsOptIn(optIn: Boolean) {
        viewModelScope.launch { preferences.setAnalyticsOptIn(optIn) }
    }

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setVoiceEnabled(enabled) }
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setCloudSyncEnabled(enabled) }
    }
}
