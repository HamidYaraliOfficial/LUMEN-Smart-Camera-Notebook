package com.lumen.app.core.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Scan : Screen("scan")
    data object NotebookList : Screen("notebooks")
    data object NotebookDetail : Screen("notebooks/{notebookId}") {
        fun buildRoute(notebookId: String) = "notebooks/$notebookId"
    }
    data object NoteDetail : Screen("notes/{noteId}") {
        fun buildRoute(noteId: String) = "notes/$noteId"
    }
    data object DocumentDetail : Screen("documents/{documentId}") {
        fun buildRoute(documentId: String) = "documents/$documentId"
    }
    data object ReceiptDetail : Screen("receipts/{receiptId}") {
        fun buildRoute(receiptId: String) = "receipts/$receiptId"
    }
    data object TableDetail : Screen("tables/{tableId}") {
        fun buildRoute(tableId: String) = "tables/$tableId"
    }
    data object Search : Screen("search")
    data object Settings : Screen("settings")
    data object BusinessHours : Screen("settings/business_hours")
    data object AiAssistant : Screen("assistant/{documentId}") {
        fun buildRoute(documentId: String) = "assistant/$documentId"
    }
}
