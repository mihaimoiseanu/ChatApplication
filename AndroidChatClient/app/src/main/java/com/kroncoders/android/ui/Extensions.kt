package com.kroncoders.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

fun <T> ViewModel.executeRequest(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: suspend () -> T,
    onLoading: (Boolean) -> Unit = {},
    onSuccess: suspend (T) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    viewModelScope.launch(dispatcher) {
        try {
            onLoading(true)
            val result = block()
            onSuccess(result)
        } catch (exception: Exception) {
            val errorMessage = exception.message ?: "Error"
            Timber.e(exception)
            onError(errorMessage)
        } finally {
            onLoading(false)
        }
    }
}
