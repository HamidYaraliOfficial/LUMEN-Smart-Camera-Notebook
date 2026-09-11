package com.lumen.app

import com.lumen.app.domain.model.NormalizedRect
import com.lumen.app.domain.model.OcrTextBlock
import com.lumen.app.intelligence.TableExtractionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TableExtractionEngineTest {

    private val engine = TableExtractionEngine()

    private fun block(text: String, left: Float, top: Float, right: Float, bottom: Float) = OcrTextBlock(
        id = text + left, text = text,
        boundingBox = NormalizedRect(left, top, right, bottom),
        confidence = 0.9f, languageHint = "en", lineIndex = 0, blockIndex = 0,
    )

    @Test
    fun `extracts a simple 2-column grid into rows and columns`() {
        val blocks = listOf(
            block("Name", 0.05f, 0.05f, 0.30f, 0.10f),
            block("Price", 0.50f, 0.05f, 0.70f, 0.10f),
            block("Apple", 0.05f, 0.15f, 0.30f, 0.20f),
            block("1.50", 0.50f, 0.15f, 0.70f, 0.20f),
            block("Bread", 0.05f, 0.25f, 0.30f, 0.30f),
            block("2.20", 0.50f, 0.25f, 0.70f, 0.30f),
        )

        val table = engine.extract("doc1", "Grocery table", blocks)

        assertNotNull(table)
        assertEquals(2, table!!.columns.size)
        assertEquals(2, table.rows.size) // header row promoted out of the data rows
    }

    @Test
    fun `returns null for content too small to be a table`() {
        val blocks = listOf(block("Just one line of text", 0.05f, 0.05f, 0.9f, 0.1f))
        val table = engine.extract("doc1", "Not a table", blocks)
        assertEquals(null, table)
    }
}
