package dgca.verifier.app.android.icao.di

import com.android.app.base.Processor
import com.android.app.base.ProcessorMarker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dgca.verifier.app.android.icao.IcaoProcessor

@InstallIn(SingletonComponent::class)
@Module
class IcaoModule {

    @Provides
    @IntoSet
    @ProcessorMarker
    fun provideIcaoProcessor(): Processor = IcaoProcessor()
}
