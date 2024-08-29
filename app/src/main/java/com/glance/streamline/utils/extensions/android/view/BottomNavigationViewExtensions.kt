package com.glance.streamline.utils.extensions.android.view

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.glance.streamline.R
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView


/**
 * Manages the various graphs needed for a [BottomNavigationView].
 *
 * This sample is a workaround until the Navigation Component supports multiple back stacks.
 */
fun BottomNavigationView.setupWithNavController(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    intent: Intent,
    defaultTabIndex: Int = -1,
    onDestinationChanged: (NavController) -> Unit = {}
): LiveData<NavHostFragment> {

    // Map of tags
    val graphIdToTagMap = SparseArray<String>()
    // Result. Mutable live data with the selected controlled
    val selectedNavController = MutableLiveData<NavHostFragment>()
    var defaultHostFragmentGraphId = 0

    // First create a NavHostFragment for each NavGraph ID
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )

        navHostFragment.navController.addOnDestinationChangedListener { controller: NavController, _: NavDestination, _: Bundle? ->
            onDestinationChanged(controller)
        }

        // Obtain its code
        val graphId = navHostFragment.navController.graph.id

        if (index == defaultTabIndex) {
            defaultHostFragmentGraphId = graphId
//            fragmentManager.beginTransaction()
//                .addToBackStack(graphIdToTagMap[defaultHostFragmentGraphId])
//                .commit()
        }

        // Save to the map
        graphIdToTagMap[graphId] = fragmentTag

        // Attach or detach nav host fragment depending on whether it's the selected item.
        if (this.selectedItemId == graphId) {
            // Update livedata with the selected graph
            selectedNavController.value = navHostFragment
            attachNavHostFragment(
                fragmentManager,
                navHostFragment,
                true
            )
        } else {
            detachNavHostFragment(
                fragmentManager,
                navHostFragment
            )
        }
    }

    // Now connect selecting an item with swapping Fragments
    var selectedItemTag = graphIdToTagMap[this.selectedItemId]
    val defaultHostFragmentTag = graphIdToTagMap[defaultHostFragmentGraphId]
    var isOnDefaultFragment = selectedItemTag == defaultHostFragmentTag

    if (!isOnDefaultFragment && defaultHostFragmentTag != null) {

//        fragmentManager.popBackStack(
//            selectedItemTag,
//            FragmentManager.POP_BACK_STACK_INCLUSIVE
//        )

        val selectedFragment = fragmentManager.findFragmentByTag(selectedItemTag)
                as NavHostFragment

        if (defaultHostFragmentTag != selectedItemTag) {
            fragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.nav_default_enter_anim,
                    R.anim.nav_default_exit_anim,
                    R.anim.nav_default_pop_enter_anim,
                    R.anim.nav_default_pop_exit_anim
                )
                .apply {
                    fragmentManager.findFragmentByTag(defaultHostFragmentTag)
                        ?.let { detach(it) }
                }
                .attach(selectedFragment)
                .addToBackStack(defaultHostFragmentTag)
                .commit()
        }


    }

    // When a navigation item is selected
    setOnNavigationItemSelectedListener { item ->
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val newlySelectedItemTag = graphIdToTagMap[item.itemId]


            if (defaultHostFragmentTag == null) {
//            if (selectedItemTag != newlySelectedItemTag) {
                // Pop everything above the first fragment (the "fixed start destination")


                fragmentManager.popBackStack(
                    newlySelectedItemTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )


                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment

                // Exclude the first fragment tag because it's always in the back stack.
//                if (firstFragmentTag != newlySelectedItemTag) {
                // Commit a transaction that cleans the back stack and adds the first fragment
                // to it, creating the fixed started destination.
                fragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.nav_default_enter_anim,
                        R.anim.nav_default_exit_anim,
                        R.anim.nav_default_pop_enter_anim,
                        R.anim.nav_default_pop_exit_anim
                    )
                    .attach(selectedFragment)
                    .setPrimaryNavigationFragment(selectedFragment)
                    .apply {
                        // Detach all other Fragments
                        graphIdToTagMap.forEach { _, fragmentTagIter ->
                            if (fragmentTagIter != newlySelectedItemTag) {
                                fragmentManager.findFragmentByTag(fragmentTagIter)?.let {
                                    detach(it)
                                }
                            }
                        }
                    }
                    .setReorderingAllowed(true)
                    .commit()
//                }

                selectedNavController.value = selectedFragment
                selectedItemTag = newlySelectedItemTag
                isOnDefaultFragment = selectedItemTag == defaultHostFragmentTag
                true
            } else {

                if (selectedItemTag != newlySelectedItemTag) {


                    fragmentManager.popBackStack(
                        defaultHostFragmentTag,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                            as NavHostFragment

                    // Exclude the first fragment tag because it's always in the back stack.
                    if (defaultHostFragmentTag != newlySelectedItemTag) {
                        // Commit a transaction that cleans the back stack and adds the first fragment
                        // to it, creating the fixed started destination.
                        fragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.nav_default_enter_anim,
                                R.anim.nav_default_exit_anim,
                                R.anim.nav_default_pop_enter_anim,
                                R.anim.nav_default_pop_exit_anim
                            )
                            .attach(selectedFragment)
                            .setPrimaryNavigationFragment(selectedFragment)
                            .apply {
                                // Detach all other Fragments
                                fragmentManager.findFragmentByTag(defaultHostFragmentTag)
                                    ?.let { detach(it) }
//                                graphIdToTagMap.forEach { _, fragmentTagIter ->
//                                    if (fragmentTagIter != newlySelectedItemTag) {
//                                        fragmentManager.findFragmentByTag(defaultHostFragmentTag)
//                                            ?.let { detach(it) }
//                                    }
//                                }
                            }
                            .addToBackStack(defaultHostFragmentTag)
                            .setReorderingAllowed(true)
                            .commit()
                    }

                    selectedNavController.value = selectedFragment
                    selectedItemTag = newlySelectedItemTag
                    isOnDefaultFragment = selectedItemTag == defaultHostFragmentTag
                    true
                } else {
                    false
                }
            }
        }
    }


    // Optional: on item reselected, pop back stack to the destination of the graph
    setupItemReselected(graphIdToTagMap, fragmentManager)

    // Handle deep link
    setupDeepLinks(navGraphIds, fragmentManager, containerId, intent)

    // Finally, ensure that we update our BottomNavigationView when the back stack changes
    fragmentManager.addOnBackStackChangedListener {
        if (!isOnDefaultFragment && defaultHostFragmentTag != null && !fragmentManager.isOnBackStack(
                defaultHostFragmentTag
            )
        ) {
            this.selectedItemId = defaultHostFragmentGraphId
        }

        // Reset the graph if the currentDestination is not valid (happens when the back
        // stack is popped after using the back button).
        selectedNavController.value?.navController?.let { controller ->
            if (controller.currentDestination == null) {
                controller.navigate(controller.graph.id)
            }
        }
    }
    return selectedNavController
}

