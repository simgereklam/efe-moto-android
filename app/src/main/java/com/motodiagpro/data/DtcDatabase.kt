package com.motodiagpro.data

object DtcDatabase {
    private val codes = mapOf(
        "13" to "Pulsar bobin sinyali hatası", "14" to "Krank konum sensörü sinyali hatası",
        "15" to "Motor sıcaklık sensörü sinyali hatası", "17" to "Vuruntu sensörü hatası",
        "18" to "Gaz kelebeği konum sensörü sinyali hatası", "19" to "Akü voltajı hatası",
        "22" to "Atmosferik basınç sensörü sinyali hatası", "23" to "Emme havası sıcaklık sensörü sinyali hatası",
        "24" to "Eksantrik mili konum sensörü hatası", "25" to "Yakıt basıncı sensörü sinyali hatası",
        "26" to "Enjektör sinyali hatası", "28" to "Vites konum anahtarı sinyali hatası",
        "29" to "Emme basınç sensörü hatası", "30" to "ECU kilitleme tespiti",
        "33" to "1. silindir ateşleme bobini arızası", "34" to "2. silindir ateşleme bobini arızası",
        "39" to "Yağ basıncı sensörü hatası", "41" to "Hız sensörü sinyali yok",
        "42" to "Vites konum anahtarı açık/kısa devre", "43" to "Yakıt sistemi voltajı anormal",
        "44" to "EEPROM okuma/yazma hatası", "46" to "Şarj voltajı anormal",
        "50" to "ECU bellek arızası", "70" to "Motor rölanti durdurma hatası",
        "P0030" to "Oksijen sensörü ısıtıcı devresi arızası", "P0100" to "Hava akış sensörü devresi arızası",
        "P0107" to "MAP sensörü düşük voltaj", "P0108" to "MAP sensörü yüksek voltaj",
        "P0110" to "Emme havası sıcaklık sensörü devresi", "P0117" to "Soğutma suyu sıcaklık sensörü düşük voltaj",
        "P0118" to "Soğutma suyu sıcaklık sensörü yüksek voltaj", "P0120" to "Gaz kelebeği konum sensörü devresi",
        "P0122" to "Gaz kelebeği konum sensörü düşük voltaj", "P0130" to "Oksijen sensörü devresi",
        "P0135" to "Oksijen sensörü ısıtıcı devresi", "P0171" to "Yakıt karışımı çok fakir",
        "P0172" to "Yakıt karışımı çok zengin", "P0201" to "1. enjektör devresi",
        "P0300" to "Rastgele/çoklu silindir ateşleme hatası", "P0301" to "1. silindir ateşleme hatası",
        "P0302" to "2. silindir ateşleme hatası", "P0335" to "Krank mili konum sensörü devresi arızası",
        "P0336" to "Krank mili sensörü aralık/performans hatası", "P0340" to "Eksantrik mili konum sensörü devresi",
        "P0351" to "1. ateşleme bobini devre hatası", "P0352" to "2. ateşleme bobini devre hatası",
        "P0353" to "3. ateşleme bobini devre hatası", "P0354" to "4. ateşleme bobini devre hatası",
        "P0112" to "Emme havası sıcaklık sensörü düşük sinyal", "P0113" to "Emme havası sıcaklık sensörü yüksek sinyal",
        "P0123" to "Gaz kelebeği konum sensörü yüksek sinyal", "P0475" to "Egzoz basınç kontrol valfi devresi",
        "P0476" to "Egzoz basınç kontrol valfi aralık/performans", "P0480" to "Soğutma fanı kontrol devresi",
        "P048B" to "Egzoz basınç kontrol valfi konum sensörü devresi", "P048D" to "Egzoz basınç kontrol valfi konum sensörü düşük sinyal",
        "P048E" to "Egzoz basınç kontrol valfi konum sensörü yüksek sinyal", "P0563" to "Sistem voltajı yüksek",
        "P0606" to "ECU işlemci performans hatası", "P0420" to "Katalizör verimliliği eşik altında",
        "P0500" to "Araç hız sensörü arızası", "P0505" to "Rölanti kontrol sistemi arızası",
        "P0506" to "Rölanti devri beklenenden düşük", "P0507" to "Rölanti devri beklenenden yüksek",
        "P0560" to "Sistem voltajı arızası", "P0562" to "Sistem voltajı düşük",
        "P0600" to "Seri iletişim hattı arızası", "P0601" to "ECU bellek kontrol toplamı arızası",
        "P0700" to "Şanzıman kontrol sistemi arızası", "P1604" to "İmmobilizer kod hatası",
        "P2100" to "Gaz kelebeği aktüatör devresi", "P2101" to "Gaz kelebeği aktüatör aralık/performans",
        "P2135" to "Gaz kelebeği sensörleri korelasyon hatası", "U0100" to "Motor kontrol modülü ile iletişim kaybı",
        "U0121" to "ABS kontrol modülü ile iletişim kaybı", "U0155" to "Gösterge paneli ile iletişim kaybı"
    )

    private val priorityCodes = setOf(
        "P0335", "P0340", "P0351", "P0352", "P0107", "P0108", "P0122", "P0123",
        "P0117", "P0118", "P0112", "P0113", "P0500", "P0560", "P2135"
    )

    fun describe(code: String): String =
        codes[code.trim().uppercase()] ?: "Kod veritabanında yok — marka/model/ECU kaynağıyla doğrulanmalı."

    fun isPriority(code: String): Boolean = code.trim().uppercase() in priorityCodes
    fun priorityList(): List<String> = priorityCodes.sorted()
}
