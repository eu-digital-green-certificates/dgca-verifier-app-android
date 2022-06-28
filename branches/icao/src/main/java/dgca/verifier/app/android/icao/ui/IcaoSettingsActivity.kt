package dgca.verifier.app.android.icao.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.app.icao.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IcaoSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_icao_settings)
    }
}
