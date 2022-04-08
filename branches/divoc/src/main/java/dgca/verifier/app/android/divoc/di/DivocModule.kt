package dgca.verifier.app.android.divoc.di

import com.android.app.base.Processor
import com.android.app.base.ProcessorMarker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dgca.verifier.app.android.divoc.DivocProcessor

@InstallIn(SingletonComponent::class)
@Module
class DivocModule {

    @Provides
    @IntoSet
    @ProcessorMarker
    fun provideDivocProcessor(): Processor = DivocProcessor()
}
