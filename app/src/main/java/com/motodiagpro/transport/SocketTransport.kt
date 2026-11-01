package com.motodiagpro.transport

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

interface ByteTransport {
    fun command(text: String, timeoutMs: Int = 2500): String
    fun close()
}

class ViecarWifiTransport : ByteTransport {
    private var socket: Socket? = null
    private var input: BufferedReader? = null
    private var output: OutputStream? = null

    fun connect(host: String, port: Int): ViecarWifiTransport {
        val s = Socket()
        s.connect(java.net.InetSocketAddress(host, port), 4000)
        s.soTimeout = 2500
        socket = s
        input = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.US_ASCII))
        output = s.getOutputStream()
        return this
    }

    @Synchronized
    override fun command(text: String, timeoutMs: Int): String {
        val s = socket ?: error("Adaptör bağlı değil")
        s.soTimeout = timeoutMs
        val out = output ?: error("Çıkış akışı yok")
        val reader = input ?: error("Giriş akışı yok")
        while (reader.ready()) reader.read()
        out.write((text.trim() + "\r").toByteArray(Charsets.US_ASCII))
        out.flush()
        val result = StringBuilder()
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (reader.ready()) {
                val ch = reader.read()
                if (ch < 0) break
                if (ch == '>') break
                if (ch != '\r' && ch != '\n') result.append(ch.toChar())
            } else {
                Thread.sleep(20)
            }
        }
        return result.toString().trim()
    }

    override fun close() { try { socket?.close() } catch (_: Exception) {} }
}
