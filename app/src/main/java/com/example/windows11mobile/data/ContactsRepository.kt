package com.example.windows11mobile.data

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Contact(
    val id: String,
    val name: String,
    val photoUri: String?,
    val lookupKey: String?,
    val isStarred: Boolean
)

data class RecentActivity(
    val id: String,
    val type: ActivityType,
    val name: String,
    val summary: String,
    val timestamp: Long,
    val photoUri: String? = null,
    val data: String? = null, // threadId for messages
    val address: String? = null // Number for both
)

enum class ActivityType {
    CALL, MESSAGE
}

class ContactsRepository private constructor(private val context: Context) {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _recentActivity = MutableStateFlow<List<RecentActivity>>(emptyList())
    val recentActivity: StateFlow<List<RecentActivity>> = _recentActivity

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            android.util.Log.d("ContactsRepository", "ContentObserver: Change detected")
            CoroutineScope(Dispatchers.IO).launch {
                updateRecentActivity()
            }
        }
    }

    init {
        registerObservers()
        // Immediate first update
        CoroutineScope(Dispatchers.IO).launch {
            updateRecentActivity()
            updateContacts()
        }
        
        // Fail-safe background refresh for phones that block observers
        CoroutineScope(Dispatchers.IO).launch {
            while(true) {
                kotlinx.coroutines.delay(60000) // Every minute
                updateRecentActivity()
            }
        }
    }

    fun registerObservers() {
        try {
            context.contentResolver.unregisterContentObserver(observer)
            context.contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer)
            context.contentResolver.registerContentObserver(Uri.parse("content://mms-sms/"), true, observer)
            android.util.Log.d("ContactsRepository", "Observers registered successfully (Calls + MMS/SMS)")
        } catch (e: SecurityException) {
            android.util.Log.e("ContactsRepository", "SecurityException during observer registration", e)
        }
    }

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

    suspend fun updateRecentActivity() = withContext(Dispatchers.IO) {
        android.util.Log.d("ContactsRepository", "RecentActivity: Update started")
        val activityList = mutableListOf<RecentActivity>()
        
        // 1. Fetch Calls
        try {
            val callProjection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.CACHED_PHOTO_URI
            )
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                callProjection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC LIMIT 10"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(CallLog.Calls._ID)
                val nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numberIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE)
                val photoIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)

                while (cursor.moveToNext()) {
                    val number = cursor.getString(numberIdx) ?: ""
                    val cachedName = cursor.getString(nameIdx)
                    val cachedPhoto = cursor.getString(photoIdx)
                    
                    var finalName = cachedName
                    var finalPhoto = cachedPhoto
                    
                    if (finalName == null || finalPhoto == null) {
                        val resolved = getContactInfo(number)
                        if (finalName == null) finalName = resolved.first ?: number
                        if (finalPhoto == null) finalPhoto = resolved.second
                    }

                    val type = when (cursor.getInt(typeIdx)) {
                        CallLog.Calls.INCOMING_TYPE -> "Incoming call"
                        CallLog.Calls.OUTGOING_TYPE -> "Outgoing call"
                        CallLog.Calls.MISSED_TYPE -> "Missed call"
                        else -> "Call"
                    }
                    activityList.add(RecentActivity(
                        id = "call_${cursor.getString(idIdx)}",
                        type = ActivityType.CALL,
                        name = finalName,
                        summary = type,
                        timestamp = cursor.getLong(dateIdx),
                        photoUri = finalPhoto,
                        data = null,
                        address = number
                    ))
                }
            }
            android.util.Log.d("ContactsRepository", "RecentActivity: Found ${activityList.size} calls")
        } catch (e: Exception) {
            android.util.Log.e("ContactsRepository", "RecentActivity: Call error", e)
        }

        // 2. Fetch SMS Messages
        try {
            val smsProjection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.THREAD_ID
            )
            context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                smsProjection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 10"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
                val addrIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                val threadIdx = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)

                while (cursor.moveToNext()) {
                    val address = cursor.getString(addrIdx) ?: ""
                    val threadId = cursor.getString(threadIdx)
                    val resolved = getContactInfo(address)
                    
                    activityList.add(RecentActivity(
                        id = "sms_${cursor.getString(idIdx)}",
                        type = ActivityType.MESSAGE,
                        name = resolved.first ?: address,
                        summary = cursor.getString(bodyIdx) ?: "",
                        timestamp = cursor.getLong(dateIdx),
                        photoUri = resolved.second,
                        data = threadId,
                        address = address
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactsRepository", "RecentActivity: SMS error", e)
        }

        // 3. Fetch MMS Messages (Modern texts are often MMS)
        try {
            val mmsProjection = arrayOf("_id", "date", "thread_id")
            context.contentResolver.query(
                Uri.parse("content://mms"),
                mmsProjection,
                null,
                null,
                "date DESC LIMIT 10"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex("_id")
                val dateIdx = cursor.getColumnIndex("date")
                val threadIdx = cursor.getColumnIndex("thread_id")

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx)
                    val threadId = cursor.getString(threadIdx)
                    // MMS date is in seconds
                    val timestamp = cursor.getLong(dateIdx) * 1000
                    
                    // Get address for MMS
                    val address = getMmsAddress(id)
                    val resolved = getContactInfo(address ?: "")

                    activityList.add(RecentActivity(
                        id = "mms_$id",
                        type = ActivityType.MESSAGE,
                        name = resolved.first ?: address ?: "MMS",
                        summary = "Multimedia message",
                        timestamp = timestamp,
                        photoUri = resolved.second,
                        data = threadId,
                        address = address
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactsRepository", "RecentActivity: MMS error", e)
        }

        val sorted = activityList.sortedByDescending { it.timestamp }.take(20)
        _recentActivity.value = sorted
        android.util.Log.d("ContactsRepository", "RecentActivity: Total items: ${sorted.size}")
    }

    private fun getMmsAddress(mmsId: String): String? {
        val uri = Uri.parse("content://mms/$mmsId/addr")
        return try {
            context.contentResolver.query(uri, null, "msg_id = ? AND type = 137", arrayOf(mmsId), null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow("address"))
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getContactInfo(address: String): Pair<String?, String?> {
        if (address.isBlank()) return Pair(null, null)
        
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
        )
        
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                    val photo = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI))
                    Pair(name, photo)
                } else Pair(null, null)
            } ?: Pair(null, null)
        } catch (e: Exception) {
            Pair(null, null)
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

    companion object {
        @Volatile
        private var INSTANCE: ContactsRepository? = null

        fun getInstance(context: Context): ContactsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
