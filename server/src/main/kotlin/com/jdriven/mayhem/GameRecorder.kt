package com.jdriven.mayhem

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import ninja.robbert.mayhem.api.ActionMessage
import ninja.robbert.mayhem.api.InputMessage
import ninja.robbert.mayhem.api.OutputMessage
import ninja.robbert.mayhem.api.StatusMessage
import java.io.FileWriter
import java.io.PrintWriter

class GameRecorder(private val objectMapper: ObjectMapper) : MessageToMessageCodec<InputMessage, OutputMessage>() {
    private val writer = PrintWriter(FileWriter("recorded-${System.currentTimeMillis()}.json"))

    private var statusMessage: StatusMessage? = null
    private val actionMessages: MutableList<ActionMessage> = mutableListOf()

    override fun encode(ctx: ChannelHandlerContext?, msg: OutputMessage, out: MutableList<Any>) {
        if (msg is StatusMessage &&
            (msg.status == StatusMessage.FightStatus.fighting || msg.status == StatusMessage.FightStatus.overtime)
        ) {
            statusMessage?.let {
                writer.println(objectMapper.writeValueAsString(RequestResponse(it, actionMessages)))
            }
            actionMessages.clear()
            statusMessage = msg
        }
        out.add(msg)
    }

    override fun decode(ctx: ChannelHandlerContext?, msg: InputMessage, out: MutableList<Any>) {
        if (msg is ActionMessage) {
            actionMessages.add(msg)
        }
        out.add(msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        writer.close()
    }
}

data class RequestResponse(val statusMessage: StatusMessage, val actionMessages: MutableList<ActionMessage>)
