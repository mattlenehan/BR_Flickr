package com.example.br_flickr.ui.main.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(msg: String?) {
    if (msg != null) {
        Snackbar.make(this, msg, Snackbar.LENGTH_LONG)
            .setAction("dismiss") {}
            .show()
    }
}