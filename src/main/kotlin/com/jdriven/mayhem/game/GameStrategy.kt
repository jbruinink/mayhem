package com.jdriven.mayhem.game

import io.netty.channel.ChannelHandlerContext
import ninja.robbert.mayhem.api.*

interface GameStrategy {
    fun handle(msg: StatusMessage, ctx: ChannelHandlerContext)
}