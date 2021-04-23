package dgca.verifier.app.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DgcaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}