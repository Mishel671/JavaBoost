package com.dzyuba.javaboost.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.dzyuba.javaboost.R
import com.dzyuba.javaboost.databinding.ProgressBarFullScreenBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.showAlert(
    titleResId: Int? = null,
    message: String,
    positiveResId: Int,
    negativeResId: Int? = null,
    positiveAction: (() -> Unit?)? = null,
    negativeAction: (() -> Unit?)? = null,
    cancelAction: (() -> Unit?)? = null
) {
    val activity = requireActivity()
    val builder = MaterialAlertDialogBuilder(activity, R.style.MyThemeOverlay_MaterialAlertDialog)
    val backgroundAlert = AppCompatResources.getDrawable(activity, R.drawable.bg_alert)
    builder.background = backgroundAlert

    builder.setMessage(message)

    titleResId?.let {
        builder.setTitle(it)
    }
    negativeResId?.let {
        builder.setNegativeButton(it) { dialog, which ->
            negativeAction?.invoke()
        }
    }
    builder.setPositiveButton(activity.resources.getString(positiveResId)) { dialog, which ->
        positiveAction?.invoke()
    }
    builder.setOnCancelListener {
        cancelAction?.invoke()
    }
    builder.show()
//    val dialog = builder.setMessage(message).setPositiveButton(positiveResId) { _, _ ->
//        positiveAction?.invoke()
//    }.create()

//    dialog.setOnShowListener {
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
//            .setTextColor(activity.getColor(R.color.black))
//        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
//            .setTextColor(activity.getColor(R.color.black))
//    }

//    dialog.show()
}


fun Fragment.showErrorAlert(
    error: Throwable?,
    titleResId: Int? = R.string.loading_error,
    positiveAction: (() -> Unit?)? = null
) =
    showAlert(
        message = error?.message ?: "Unknown error",
        titleResId = titleResId,
        positiveResId = R.string.ok,
        positiveAction = positiveAction
    )

fun initProgressBar(layoutInflater: LayoutInflater, context: Context): AlertDialog {
    return AlertDialog.Builder(context).apply {
        setView(ProgressBarFullScreenBinding.inflate(layoutInflater).root)
        setCancelable(false)

    }.create().apply {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}

fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}
