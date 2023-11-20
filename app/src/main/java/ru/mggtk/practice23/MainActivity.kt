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
    private var flag: Boolean = true
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job // Изменение на главный поток

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonGenerate = findViewById<Button>(R.id.button2)
        val buttonCalculate = findViewById<Button>(R.id.button)
        buttonCalculate.setOnClickListener {
            launch {
                val loadingProgressBar = findViewById<ProgressBar>(R.id.progressBar)
                flag = false
                loadingProgressBar.visibility = View.VISIBLE // Обновление UI в главном потоке
                calculateHash(flag)
                loadingProgressBar.visibility = View.INVISIBLE // Обновление UI в главном потоке
            }
        }
        buttonGenerate.setOnClickListener {
            launch {
                val loadingProgressBar = findViewById<ProgressBar>(R.id.progressBar)
                flag = true
                loadingProgressBar.visibility = View.VISIBLE
                calculateHash(flag)
                loadingProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    suspend fun calculateHash(_flag: Boolean) {
        if(_flag == false){
            val enter: EditText = findViewById(R.id.editTextNumber)
            val num = enter.text.toString().toInt()
            val array = byteArrayOf(num.toByte())
            val result = crc16(array)
            withContext(Dispatchers.Main) {
                val tv: TextView = findViewById(R.id.textView)
                val resultHex = result.toHex()
                Log.d("Hue","${result.joinToString("")}")
                tv.setText("$resultHex")
            }
        }
        else{
            val randomData = generateRandomData()
            val result = crc16(randomData)
            withContext(Dispatchers.Main){
                val tv:TextView = findViewById(R.id.textView)
                val resultHex = result.toHex()
                Log.d("Hue","${result.joinToString(",")}")
                tv.setText("$resultHex")
            }
        }

    }
    /*suspend fun calculateHash(_flag: Boolean) {
        val enter: EditText = findViewById(R.id.editTextNumber)
        val num = enter.text.toString().toInt()
        val array = byteArrayOf(num.toByte())
        val result = crc16(array)
        withContext(Dispatchers.Main) {
            val tv: TextView = findViewById(R.id.textView)
            val resultHex = result.toHex()
            val resultString = result.joinToString(",")
            Log.d("Hue","$resultString")
            tv.setText("$resultHex")
        }
    }*/

    fun generateRandomData(): ByteArray {
        val dataSize = 16777216 // Размер данных в байтах (16 МБ)
        return ByteArray(dataSize) { _ -> (0..255).random().toByte() }
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
        delay(1000)
        Log.d("Hue","${crc}")
        return crc.to2ByteArray()
    }

    fun Int.to2ByteArray(): ByteArray = byteArrayOf(toByte(), shr(8).toByte())

    fun ByteArray.toHex(): String = joinToString("") { eachByte ->
        "%02x".format(eachByte)
    }
}