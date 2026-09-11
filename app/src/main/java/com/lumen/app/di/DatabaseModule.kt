package com.lumen.app.di

import android.content.Context
import androidx.room.Room
import com.lumen.app.data.local.LumenDatabase
import com.lumen.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LumenDatabase =
        Room.databaseBuilder(context, LumenDatabase::class.java, LumenDatabase.DB_NAME)
            // .addMigrations(LumenDatabase.MIGRATION_1_2) // add as schema evolves
            .build()

    @Provides fun provideNotebookDao(db: LumenDatabase): NotebookDao = db.notebookDao()
    @Provides fun provideNoteDao(db: LumenDatabase): NoteDao = db.noteDao()
    @Provides fun provideDocumentDao(db: LumenDatabase): DocumentDao = db.documentDao()
    @Provides fun provideOcrDao(db: LumenDatabase): OcrDao = db.ocrDao()
    @Provides fun provideReceiptDao(db: LumenDatabase): ReceiptDao = db.receiptDao()
    @Provides fun provideTableDao(db: LumenDatabase): TableDao = db.tableDao()
    @Provides fun provideTaskDao(db: LumenDatabase): TaskDao = db.taskDao()
    @Provides fun provideStudyDao(db: LumenDatabase): StudyDao = db.studyDao()
    @Provides fun provideMiscDao(db: LumenDatabase): MiscDao = db.miscDao()
    @Provides fun provideBusinessHoursDao(db: LumenDatabase): BusinessHoursDao = db.businessHoursDao()
}
