package com.glance.streamline.ui.dialogs

import android.app.Activity
import androidx.recyclerview.widget.GridLayoutManager
import com.glance.streamline.R
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.adapters.recycler_view.ProductMainAdapter
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.convertDpToPixel
import com.glance.streamline.utils.extensions.android.view.recycler_view.SpacesItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_product_category.*

class ProductCategoriesBottomSheetDialog(
    context: Activity,
    private val filteredClickListener: FilteredClickListener
) : BottomSheetDialog(context, R.style.ProductOptionsBottomSheetDialogTheme) {

    private var categoriesListAdapter: ProductMainAdapter? = null
    var onCategorySelected: (ProductButtonInfo) -> Unit = {}

    fun init(layouts: ArrayList<ProductLayout>) {
        if (layouts.isNotEmpty()) {
            setContentView(R.layout.dialog_product_category)
            options_list?.apply {
                val decorationsSpacing = context.convertDpToPixel(3f).toInt()
                categoriesListAdapter = ProductMainAdapter(
                    /*layouts*/arrayListOf(),
                    filteredClickListener,
                    ProductMainAdapter.AdapterType.GRID_SIZE,
                    onCategorySelected
                )
                adapter = categoriesListAdapter
                (layoutManager as? GridLayoutManager)?.let {
                    addItemDecoration(SpacesItemDecoration(it.spanCount, decorationsSpacing, false))
                }
            }
        }
    }

    override fun show() {
        if ((categoriesListAdapter?.itemCount ?: 0) > 0)
            super.show()
    }
}