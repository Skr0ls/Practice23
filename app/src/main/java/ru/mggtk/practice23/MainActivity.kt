package ru.mggtk.practice23

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job // Изменение на главный поток

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonCalculate = findViewById<Button>(R.id.button)
        buttonCalculate.setOnClickListener {
            launch {
                val loadingProgressBar = findViewById<ProgressBar>(R.id.progressBar)
                loadingProgressBar.visibility = View.VISIBLE // Обновление UI в главном потоке
                calculateHash()
                loadingProgressBar.visibility = View.INVISIBLE // Обновление UI в главном потоке
            }
        }
    }

    suspend fun calculateHash() {
        val enter: EditText = findViewById(R.id.editTextNumber)
        val num = enter.text.toString().toInt()
        val array = byteArrayOf(num.toByte())
        val result = crc16(array)
        withContext(Dispatchers.Main) {
            val tv: TextView = findViewById(R.id.textView)
            val resultHex = result.toHex()
            val resultString = result.joinToString(",")
            Log.d("Hue","$resultString")
            tv.setText("$resultHex; $resultString")
        }
    }

    suspend fun crc16(byteArray: ByteArray): ByteArray {
        var crc = 0xffff
        byteArray.forEach { byte ->
            crc = (crc ushr 8 or crc shl 8) and 0xffff
            crc = crc xor (byte.toInt() and 0xff)
            crc = crc xor ((crc and 0xff) shr 4)
            crc = crc xor ((crc shl 12) and 0xffff)
            crc = crc xor (((crc and 0xff) shl 5) and 0xffff)
        }
        crc = crc and 0xffff
        delay(2500)
        Log.d("Hue","${crc}")
        return crc.to2ByteArray()
    }

    fun Int.to2ByteArray(): ByteArray = byteArrayOf(toByte(), shr(8).toByte())

    fun ByteArray.toHex(): String = joinToString("") { eachByte ->
        "%02x".format(eachByte)
    }
}