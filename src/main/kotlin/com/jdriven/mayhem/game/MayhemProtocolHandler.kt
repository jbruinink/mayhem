package com.jdriven.mayhem.game

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import ninja.robbert.mayhem.api.*

class MayhemProtocolHandler(
    private val strategy: GameStrategy,
    private val account: Account,
    private val resultCallback: (GameResult) -> Unit,
) : ChannelInboundHandlerAdapter() {
    private var errors = 0

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is WelcomeMessage -> register(ctx)
            is ErrorMessage -> handle(msg)
            is StatusMessage -> handle(msg, ctx)
            is AcceptMessage -> Unit
            else -> throw IllegalArgumentException("Unknown message type ${msg::class}")
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }

    private fun register(ctx: ChannelHandlerContext) {
        with(account) { ctx.writeAndFlush(RegisterMessage(name, email, password)) }
    }

    private fun handle(msg: ErrorMessage) {
        errors++
    }

    private fun handle(msg: StatusMessage, ctx: ChannelHandlerContext) {
        if (msg.competitionResult != null) {
            ctx.close().addListener { resultCallback.invoke(msg.competitionResult.first { it.name == account.name }.let { GameResult(it.wins, errors) }) }
        } else {
            strategy.handle(msg, ctx)
        }
    }
}
