package com.glance.streamline.ui.adapters.view_pager

import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.glance.streamline.ui.base.BaseFragment

class TroubleshootingPagerAdapter(fm: FragmentManager,
                                  pager: ViewPager,
                                  fragments: ArrayList<BaseFragment<*>>) : BasePagerAdapter(fm, pager, fragments) {

    init {
        initFragments()
    }

    override fun getTitle(pageIndex: Int): String {
        return ""
    }
}
