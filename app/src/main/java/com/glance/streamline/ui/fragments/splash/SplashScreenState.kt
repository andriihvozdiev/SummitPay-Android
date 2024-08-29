package com.glance.streamline.ui.fragments.splash

import com.glance.streamline.data.entities.UserModel

sealed class SplashScreenState
object DeviceAssignScreenState : SplashScreenState()
object SignInScreenState : SplashScreenState()
object DebugScreenState : SplashScreenState()
data class MainScreenState(val userModel: UserModel) : SplashScreenState()