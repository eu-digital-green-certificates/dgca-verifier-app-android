package dgca.verifier.app.android.divoc.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.app.divoc.R

class DivocActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_divoc_main)

        intent.getStringExtra("result_key")?.let {
            findViewById<TextView>(R.id.textView).text = it
        }
    }
}
