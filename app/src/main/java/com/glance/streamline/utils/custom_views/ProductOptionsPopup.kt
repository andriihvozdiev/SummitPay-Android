package com.glance.streamline.utils.custom_views

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.GridLayoutManager
import com.glance.streamline.R
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.adapters.recycler_view.OrderItemsListAdapter
import com.glance.streamline.ui.adapters.recycler_view.ProductOptionsListAdapter
import com.glance.streamline.ui.base.BaseFragment
import com.glance.streamline.utils.convertDpToPixel
import com.glance.streamline.utils.extensions.android.view.recycler_view.SpacesItemDecoration
import com.glance.streamline.utils.extensions.android.view.visible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.popup_food_item_layout.view.*
import kotlin.math.abs

class ProductOptionsPopup(val fragment: BaseFragment<*>) : PopupWindow(), LayoutContainer {
    override val containerView: View? get() = fragment.view

    init {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
    }

    var addToOrderItemsList: (OrderItemsListAdapter.BasketItem) -> Unit = {}
    var onAnchorViewShouldBeScrolledVertically: (scrollLength: Int) -> Unit = {}
    var onReopen: (viewToAnchor: View, product: ProductButtonInfo) -> Unit =
        { _, _ -> }

    fun show(viewToAnchor: View, button: ProductButtonInfo) {
        if (button.allProductOptions?.isNullOrEmpty()?.not() == true) {
            val popUpView = fragment.layoutInflater.inflate(R.layout.popup_food_item_layout, null)
            popUpView.options_title_text_view?.text = button.product_name

            popUpView.options_back_text_view.apply {
                visible()
                fragment.apply {
                    onClick {
                        //onReopen(viewToAnchor, button)
                        dismiss()
                    }
                }
            }

            popUpView.options_list?.apply {
                val arrayOptions = button.allProductOptions as? ArrayList<ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption>
                adapter = ProductOptionsListAdapter(arrayOptions ?: arrayListOf(), fragment) { view, item ->
                        if (arrayOptions?.isNullOrEmpty()?.not() == true) {
                            addToOrderItemsList(OrderItemsListAdapter.BasketOption(item))
                            onReopen(viewToAnchor, button)
                        }
                        else {
                            addToOrderItemsList(OrderItemsListAdapter.BasketOption(item))
                        }
                        dismiss()
                    }
                (layoutManager as? GridLayoutManager)?.let {
                    addItemDecoration(SpacesItemDecoration(it.spanCount, 8, false))
                }
            }

            popUpView.options_list.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            popUpView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            val xOffset = getPopUpWindowHorizontalOffset(popUpView, viewToAnchor)
            val yOffset = getPopUpWindowVerticalOffset(popUpView, viewToAnchor, xOffset < 0)

            contentView = popUpView

            showAsDropDown(viewToAnchor, xOffset, yOffset)
        }
    }

    private fun getPopUpWindowHorizontalOffset(popUpView: View, viewToAnchor: View): Int {
        val visibleAnchorRect = Rect()
        viewToAnchor.getGlobalVisibleRect(visibleAnchorRect)
        val isRightPartOfScreen = popUpView.measuredWidth >= visibleAnchorRect.left
        val arrowWidth = popUpView.options_top_arrow.minimumWidth / 2
        var xOffset: Int
        if (isRightPartOfScreen) {
            xOffset = viewToAnchor.measuredWidth
        } else {
            val x0 = popUpView.measuredWidth
            val x1 = popUpView.options_card_view.marginEnd
            val x2 = abs(popUpView.translationX.toInt())
            xOffset = -(x0 - x1 - x2)
        }
        xOffset -= arrowWidth
        return xOffset
    }

    private fun getPopUpWindowVerticalOffset(popUpView: View, viewToAnchor: View, showLeftArrow: Boolean): Int {
        val popUpArrowOffset = popUpView.context.convertDpToPixel(34f).toInt()
        val visibleAnchorRect = Rect()
        viewToAnchor.getGlobalVisibleRect(visibleAnchorRect)
        val isTopPartOfScreen = popUpView.measuredHeight >= visibleAnchorRect.top
        var yOffset: Int
        var minimumHeightToScroll = visibleAnchorRect.height() - popUpArrowOffset
        if (isTopPartOfScreen) {
            yOffset = -visibleAnchorRect.height()
            if (!showLeftArrow) popUpView.options_top_arrow.visible()
            else popUpView.options_top_right_arrow.visible()
        } else {
            yOffset =
                -(popUpView.measuredHeight - popUpView.options_card_view.marginBottom + viewToAnchor.height - visibleAnchorRect.height())
            minimumHeightToScroll *= -1
            if (!showLeftArrow) popUpView.options_bottom_arrow.visible()
            else popUpView.options_bottom_right_arrow.visible()
        }
        if (visibleAnchorRect.height() < popUpArrowOffset) {
            onAnchorViewShouldBeScrolledVertically(minimumHeightToScroll)
            yOffset -= -minimumHeightToScroll
        }
        return yOffset
    }
}