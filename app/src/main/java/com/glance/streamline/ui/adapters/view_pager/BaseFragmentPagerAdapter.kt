package com.glance.streamline.ui.adapters.view_pager

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.glance.streamline.ui.base.BaseFragment

abstract class BasePagerAdapter(fm: FragmentManager,
                                val pager: ViewPager,
                                val fragments: ArrayList<BaseFragment<*>>) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    val context: Context = pager.context

    private fun getVisibleCallback(fragment: Fragment): (fragment: Fragment) -> Boolean {
        return {
            val fragmentIndex = fragments.indexOf(fragment)
            fragmentIndex == pager.currentItem
        }
    }

    init {
        initFragments()
    }

    protected fun initFragments(){
        fragments.forEachIndexed { index, item ->
            item.isPageVisible = getVisibleCallback(item)
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as BaseFragment<*>
        fragments[position] = fragment
        return fragment
    }

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size

    override fun getPageTitle(position: Int) = getTitle(position)

    abstract fun getTitle(pageIndex: Int): String
}
