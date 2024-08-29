package com.glance.streamline.utils.extensions

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.util.*


fun Bitmap.getUri(context: Context, compress: Int = 50): Uri {
    // Get the context wrapper
    val wrapper = ContextWrapper(context)

    // Initialize a new file instance to save bitmap object
    var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
    file = File(file, "${UUID.randomUUID()}.jpg")

    try {
        // Compress the bitmap and save in jpg format
        val stream: OutputStream = FileOutputStream(file)
        compress(Bitmap.CompressFormat.JPEG, compress, stream)
        stream.flush()
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    // Return the saved bitmap uri
    return Uri.parse(file.absolutePath)
}

fun String.getCompressedBitmap(context: Context, compress: Int = 100): Uri? {
    if (isPathToImage()) {
        val bitmap = BitmapFactory.decodeFile(this)
        return bitmap.getUri(context, compress)
    }
    return null
}

fun Bitmap.getBase64String(compress: Int = 100): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, compress, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun String.getBitmapFromBase64(): Bitmap {
    val decodedByteArray = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
}

fun ByteArray.getBitmapFromBase64(): Bitmap {
    val decodedByteArray = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
}

fun String.getBitmapFromBase64Single(): Single<Bitmap> {
    return Single.fromCallable {
        val decodedByteArray = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }.observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.computation())
}

fun Drawable.getBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(
        intrinsicWidth,
        intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bmp
}