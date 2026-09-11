package com.lumen.app.data.repository

import com.lumen.app.data.local.entity.*
import com.lumen.app.domain.model.*

fun NotebookEntity.toDomain(itemCount: Int = 0) = Notebook(
    id = id, name = name, colorHex = colorHex, icon = icon,
    isPinned = isPinned, isArchived = isArchived, isLocked = isLocked,
    createdAtEpochMs = createdAtEpochMs, updatedAtEpochMs = updatedAtEpochMs, itemCount = itemCount,
)

fun Notebook.toEntity() = NotebookEntity(
    id = id, name = name, colorHex = colorHex, icon = icon,
    isPinned = isPinned, isArchived = isArchived, isLocked = isLocked,
    createdAtEpochMs = createdAtEpochMs, updatedAtEpochMs = updatedAtEpochMs,
)

fun NoteEntity.toDomain() = Note(
    id = id, notebookId = notebookId, title = title, bodyMarkdown = bodyMarkdown,
    ocrResultId = ocrResultId, sourceDocumentId = sourceDocumentId, tags = tags,
    attachments = emptyList(), isFavorite = isFavorite, isArchived = isArchived, isPinned = isPinned,
    version = version, createdAtEpochMs = createdAtEpochMs, updatedAtEpochMs = updatedAtEpochMs,
)

fun Note.toEntity() = NoteEntity(
    id = id, notebookId = notebookId, title = title, bodyMarkdown = bodyMarkdown,
    ocrResultId = ocrResultId, sourceDocumentId = sourceDocumentId, tags = tags,
    isFavorite = isFavorite, isArchived = isArchived, isPinned = isPinned,
    version = version, createdAtEpochMs = createdAtEpochMs, updatedAtEpochMs = updatedAtEpochMs,
)

fun NoteVersionEntity.toDomain() = NoteVersion(id, noteId, versionNumber, bodyMarkdownSnapshot, editedAtEpochMs)

fun DocumentEntity.toDomain(pages: List<DocumentPage> = emptyList()) = LumenDocument(
    id = id, notebookId = notebookId, title = title, scanMode = runCatching { ScanMode.valueOf(scanMode) }.getOrDefault(ScanMode.DOCUMENT),
    pages = pages, summaryShort = summaryShort, summaryMedium = summaryMedium, summaryDeep = summaryDeep,
    keywords = keywords, tags = tags, pdfUri = pdfUri, isLocked = isLocked,
    createdAtEpochMs = createdAtEpochMs, updatedAtEpochMs = updatedAtEpochMs,
)

fun LumenDocument.toEntity() = DocumentEntity(
    id = id, notebookId = notebookId, title = title, scanMode = scanMode.name,
    summaryShort = summaryShort, summaryMedium = summaryMedium, summaryDeep = summaryDeep,
    keywords = keywords, tags = tags, pdfUri = pdfUri, isLocked = isLocked,
    createdAtEpochMs = createdAtEpochMs, updatedAtEpochMs = updatedAtEpochMs,
)

fun DocumentPageEntity.toDomain() = DocumentPage(id, pageIndex, originalImageUri, processedImageUri, ocrResultId, rotationDegrees)
fun DocumentPage.toEntity(documentId: String) = DocumentPageEntity(id, documentId, pageIndex, originalImageUri, processedImageUri, ocrResultId, rotationDegrees)

fun OcrResultEntity.toDomain(blocks: List<OcrTextBlock>) = OcrResult(
    id = id, sourceImageId = sourceImageId, fullText = fullText, blocks = blocks,
    detectedLanguage = detectedLanguage, averageConfidence = averageConfidence, createdAtEpochMs = createdAtEpochMs,
)

fun OcrTextBlockEntity.toDomain() = OcrTextBlock(
    id = id, text = text, boundingBox = NormalizedRect(left, top, right, bottom),
    confidence = confidence, languageHint = languageHint, lineIndex = lineIndex, blockIndex = blockIndex,
)

fun OcrTextBlock.toEntity(ocrResultId: String) = OcrTextBlockEntity(
    id = id, ocrResultId = ocrResultId, text = text,
    left = boundingBox.left, top = boundingBox.top, right = boundingBox.right, bottom = boundingBox.bottom,
    confidence = confidence, languageHint = languageHint, lineIndex = lineIndex, blockIndex = blockIndex,
)

fun ExtractedField.toValuePair() = key to value

fun ReceiptEntity.toDomain(items: List<ReceiptItem>) = Receipt(
    id = id, notebookId = notebookId, sourceDocumentId = sourceDocumentId,
    merchant = merchantValue?.let { ExtractedField("merchant", it, merchantConfidence ?: 0f, null) },
    date = dateValue?.let { ExtractedField("date", it, dateConfidence ?: 0f, null) },
    total = totalValue?.let { ExtractedField("total", it, totalConfidence ?: 0f, null) },
    tax = taxValue?.let { ExtractedField("tax", it, taxConfidence ?: 0f, null) },
    currency = currencyValue?.let { ExtractedField("currency", it, 1f, null) },
    paymentMethod = paymentMethodValue?.let { ExtractedField("paymentMethod", it, 1f, null) },
    items = items, category = category, tags = tags, createdAtEpochMs = createdAtEpochMs,
)

fun Receipt.toEntity() = ReceiptEntity(
    id = id, notebookId = notebookId, sourceDocumentId = sourceDocumentId,
    merchantValue = merchant?.value, merchantConfidence = merchant?.confidence,
    dateValue = date?.value, dateConfidence = date?.confidence,
    totalValue = total?.value, totalConfidence = total?.confidence,
    taxValue = tax?.value, taxConfidence = tax?.confidence,
    currencyValue = currency?.value, paymentMethodValue = paymentMethod?.value,
    category = category, tags = tags, createdAtEpochMs = createdAtEpochMs,
)

fun ReceiptItemEntity.toDomain() = ReceiptItem(id, name, quantity, unitPrice, lineTotal, confidence)
fun ReceiptItem.toEntity(receiptId: String) = ReceiptItemEntity(id, receiptId, name, quantity, unitPrice, lineTotal, confidence)

fun FlashcardEntity.toDomain() = Flashcard(id, notebookId, sourceDocumentId, question, answer, tags, timesReviewed, lastReviewedAtEpochMs, createdAtEpochMs)
fun Flashcard.toEntity() = FlashcardEntity(id, notebookId, sourceDocumentId, question, answer, tags, timesReviewed, lastReviewedAtEpochMs, createdAtEpochMs)

fun BusinessHoursScheduleEntity.toDomain(days: List<DayHours>) = BusinessHoursSchedule(id, label, timeZoneId, days, updatedAtEpochMs)
fun BusinessHoursSchedule.toEntity() = BusinessHoursScheduleEntity(id, label, timeZoneId, updatedAtEpochMs)
fun DayHoursEntity.toDomain() = DayHours(dayOfWeek, isOpen, openMinuteOfDay, closeMinuteOfDay)
fun DayHours.toEntity(scheduleId: String) = DayHoursEntity(scheduleId = scheduleId, dayOfWeek = dayOfWeek, isOpen = isOpen, openMinuteOfDay = openMinuteOfDay, closeMinuteOfDay = closeMinuteOfDay)
