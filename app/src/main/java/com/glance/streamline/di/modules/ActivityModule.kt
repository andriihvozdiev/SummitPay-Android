package com.glance.streamline.di.modules

import com.glance.streamline.di.scopes.ActivityScope
import com.glance.streamline.ui.base.BaseActivity
import dagger.Module
import dagger.Provides

@Module
class ActivityModule (private val baseActivity: BaseActivity<*>) {

    @Provides
    @ActivityScope
    fun provideBaseActivity(): BaseActivity<*> = baseActivity

}