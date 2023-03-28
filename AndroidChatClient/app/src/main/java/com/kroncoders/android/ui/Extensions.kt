package com.kroncoders.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.log.Priority
import io.getstream.log.streamLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            streamLog(priority = Priority.ERROR, throwable = exception) { "Error in request " }
            onError(errorMessage)
        } finally {
            onLoading(false)
        }
    }
}
