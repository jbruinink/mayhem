package com.jdriven.mayhem.game

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

class LineBasedFrameEncoder: MessageToMessageEncoder<ByteBuf>() {
    private val newline = "\r\n".toByteArray()
    override fun acceptOutboundMessage(msg: Any?): Boolean {
        return super.acceptOutboundMessage(msg)
    }

    override fun encode(ctx: ChannelHandlerContext?, msg: ByteBuf, out: MutableList<Any>) {
        out.add(msg.retain().writeBytes(newline))
    }
}
