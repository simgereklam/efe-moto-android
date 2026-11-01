package com.motodiagpro.transport

class ElmSession(private val io: ByteTransport) {
    fun initialize() {
        listOf("ATZ", "ATE0", "ATL0", "ATS0", "ATH0", "ATSP0").forEach {
            val r = io.command(it, 4000)
            if (r.contains("UNABLE", true) || r.contains("ERROR", true))
                error("ELM başlatma başarısız ($it): $r")
        }
    }

    private fun pid(cmd: String): String = io.command(cmd)

    fun readLiveData(): List<String> {
        val result = mutableListOf<String>()
        fun read(cmd: String, name: String, decode: (String) -> String) {
            val raw = pid(cmd).replace(" ", "").uppercase()
            val payload = raw.substringAfter("41" + cmd.takeLast(2).uppercase(), "")
            if (payload.length >= 2 && payload.take(2).all { it in "0123456789ABCDEF" })
                result += "$name: ${decode(payload)}  [ECU yanıtı]"
            else result += "$name: Desteklenmiyor / yanıt alınamadı"
        }
        read("010C", "Motor devri") { p ->
            val n = p.take(4).toInt(16); "${(n / 4.0).toInt()} rpm"
        }
        read("0111", "Gaz kelebeği") { p ->
            val n = p.take(2).toInt(16); "%.1f%%".format(n * 100.0 / 255.0)
        }
        read("0105", "Soğutma suyu sıcaklığı") { p ->
            "${p.take(2).toInt(16) - 40} °C"
        }
        read("010B", "Emme manifoldu basıncı") { p ->
            "${p.take(2).toInt(16)} kPa"
        }
        read("0142", "ECU kontrol modülü voltajı") { p ->
            val n = p.take(4).toInt(16); "%.2f V".format(n / 1000.0)
        }
        return result
    }

    fun readDtcs(): List<String> {
        val raw = io.command("03", 5000).replace(" ", "").uppercase()
        if (raw.contains("NODATA") || raw.contains("UNABLE") || raw.contains("ERROR")) return emptyList()
        val data = raw.substringAfter("43", "")
        if (data.length < 4) return emptyList()
        val codes = mutableListOf<String>()
        var i = 0
        while (i + 4 <= data.length) {
            val chunk = data.substring(i, i + 4)
            if (chunk == "0000") { i += 4; continue }
            if (!chunk.all { it in "0123456789ABCDEF" }) break
            val a = chunk.substring(0, 2).toInt(16)
            val b = chunk.substring(2, 4).toInt(16)
            val first = listOf('P', 'C', 'B', 'U')[(a shr 6) and 0x03]
            val digit = (a shr 4) and 0x03
            codes += "$first$digit%X%02X".format(a and 0x0F, b)
            i += 4
        }
        return codes
    }

    fun clearDtcsAndVerify(): String {
        val response = io.command("04", 5000).replace(" ", "").uppercase()
        if (!response.contains("44") || response.contains("ERROR") || response.contains("UNABLE"))
            return "Silme işlemi ECU tarafından doğrulanmadı. Yanıt: $response"
        Thread.sleep(1200)
        val verify = io.command("03", 5000).replace(" ", "").uppercase()
        if (!verify.contains("43"))
            return "Silme komutu yanıtlandı ancak doğrulama sorgusu geçerli 43 yanıtı vermedi. Kesin başarı denemez. Yanıt: $verify"
        val remaining = decodeDtcResponse(verify)
        return if (remaining.isEmpty())
            "Standart OBD DTC silme doğrulandı: 43 yanıtında kayıtlı kod yok. Üreticiye özel modüller ayrıca taranmalıdır."
        else "Silme doğrulanmadı. Kalan kodlar: ${remaining.joinToString()}"
    }

    private fun decodeDtcResponse(raw: String): List<String> {
        val data = raw.substringAfter("43", "")
        if (data.isEmpty()) return emptyList()
        val codes = mutableListOf<String>()
        var i = 0
        while (i + 4 <= data.length) {
            val chunk = data.substring(i, i + 4)
            if (chunk == "0000") { i += 4; continue }
            if (!chunk.all { it in "0123456789ABCDEF" }) break
            val a = chunk.substring(0, 2).toInt(16)
            val b = chunk.substring(2, 4).toInt(16)
            val first = listOf('P', 'C', 'B', 'U')[(a shr 6) and 0x03]
            val digit = (a shr 4) and 0x03
            codes += "$first$digit%X%02X".format(a and 0x0F, b)
            i += 4
        }
        return codes
    }

    fun close() = io.close()
}
