package com.lumen.app.intelligence

import com.lumen.app.domain.model.LumenContact
import com.lumen.app.domain.model.OcrResult
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Real regex-based Business Card parser: pulls email/phone/URL deterministically, and guesses
 * name/company/title from line position and formatting — always subject to user confirmation
 * before being written to the OS Contacts provider (see ContactSaveUseCase). */
@Singleton
class BusinessCardParsingEngine @Inject constructor() {

    private val emailRegex = Regex("[\\w.+-]+@[\\w-]+\\.[\\w.-]+")
    private val phoneRegex = Regex("(\\+?\\d[\\d\\s().-]{6,}\\d)")
    private val urlRegex = Regex("((https?://)?(www\\.)?[\\w-]+\\.[a-zA-Z]{2,}(/\\S*)?)")

    fun parse(sourceDocumentId: String, ocr: OcrResult): LumenContact {
        val fullText = ocr.fullText
        val lines = fullText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val email = emailRegex.find(fullText)?.value
        val phone = phoneRegex.find(fullText)?.value?.trim()
        val website = urlRegex.find(fullText.replace(email ?: "", ""))?.value

        // Name heuristic: first line that isn't the email/phone/website and has 2-4 words, capitalised.
        val name = lines.firstOrNull { line ->
            line != email && line != phone && line != website &&
                line.split(" ").size in 2..4 &&
                line.none { it.isDigit() }
        }

        // Job title heuristic: a short line right after the name.
        val nameIndex = lines.indexOf(name)
        val jobTitle = if (nameIndex in 0 until lines.size - 1) {
            lines[nameIndex + 1].takeIf { it.length in 3..40 && it != email && it != phone }
        } else null

        // Company heuristic: an all-caps or title-case short line elsewhere in the card.
        val company = lines.firstOrNull { line ->
            line != name && line != jobTitle && line != email && line != phone && line != website &&
                (line == line.uppercase() && line.any { it.isLetter() })
        }

        val addressLine = lines.lastOrNull { line ->
            line != name && line != jobTitle && line != company && line != email && line != phone && line != website &&
                (line.any { it.isDigit() } && line.length > 8)
        }

        return LumenContact(
            id = UUID.randomUUID().toString(),
            name = name,
            company = company,
            jobTitle = jobTitle,
            phone = phone,
            email = email,
            website = website,
            address = addressLine,
            sourceDocumentId = sourceDocumentId,
            savedToContacts = false,
        )
    }
}
