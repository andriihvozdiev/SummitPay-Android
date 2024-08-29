package com.glance.streamline.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.adapters.recycler_view.ProductsListAdapter
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.convertDpToPixel


class ProductsListLayout(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    private val productRecyclerViewIds = arrayListOf<Int>()

    var onListEmpty: (isEmpty: Boolean) -> Unit = {}
    var onItemClicked: (view: View, product: ProductButtonInfo) -> Unit = { _, _ -> }
    //var currentCategoryColor = 0

    fun loadProductsList(
        filteredClickListener: FilteredClickListener,
        productList: ArrayList<ProductButtonInfo>
    ) {
        productRecyclerViewIds.forEach {
            (findViewById<RecyclerView>(it)?.adapter as? ProductsListAdapter)?.clear()
        }
        if (productList.isEmpty()) {
            onListEmpty(true)
        } else {
            onListEmpty(false)
            //currentCategoryColor = productList[0].category.color
            val lists = arrayListOf<MutableList<ProductButtonInfo>>()
            //val stepSize = productList.size / (if (context.isTabletDevice()) 3 else 2)
            //if (stepSize == 0) lists.add(productList)
            lists.add(productList)
//            else {
//                if (productList.lastIndex >= stepSize - 1) lists.add(
//                    productList.subList(0, stepSize)
//                )
//                if (productList.lastIndex >= stepSize * 2 - 1) lists.add(
//                    productList.subList(stepSize, stepSize * 2)
//                )
//                if (productList.lastIndex > stepSize * 2) lists.add(
//                    productList.subList(stepSize * 2, productList.lastIndex)
//                )
//            }
            lists.forEachIndexed { index, item ->
                val list = item.toCollection(ArrayList())
                //list.sortByDescending { it.popularity }
                if (productRecyclerViewIds.lastIndex >= index) {
                    val recyclerView = findViewById<RecyclerView>(productRecyclerViewIds[index])
                    if (recyclerView != null) {
                        (recyclerView.adapter as? ProductsListAdapter)?.let {
                            it.setProductsList(list)
                            return@forEachIndexed
                        }
                    }
                }
                val recycler = initProductsList(filteredClickListener, list)
                addView(recycler)
            }
        }
    }

    private fun initProductsList(
        filteredClickListener: FilteredClickListener,
        productList: ArrayList<ProductButtonInfo>
    ): RecyclerView {
        return RecyclerView(context).apply {
            this.id = View.generateViewId()
            productRecyclerViewIds.add(id)
            this.layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
            )
            //val gridLayoutManager = GridLayoutManager(context, 10)
           // this.layoutManager = gridLayoutManager
            layoutManager = StaggeredGridLayoutManager(6, GridLayoutManager.VERTICAL)
            val foodsAdapter =
                ProductsListAdapter(productList, filteredClickListener, onClick = onItemClicked)
            this.adapter = foodsAdapter
            this.isNestedScrollingEnabled = false
//            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//                override fun getSpanSize(position: Int): Int {
//                    //return if (foodsAdapter.getItemsList()[position].popularity > 0) 2 else 1
//                    return foodsAdapter.getItemsList()[position].x
//                }
//            }
            val spaceSize = context.convertDpToPixel(3f).toInt()
//            this.addItemDecoration(
//                SpacesItemDecoration(
//                    gridLayoutManager.spanCount,
//                    spaceSize,
//                    false
//                )
//            )
        }
    }

//    fun checkColors(categoryColors: List<Int>) {
//        val currentColor = categoryColors.find { it == currentCategoryColor }
//        if (currentColor == null) clearLists()
//    }

    fun clearLists() {
        productRecyclerViewIds.forEach { listId ->
            val recyclerView = findViewById<RecyclerView>(listId)
            if (recyclerView != null) {
                (recyclerView.adapter as? ProductsListAdapter)?.let {
                    it.clear()
                }
            }
            onListEmpty(true)
        }
    }

//    private fun getAdapters(): List<ProductsListAdapter> {
//        return productRecyclerViewIds.flatMap {
//            val recyclerView = findViewById<RecyclerView>(it)
//            arrayListOf(
//                recyclerView.adapter as? ProductsListAdapter
//            )
//        }.filterNotNull()
//    }
}
