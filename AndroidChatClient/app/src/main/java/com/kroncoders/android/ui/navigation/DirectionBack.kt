package com.kroncoders.android.ui.navigation

import android.os.Bundle
import androidx.navigation.NavOptionsBuilder

object DirectionBack : NavigationDirection {
    override val arguments: Bundle? = null
    override val navBuilder: NavOptionsBuilder.() -> Unit = {}
}
