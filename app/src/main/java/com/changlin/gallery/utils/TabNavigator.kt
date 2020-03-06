package com.changlin.gallery.utils

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment

@Navigator.Name("tab_fragment")  // Use as custom tag at navigation.xml
class TabNavigator (
    private val context: Context,
    private val manager: FragmentManager,
    private val containerId: Int
) : FragmentNavigator(context, manager, containerId) {

    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ): NavDestination? {
        return super.navigate(destination, args, navOptions, navigatorExtras)
    }

}

class TabNavHostFragment: NavHostFragment() {
    override fun createFragmentNavigator(): Navigator<out FragmentNavigator.Destination> {
        return TabNavigator(requireContext(), childFragmentManager, id)
    }
}