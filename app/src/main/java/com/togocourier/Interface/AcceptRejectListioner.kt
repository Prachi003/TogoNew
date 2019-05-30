package com.togocourier.Interface

interface AcceptRejectListioner {
    fun OnClick(id: String, status: String, bitPrice: String, applyUserId: String?)
    fun OnClickUserId(userId: String, postUserId: String?)
}