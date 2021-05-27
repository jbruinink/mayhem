package com.jdriven.mayhem.game

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import ninja.robbert.mayhem.api.*
import org.slf4j.LoggerFactory

class MayhemProtocolHandler(
    private val strategy: GameStrategy,
    private val account: Account,
    private val resultCallback: (GameResult) -> Unit,
) : ChannelInboundHandlerAdapter() {
    private val logger = LoggerFactory.getLogger(MayhemProtocolHandler::class.java)

    private var matchStart = 0L
    private var totalMatchTime = 0L
    private var kills = 0
    private var healthDifference = 0
    private var matchesPlayed = 0
    private var matchesWon = 0

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        when (msg) {
            is WelcomeMessage -> register(ctx)
            is ErrorMessage -> handleError(msg, ctx)
            is StatusMessage -> handle(msg, ctx)
            is AcceptMessage -> handleAccepted(msg, ctx)
            else -> throw IllegalArgumentException("Unknown message type ${msg::class}")
        }
    }

    private fun handleAccepted(msg: AcceptMessage, ctx: ChannelHandlerContext) {
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }

    private fun register(ctx: ChannelHandlerContext) {
        with(account) { ctx.writeAndFlush(RegisterMessage(name, email, password)) }
    }

    private fun handleError(msg: ErrorMessage, ctx: ChannelHandlerContext) {
    }

    private fun handle(msg: StatusMessage, ctx: ChannelHandlerContext) {
        if (msg.status == StatusMessage.FightStatus.ready) {
            matchStart = msg.timestamp.time
        }

        if (msg.status == StatusMessage.FightStatus.finished) {
            totalMatchTime += msg.timestamp.time - matchStart
            matchesPlayed++
            kills += msg.opponent.count { !it.isAlive }
            healthDifference += msg.you.sumOf { it.health } - msg.opponent.sumOf { it.health}
            if (msg.result == StatusMessage.FightResult.win) {
                matchesWon++
            }
        }

        if (msg.competitionResult != null) {
            ctx.close().addListener {
                resultCallback.invoke(GameResult(matchesWon, healthDifference, totalMatchTime.toInt()))
            }
        } else {
            strategy.createResponse(msg).forEach {
                ctx.writeAndFlush(ActionMessage(it.heroId, it.skill.id, it.targetId, false))
            }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.close().addListener {
            resultCallback.invoke(GameResult(matchesWon, kills, totalMatchTime.toInt()))
        }
    }
}
