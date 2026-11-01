package com.motodiagpro

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import com.motodiagpro.data.DtcDatabase
import com.motodiagpro.transport.ViecarWifiTransport
import com.motodiagpro.transport.ElmSession
import kotlinx.coroutines.*

class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var status: TextView
    private lateinit var output: TextView
    private val transport = ViecarWifiTransport()
    private var session: ElmSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 12, 20)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
            setBackgroundColor(Color.rgb(8, 12, 20))
        }
        fun label(s: String, size: Float = 16f) = TextView(this).apply {
            text = s; textSize = size; setTextColor(Color.WHITE)
        }
        layout.addView(label("MOTO DIAG PRO", 26f))
        layout.addView(label("Yamaha öncelikli • Gerçek cihaz modu"))
        status = label("BAĞLANTI BEKLİYOR")
        layout.addView(status)
        val connect = Button(this).apply { text = "VIECAR / ELM WI-FI BAĞLAN" }
        layout.addView(connect)
        val live = Button(this).apply { text = "CANLI VERİLERİ OKU" }
        layout.addView(live)
        val dtc = Button(this).apply { text = "ARIZA KODLARINI OKU" }
        layout.addView(dtc)
        val clear = Button(this).apply { text = "ARIZALARI SİL (ONAYLI)" }
        layout.addView(clear)
        output = label("Henüz ECU verisi alınmadı.")
        output.textSize = 14f
        layout.addView(ScrollView(this).apply { addView(output) },
            LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(layout)

        connect.setOnClickListener {
            status.text = "Viecar aranıyor: 192.168.0.10:35000"
            scope.launch {
                try {
                    val s = withContext(Dispatchers.IO) {
                        val t = transport.connect("192.168.0.10", 35000)
                        ElmSession(t).also { it.initialize() }
                    }
                    session = s
                    status.text = "ADAPTÖR BAĞLANDI • ECU yanıtı ayrıca doğrulanacak"
                    output.text = "ELM başlatma tamamlandı. ECU sorgularını deneyebilirsiniz."
                } catch (e: Exception) {
                    status.text = "BAĞLANTI HATASI"
                    output.text = e.message ?: "Bilinmeyen bağlantı hatası"
                }
            }
        }
        live.setOnClickListener {
            val s = session
            if (s == null) { output.text = "Önce adaptöre bağlanın."; return@setOnClickListener }
            scope.launch {
                try {
                    val lines = withContext(Dispatchers.IO) { s.readLiveData() }
                    output.text = lines.joinToString("\n")
                } catch (e: Exception) {
                    output.text = "Canlı veri okuma hatası: ${e.message ?: "ECU yanıt vermedi"}"
                }
            }
        }
        dtc.setOnClickListener {
            val s = session
            if (s == null) { output.text = "Önce adaptöre bağlanın."; return@setOnClickListener }
            scope.launch {
                try {
                    val codes = withContext(Dispatchers.IO) { s.readDtcs() }
                    output.text = if (codes.isEmpty()) {
                        "Standart OBD sorgusunda kod alınamadı veya kayıtlı kod bulunamadı. Bu sonuç Yamaha'nın tüm özel modüllerinin temiz olduğu anlamına gelmez."
                    } else codes.joinToString("\n\n") {
                        val tag = if (DtcDatabase.isPriority(it)) "[ÖNCELİKLİ KOD]" else "[KATALOG KODU]"
                        "$tag\n$it — ${DtcDatabase.describe(it)}"
                    }
                } catch (e: Exception) {
                    output.text = "DTC okuma hatası: ${e.message ?: "ECU yanıt vermedi"}"
                }
            }
        }
        clear.setOnClickListener {
            val s = session
            if (s == null) { output.text = "Önce adaptöre bağlanın."; return@setOnClickListener }
            AlertDialog.Builder(this)
                .setTitle("Arıza kodlarını sil?")
                .setMessage("Bu işlem ECU'daki kayıtlı arıza kodlarını silebilir. Devam edilsin mi?")
                .setNegativeButton("Vazgeç", null)
                .setPositiveButton("Sil") { _, _ ->
                    scope.launch {
                        try {
                            output.text = withContext(Dispatchers.IO) { s.clearDtcsAndVerify() }
                        } catch (e: Exception) {
                            output.text = "Silme/doğrulama hatası: ${e.message ?: "ECU yanıt vermedi"}"
                        }
                    }
                }.show()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        session?.close()
        super.onDestroy()
    }
}
