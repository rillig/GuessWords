package de.roland_illig.guesswords

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException

class ExportContentProvider : ContentProvider() {

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        .also { it.addURI("de.roland_illig.guesswords", "*", 12345) }

    override fun onCreate() = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = "text/csv; charset=UTF-16LE"

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        uriMatcher.match(uri) == 12345 || throw FileNotFoundException("$uri")
        val location = File("${context!!.cacheDir}", uri.lastPathSegment)
        return ParcelFileDescriptor.open(location, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        TODO("not implemented")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = TODO("not implemented")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        TODO("not implemented")
}
