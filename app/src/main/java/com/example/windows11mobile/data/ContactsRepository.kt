package com.example.windows11mobile.data

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

data class Contact(
    val id: String,
    val name: String,
    val photoUri: String?,
    val lookupKey: String?,
    val isStarred: Boolean
)

class ContactsRepository(private val context: Context) {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    suspend fun updateContacts() = withContext(Dispatchers.IO) {
        val contactList = mutableListOf<Contact>()
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.STARRED
        )

        try {
            context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.Contacts.STARRED} DESC, ${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val photoIdx = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
                val lookupIdx = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
                val starredIdx = cursor.getColumnIndex(ContactsContract.Contacts.STARRED)

                while (cursor.moveToNext()) {
                    contactList.add(
                        Contact(
                            id = cursor.getString(idIdx),
                            name = cursor.getString(nameIdx) ?: "Unknown",
                            photoUri = cursor.getString(photoIdx),
                            lookupKey = cursor.getString(lookupIdx),
                            isStarred = cursor.getInt(starredIdx) == 1
                        )
                    )
                }
            }
            _contacts.value = contactList
        } catch (e: SecurityException) {
            // No permission
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun toggleStarred(contact: Contact) = withContext(Dispatchers.IO) {
        val values = android.content.ContentValues()
        values.put(ContactsContract.Contacts.STARRED, if (contact.isStarred) 0 else 1)
        
        try {
            context.contentResolver.update(
                ContactsContract.Contacts.CONTENT_URI,
                values,
                ContactsContract.Contacts._ID + "=?",
                arrayOf(contact.id)
            )
            updateContacts()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
