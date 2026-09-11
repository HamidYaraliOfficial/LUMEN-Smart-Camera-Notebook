package com.lumen.app.di

import com.lumen.app.intelligence.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {
    // Note: engines with an @Inject constructor (TableExtractionEngine, ReceiptParsingEngine,
    // BusinessCardParsingEngine, DateTimeExtractionEngine, SummarizationEngine,
    // FlashcardGenerationEngine, AiCleanupEngine, TranslationEngine, OnDeviceTranslationProvider)
    // are provided automatically by Hilt and must NOT be re-declared here (that would create a
    // duplicate-binding compile error). Only plain classes without an @Inject constructor need
    // an explicit @Provides:
    @Provides @Singleton fun provideSmartClassificationEngine() = SmartClassificationEngine()
    @Provides @Singleton fun provideFormFieldExtractionEngine() = FormFieldExtractionEngine()
    @Provides @Singleton fun provideMathFormulaRecognitionEngine() = MathFormulaRecognitionEngine()
}
