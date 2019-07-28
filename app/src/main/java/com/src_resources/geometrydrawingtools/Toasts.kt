package com.src_resources.geometrydrawingtools

import android.content.Context
//import android.support.annotation.StringRes
import android.widget.Toast

fun Context.showToast(text: String, duration: Int) {
    Toast.makeText(this, text, duration).show()
}

fun Context.showToast(/*@StringRes*/ textId: Int, duration: Int) {
    Toast.makeText(this, textId, duration).show()
}

fun Context.showLongDurationToast(text: String) = showToast(text, Toast.LENGTH_LONG)

fun Context.showLongDurationToast(/*@StringRes*/ textId: Int) = showToast(textId, Toast.LENGTH_LONG)

fun Context.showShortDurationToast(text: String) = showToast(text, Toast.LENGTH_SHORT)

fun Context.showShortDurationToast(/*@StringRes*/ textId: Int) = showToast(textId, Toast.LENGTH_SHORT)
