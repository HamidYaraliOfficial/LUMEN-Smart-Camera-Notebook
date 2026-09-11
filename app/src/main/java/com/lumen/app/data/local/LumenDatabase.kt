package com.lumen.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lumen.app.data.local.dao.*
import com.lumen.app.data.local.entity.*

@Database(
    entities = [
        NotebookEntity::class,
        NoteEntity::class, NoteVersionEntity::class, NoteFtsEntity::class,
        DocumentEntity::class, DocumentFtsEntity::class, DocumentPageEntity::class,
        OcrResultEntity::class, OcrResultFtsEntity::class, OcrTextBlockEntity::class,
        ReceiptEntity::class, ReceiptItemEntity::class,
        DataTableEntity::class, TableColumnEntity::class, TableRowEntity::class, TableCellEntity::class,
        TaskEntity::class, ReminderEntity::class, EventEntity::class, ContactEntity::class,
        FlashcardEntity::class,
        BookmarkedCodeEntity::class, SmartClipboardEntity::class, ScanJobEntity::class, BackupRecordEntity::class,
        BusinessHoursScheduleEntity::class, DayHoursEntity::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LumenDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun noteDao(): NoteDao
    abstract fun documentDao(): DocumentDao
    abstract fun ocrDao(): OcrDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun tableDao(): TableDao
    abstract fun taskDao(): TaskDao
    abstract fun studyDao(): StudyDao
    abstract fun miscDao(): MiscDao
    abstract fun businessHoursDao(): BusinessHoursDao

    companion object {
        const val DB_NAME = "lumen_database"

        /**
         * Example of a real migration path for future versions (kept here as a template —
         * v1 has no prior version to migrate from). Add MIGRATION_1_2 etc. as the schema evolves
         * instead of relying on destructive fallback in production builds.
         */
        // val MIGRATION_1_2 = object : Migration(1, 2) {
        //     override fun migrate(db: SupportSQLiteDatabase) {
        //         db.execSQL("ALTER TABLE notes ADD COLUMN newColumn TEXT")
        //     }
        // }
    }
}
