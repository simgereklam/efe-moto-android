package com.motodiagpro.transport

data class AdapterIdentity(
    val manufacturer: String,
    val model: String,
    val transport: String,
    val firmware: String? = null
)

interface AdapterProvider {
    val id: String
    fun detect(): List<AdapterIdentity>
    fun connect(identity: AdapterIdentity): ByteTransport
}

interface AndroidJ2534Provider : AdapterProvider
