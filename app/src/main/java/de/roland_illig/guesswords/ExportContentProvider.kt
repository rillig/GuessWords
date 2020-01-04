package de.roland_illig.guesswords

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException

class ExportContentProvider : ContentProvider() {

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).also {
        it.addURI("de.roland_illig.guesswords", "*", 1)
    }

    override fun onCreate() = true

    override fun query(
        uri: Uri?,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ) = null

    override fun getType(uri: Uri?) = "text/csv; charset=UTF-8"

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (uriMatcher.match(uri) != 1) {
            throw FileNotFoundException(uri.toString())
        }
        val location = File(context.cacheDir.toString(), uri.lastPathSegment)
        return ParcelFileDescriptor.open(location, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri?, values: ContentValues?) = TODO("not implemented")

    override fun update(
        uri: Uri?,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ) = TODO("not implemented")

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?) =
        TODO("not implemented")
}
