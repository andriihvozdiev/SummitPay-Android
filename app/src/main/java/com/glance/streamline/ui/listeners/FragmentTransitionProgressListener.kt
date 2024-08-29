package com.glance.streamline.ui.listeners

import com.glance.streamline.ui.models.TransitionAnimationStatus

interface FragmentTransitionProgressListener {
    fun onFragmentTransitionProgressChanged(status: TransitionAnimationStatus)
    companion object {
        val empty = object: FragmentTransitionProgressListener {
            override fun onFragmentTransitionProgressChanged(status: TransitionAnimationStatus){}
        }
    }
}