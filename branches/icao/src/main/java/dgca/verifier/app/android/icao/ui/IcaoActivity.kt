package dgca.verifier.app.android.icao.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.app.icao.R

class IcaoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icao_main)

        intent.getStringExtra("result_key")?.let {
            findViewById<TextView>(R.id.textView).text = it
        }
    }
}
