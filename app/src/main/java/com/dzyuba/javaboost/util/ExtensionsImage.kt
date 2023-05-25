package com.dzyuba.javaboost.util

import android.graphics.*
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.signature.ObjectKey
import com.dzyuba.javaboost.R
import kotlin.math.min


var options = RequestOptions()
    .centerCrop()
//    .placeholder(R.drawable.icon_avatar_empty)
//    .error(R.drawable.icon_avatar_empty)
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .priority(Priority.HIGH)

/**
 * Расширение для загрузки изображений по сети с обрезанием в виде окружности.
 *
 * @param model - Объект поддерживаемый Glide (Uri, File, Bitmap, String, id ресурса как Int, ByteArray, и Drawable)
 * @param borderSize - Размер рамки в пикселях.
 * @param borderColor - Цвет рамки.
 */
fun <T> ImageView.loadCircularImage(
    model: T,
    borderSize: Float = 0F,
    borderColor: Int = Color.WHITE
) {
    //Если у изображения меняется borderSize или borderColor
    //меняем строку и glide загружает это изображение занов
    val signatureString = if (borderSize>0)
        "$model$borderSize$borderColor"
    else
        "$model"
    val thumbnail = Glide.with(context)
        .asBitmap()
        .load(R.drawable.ic_placeholder)
        .circleCrop()

    Glide.with(context)
        .asBitmap()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .skipMemoryCache(false)
        .format(DecodeFormat.PREFER_RGB_565)
        .signature(ObjectKey(model.toString()))
        .apply(RequestOptions.circleCropTransform())
        .apply(options)
        .load(model)
        .signature(ObjectKey(signatureString))
        .thumbnail(thumbnail)
        .into(object : BitmapImageViewTarget(this) {
            override fun setResource(resource: Bitmap?) {
                setImageDrawable(
                    resource?.run {
                        RoundedBitmapDrawableFactory.create(
                            resources,
                            if (borderSize > 0) {
                                createBitmapWithBorder(borderSize, borderColor)
                            } else {
                                this
                            }
                        ).apply {
                            isCircular = true
                        }
                    }
                )
            }
        })
}

/**
 * Создает bitmap с указанной толщиной и цветом рамки.
 *
 * @param borderSize - Размер рамки в пикселях.
 * @param borderColor - Цвет рамки.
 * @return Новое изображение с рамкой.
 */
fun Bitmap.createBitmapWithBorder(borderSize: Float, borderColor: Int = Color.WHITE): Bitmap {
    val borderOffset = (borderSize * 2).toInt()
    val halfWidth = width / 2
    val halfHeight = height / 2
    val circleRadius = min(halfWidth, halfHeight).toFloat()
    val newBitmap = Bitmap.createBitmap(
        width + borderOffset,
        height + borderOffset,
        Bitmap.Config.ARGB_8888
    )

    // Координаты центра изображения
    val centerX = halfWidth + borderSize
    val centerY = halfHeight + borderSize

    val paint = Paint()
    val canvas = Canvas(newBitmap).apply {
        // Установка первоначальной прозрачной области
        drawARGB(0, 0, 0, 0)
    }

    // Отрисовка первоначальной прозрачной области
    paint.isAntiAlias = true
    paint.style = Paint.Style.FILL
    canvas.drawCircle(centerX, centerY, circleRadius, paint)

    // Отрисовка изображения
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, borderSize, borderSize, paint)

    // Отрисовка рамки
    paint.xfermode = null
    paint.style = Paint.Style.STROKE
    paint.color = borderColor
    paint.strokeWidth = borderSize
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    return newBitmap
}