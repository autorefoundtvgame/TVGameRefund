package com.openhands.tvgamerefund.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.openhands.tvgamerefund.data.network.FreeAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel qui expose FreeAuthManager pour l'injection dans les composables
 */
@HiltViewModel
class FreeAuthManagerViewModel @Inject constructor(
    val authManager: FreeAuthManager
) : ViewModel()