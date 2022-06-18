package com.fleet.knock.utils.requester

abstract class Requester {
    abstract val isRequest:Boolean

    abstract fun refresh()

    protected abstract fun doWork()

    fun request() : Boolean{
        val ir = isRequest
        if(ir){
            refresh()
            doWork()
        }
        return ir
    }
}