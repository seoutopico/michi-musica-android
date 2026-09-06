package com.ainalluna.michimusica.validation

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsProvider
import java.io.File
import java.io.FileNotFoundException

/** SAF fault provider confined to the validation application and its own private files. */
class RegressionDocuments : DocumentsProvider() {
    companion object {
        @Volatile var deny = false
        @Volatile var full = false
        @Volatile var pauseRename = false
        @Volatile var renameEntered = false
    }
    private val root get() = File(requireNotNull(context).filesDir, "regression-audio").apply { mkdirs() }
    private fun file(id: String): File {
        if (deny) throw SecurityException("Simulated revoked SAF grant")
        val result = if (id == "root") root else File(root, id)
        require(result.canonicalFile == root.canonicalFile || result.canonicalFile.parentFile == root.canonicalFile)
        return result
    }
    override fun onCreate() = true
    override fun queryRoots(projection: Array<out String>?): Cursor = MatrixCursor(projection ?: emptyArray())
    private fun cursor(projection: Array<out String>?) = MatrixCursor(projection ?: arrayOf(
        Document.COLUMN_DOCUMENT_ID, Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE,
        Document.COLUMN_FLAGS, Document.COLUMN_SIZE, Document.COLUMN_LAST_MODIFIED))
    private fun add(cursor: MatrixCursor, id: String) {
        val f = file(id)
        val values = mapOf<String, Any>(Document.COLUMN_DOCUMENT_ID to id,
            Document.COLUMN_DISPLAY_NAME to f.name,
            Document.COLUMN_MIME_TYPE to if (f.isDirectory) Document.MIME_TYPE_DIR else "audio/wav",
            Document.COLUMN_FLAGS to if (f.isDirectory) Document.FLAG_DIR_SUPPORTS_CREATE else
                (Document.FLAG_SUPPORTS_WRITE or Document.FLAG_SUPPORTS_DELETE or Document.FLAG_SUPPORTS_RENAME),
            Document.COLUMN_SIZE to f.length(), Document.COLUMN_LAST_MODIFIED to f.lastModified())
        cursor.addRow(cursor.columnNames.map { values[it] }.toTypedArray())
    }
    override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor = cursor(projection).also { if (file(documentId).exists()) add(it, documentId) }
    override fun queryChildDocuments(parentDocumentId: String, projection: Array<out String>?, sortOrder: String?): Cursor = cursor(projection).also { c -> file(parentDocumentId).listFiles().orEmpty().forEach { add(c, it.name) } }
    override fun getDocumentType(documentId: String) = if (file(documentId).isDirectory) Document.MIME_TYPE_DIR else "audio/wav"
    override fun isChildDocument(parentDocumentId: String, documentId: String) = file(documentId).parentFile == file(parentDocumentId)
    override fun createDocument(parentDocumentId: String, mimeType: String, displayName: String): String {
        if (full) throw FileNotFoundException("ENOSPC: simulated full provider")
        require(file(parentDocumentId) == root)
        check(file(displayName).createNewFile())
        return displayName
    }
    override fun renameDocument(documentId: String, displayName: String): String {
        if (pauseRename) { renameEntered = true; Thread.sleep(500) }
        check(file(documentId).renameTo(file(displayName)))
        return displayName
    }
    override fun deleteDocument(documentId: String) { check(file(documentId).delete()) }
    override fun openDocument(documentId: String, mode: String, signal: CancellationSignal?): ParcelFileDescriptor = ParcelFileDescriptor.open(file(documentId), ParcelFileDescriptor.parseMode(mode))
}
