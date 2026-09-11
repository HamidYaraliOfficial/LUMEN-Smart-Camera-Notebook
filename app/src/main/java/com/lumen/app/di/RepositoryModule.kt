package com.lumen.app.di

import com.lumen.app.data.repository.*
import com.lumen.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton abstract fun bindNotebookRepository(impl: NotebookRepositoryImpl): NotebookRepository
    @Binds @Singleton abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
    @Binds @Singleton abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository
    @Binds @Singleton abstract fun bindOcrRepository(impl: OcrRepositoryImpl): OcrRepository
    @Binds @Singleton abstract fun bindReceiptRepository(impl: ReceiptRepositoryImpl): ReceiptRepository
    @Binds @Singleton abstract fun bindTableRepository(impl: TableRepositoryImpl): TableRepository
    @Binds @Singleton abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
    @Binds @Singleton abstract fun bindStudyRepository(impl: StudyRepositoryImpl): StudyRepository
    @Binds @Singleton abstract fun bindBusinessHoursRepository(impl: BusinessHoursRepositoryImpl): BusinessHoursRepository
    @Binds @Singleton abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository
}
