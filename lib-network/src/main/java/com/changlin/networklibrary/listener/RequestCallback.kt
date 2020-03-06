package com.changlin.networklibrary.listener

interface RequestCallback<T> {

    fun success(any: T)

    fun failed(e:Throwable)
}