package com.kroncoders.android.ui.navigation.directions

import android.os.Bundle
import androidx.navigation.NavOptionsBuilder
import com.kroncoders.android.ui.navigation.NavigationDirection

object Loading : NavigationDirection {
    override val arguments: Bundle? = null
    override val navBuilder: NavOptionsBuilder.() -> Unit = {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}