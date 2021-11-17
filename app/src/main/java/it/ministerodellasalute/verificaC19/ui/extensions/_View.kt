package it.ministerodellasalute.verificaC19.ui.extensions

import android.view.View
import android.view.View.*

fun View.show() {
    visibility = VISIBLE
}

fun View.hide() {
    visibility = GONE
}

fun View.invisible() {
    visibility = INVISIBLE
}