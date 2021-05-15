package com.jdriven.mayhem.game

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageCodec
import ninja.robbert.mayhem.api.InputMessage
import ninja.robbert.mayhem.api.OutputMessage
import java.io.InputStream
import java.io.OutputStream

class MayhemMessageCodec(private val objectMapper: ObjectMapper) : ByteToMessageCodec<InputMessage>() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        ByteBufInputStream(buf).use {
            out.add(objectMapper.readValue(it as InputStream, OutputMessage::class.java))
        }
    }

    override fun encode(ctx: ChannelHandlerContext?, msg: InputMessage, out: ByteBuf) {
        ByteBufOutputStream(out).use {
            objectMapper.writeValue(it as OutputStream, msg)
        }
    }
}
