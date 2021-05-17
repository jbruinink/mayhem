package com.jdriven.mayhem.game

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import ninja.robbert.mayhem.api.*
import org.slf4j.LoggerFactory
import java.util.*

class MayhemProtocolHandler(
    private val strategy: GameStrategy,
    private val account: Account,
    private val resultCallback: (GameResult) -> Unit,
) : ChannelInboundHandlerAdapter() {
    private val logger = LoggerFactory.getLogger(MayhemProtocolHandler::class.java)
    private val actionQueue: Queue<Action> = LinkedList()

    private var effects = Effects(0, 0, 0, 0, 0)
    private var matchStart = 0L
    private var totalMatchTime = 0L

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
        val action = actionQueue.remove()
        logger.debug("accepted {} {}", action, msg.timestamp)

        effects += action.effects

        sendNextAction(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }

    private fun register(ctx: ChannelHandlerContext) {
        with(account) { ctx.writeAndFlush(RegisterMessage(name, email, password)) }
    }

    private fun handleError(msg: ErrorMessage, ctx: ChannelHandlerContext) {
        logger.debug("error {}: {}", actionQueue.remove(), msg.message)
        sendNextAction(ctx)
    }

    private fun sendNextAction(ctx: ChannelHandlerContext) {
        if (actionQueue.isNotEmpty()) {
            val action = actionQueue.peek()
            logger.debug("send {}", action)
            ctx.writeAndFlush(ActionMessage(action.heroId, action.skill.id, action.targetId, false))
        }
    }

    private fun handle(msg: StatusMessage, ctx: ChannelHandlerContext) {
        if(msg.status == StatusMessage.FightStatus.ready) {
            matchStart = msg.timestamp.time
        }

        if(msg.status == StatusMessage.FightStatus.finished) {
            totalMatchTime += msg.timestamp.time - matchStart
        }

        if (msg.competitionResult != null) {
            ctx.close().addListener {
                resultCallback.invoke(msg.competitionResult.first { it.name == account.name }
                    .let { GameResult(it.wins, effects, totalMatchTime.toInt()) })
            }
        } else if (actionQueue.size < 5) {
            val actions = strategy.createResponse(msg)
            if (actionQueue.isEmpty() && actions.isNotEmpty()) {
                actionQueue.addAll(actions)
                sendNextAction(ctx)
            } else {
                actionQueue.addAll(actions)
            }
        }
    }
}
