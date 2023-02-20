package com.example.br_flickr.ui.main.util

interface ViewItem<T> : Comparable<T> {
    fun areContentsTheSame(other: T): Boolean
    fun areItemsTheSame(other: T): Boolean
}