fun BottomNavigationView.getNavigationItemView(index: Int): View? {
    val bottomMenu = getChildAt(0) as? BottomNavigationMenuView
    return bottomMenu?.getChildAt(index) as? BottomNavigationItemView
}

private fun BottomNavigationView.setupDeepLinks(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    intent: Intent
) {
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )
        // Handle Intent
        if (navHostFragment.navController.handleDeepLink(intent)
            && selectedItemId != navHostFragment.navController.graph.id
        ) {
            this.selectedItemId = navHostFragment.navController.graph.id
        }
    }

    /*val deepLink = intent.data?.toString()?:""
    when {
        deepLink.contains(context.getString(R.string.demo_deep_link_main)) -> {
            navGraphIds.getOrNull(0)?.let {
                val fragmentTag = getFragmentTag(0)
                // Find or create the Navigation host fragment
                val navHostFragment = obtainNavHostFragment(
                    fragmentManager,
                    fragmentTag,
                    it,
                    containerId
                )
                // Handle Intent
                if (selectedItemId != navHostFragment.navController.graph.code) {
                    this.selectedItemId = navHostFragment.navController.graph.code
                }
            }
        }
        deepLink.contains(context.getString(R.string.demo_deep_link_event)) -> {}
        deepLink.contains(context.getString(R.string.demo_deep_link_wellbeing)) -> {}
        else -> {}
    }*/
}

private fun BottomNavigationView.setupItemReselected(
    graphIdToTagMap: SparseArray<String>,
    fragmentManager: FragmentManager
) {
    setOnNavigationItemReselectedListener { item ->
        val newlySelectedItemTag = graphIdToTagMap[item.itemId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                as NavHostFragment?
        selectedFragment?.let {
            it.navController
            // Pop the back stack to the start destination of the current navController graph
            it.navController.popBackStack(
                it.navController.graph.startDestination, false
            )
        }
    }
}

private fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryNavFragment: Boolean
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()

}

private fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int
): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

private fun getFragmentTag(index: Int) = "bottomNavigation#$index"
