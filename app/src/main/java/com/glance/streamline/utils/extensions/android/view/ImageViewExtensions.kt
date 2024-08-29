package com.glance.streamline.utils.extensions.android.view

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

fun ImageView.loadImage(
    imageUrl: String = "",
    @DrawableRes imageResId: Int? = null,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes errorResId: Int? = null,
    progress: View? = null,
    compress: Int = 33,
    onSuccess: (resource: Drawable) -> Unit = {},
    onError: () -> Unit = {}
) {
    progress?.visible()
    val options = RequestOptions()
        .encodeQuality(compress)
        .centerInside()
        .diskCacheStrategy(DiskCacheStrategy.ALL)

    errorResId?.let { options.apply { error(it) } }

    val loader = Glide.with(this.context)
        .load(if (imageUrl.isBlank() && imageResId != null) imageResId else imageUrl)
    if (placeholder != null) loader.placeholder(placeholder)
    loader.apply(options)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                progress?.gone()
                onError()
                return false
            }

            override fun onResourceReady(
                resource: Drawable, model: Any,
                target: Target<Drawable>, dataSource:
                DataSource, isFirstResource: Boolean
            ): Boolean {
                progress?.gone()
                onSuccess(resource)
                return false
            }
        })
        .into(this)
}

fun ImageView.loadImage(
    bitmap: Bitmap,
    @DrawableRes errorResId: Int? = null,
    progress: View? = null,
    compress: Int = 33
) {
    progress?.visible()
    val options = RequestOptions()
        .encodeQuality(compress)
        .centerInside()
        .diskCacheStrategy(DiskCacheStrategy.ALL)

    errorResId?.let { options.apply { error(it) } }

    Glide.with(this.context)
        .load(bitmap)
        .apply(options)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                progress?.gone()
                return false
            }

            override fun onResourceReady(
                resource: Drawable, model: Any,
                target: Target<Drawable>, dataSource:
                DataSource, isFirstResource: Boolean
            ): Boolean {
                progress?.gone()
                return false
            }
        })
        .into(this)
}

