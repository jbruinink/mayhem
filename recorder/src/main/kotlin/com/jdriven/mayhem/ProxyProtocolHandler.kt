package com.jdriven.mayhem

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import ninja.robbert.mayhem.api.AcceptMessage
import ninja.robbert.mayhem.api.ErrorMessage
import ninja.robbert.mayhem.api.StatusMessage
import ninja.robbert.mayhem.api.WelcomeMessage

class ProxyProtocolHandler(private val serverCtx: ChannelHandlerContext) : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is WelcomeMessage -> register(msg)
            is ErrorMessage -> handleError(msg)
            is StatusMessage -> handle(msg)
            is AcceptMessage -> handleAccepted(msg)
            else -> throw IllegalArgumentException("Unknown message type ${msg::class}")
        }
    }

    private fun handleAccepted(msg: AcceptMessage) {
        serverCtx.writeAndFlush(msg)
    }

    private fun handle(msg: StatusMessage) {
        serverCtx.writeAndFlush(msg)
    }

    private fun handleError(msg: ErrorMessage) {
        serverCtx.writeAndFlush(msg)
    }

    private fun register(msg: WelcomeMessage) {
        serverCtx.writeAndFlush(msg)
    }
}
