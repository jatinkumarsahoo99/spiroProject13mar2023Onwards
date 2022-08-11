package com.safey.lungmonitoring.data.datautils

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor

class MyContentProvider : ContentProvider() {

      var AUTHORITY = "com.safey.lungapp..contentprovider.provider";

    // UriMatcher used to match against incoming requests

    private lateinit var uriMatcher: UriMatcher
    override fun onCreate(): Boolean {
        uriMatcher =  UriMatcher(UriMatcher.NO_MATCH);

        // Add a URI to the matcher which will match against the form

        // 'content://com.ashray.example.activity.contentprovider.provider/*'

        // and return 1 in the case that the incoming Uri matches this pattern

        uriMatcher.addURI(AUTHORITY, "*", 1);
        return true;
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        return super.openFile(uri, mode)

    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor? {
        TODO("Not yet implemented")
    }

    override fun getType(p0: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        TODO("Not yet implemented")
    }
}