package com.jdriven.mayhem

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

class MayhemServerMessageCodec(private val objectMapper: ObjectMapper) : ByteToMessageCodec<OutputMessage>() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        ByteBufInputStream(buf).use {
            out.add(objectMapper.readValue(it as InputStream, InputMessage::class.java))
        }
    }

    override fun encode(ctx: ChannelHandlerContext?, msg: OutputMessage, out: ByteBuf) {
        ByteBufOutputStream(out).use {
            objectMapper.writeValue(it as OutputStream, msg)
        }
    }
}
