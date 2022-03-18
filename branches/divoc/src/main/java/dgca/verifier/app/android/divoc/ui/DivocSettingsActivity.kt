package dgca.verifier.app.android.divoc.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.app.divoc.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DivocSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_divoc_settings)
    }
}
