package com.togocourier.Interface

interface MyOnClick {
    fun OnClick(id: String, requestId: String, position: Int)
    fun OnClickItem(postId: String, userId: String?,position: Int)

    fun deleteMyPost(postId: String, position: Int)
}