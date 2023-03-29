package com.kroncoders.android.ui.screens.call

import androidx.lifecycle.ViewModel
import com.kroncoders.android.networking.call.CallService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CallScreenViewModel @Inject constructor(
    private val callService: CallService
) : ViewModel() {


}