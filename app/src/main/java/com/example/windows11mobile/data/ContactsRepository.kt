package com.example.windows11mobile.data

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

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

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            android.util.Log.d("ContactsRepository", "ContentObserver: Change detected")
            // Try to show a toast for debug
            try {
                android.widget.Toast.makeText(context, "System Activity Detected", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {}
            
            CoroutineScope(Dispatchers.IO).launch {
                updateRecentActivity()
            }
        }
    }

    init {
        // Immediate first update
        CoroutineScope(Dispatchers.IO).launch {
            updateRecentActivity()
            updateContacts()
        }
        
        registerObservers()

        // Fail-safe background refresh for phones that block observers
        CoroutineScope(Dispatchers.IO).launch {
            while(true) {
                kotlinx.coroutines.delay(30000) // Every 30 seconds
                updateRecentActivity()
            }
        }
    }

    fun registerObservers() {
        try {
            android.util.Log.d("ContactsRepository", "Registering observers...")
            context.contentResolver.unregisterContentObserver(observer)
            context.contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer)
            context.contentResolver.registerContentObserver(Uri.parse("content://sms"), true, observer)
            context.contentResolver.registerContentObserver(Uri.parse("content://mms"), true, observer)
            context.contentResolver.registerContentObserver(Telephony.MmsSms.CONTENT_URI, true, observer)
            android.util.Log.d("ContactsRepository", "Observers registered successfully (Calls + SMS + MMS + MmsSms)")
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
        android.util.Log.d("ContactsRepository", "RecentActivity: Update starting... [FORCE REFRESH TRIGGERED]")
        
        val hasCallLog = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        val hasSms = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        
        android.util.Log.d("ContactsRepository", "Permissions: CallLog=$hasCallLog, SMS=$hasSms")

        if (!hasCallLog && !hasSms) {
            android.util.Log.w("ContactsRepository", "RecentActivity: Missing permissions (CallLog=$hasCallLog, SMS=$hasSms)")
            return@withContext
        }

        val activityList = mutableListOf<RecentActivity>()
        
        // 1. Calls
        if (hasCallLog) {
            try {
                android.util.Log.d("ContactsRepository", "Querying CallLog...")
                val callProjection = arrayOf(
                    CallLog.Calls._ID,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.CACHED_PHOTO_URI,
                    CallLog.Calls.DURATION
                )
                
                val callUris = listOf(
                    CallLog.Calls.CONTENT_URI,
                    Uri.parse("content://call_log/calls")
                )

                for (uri in callUris) {
                    context.contentResolver.query(
                        uri,
                        callProjection,
                        null,
                        null,
                        "${CallLog.Calls.DATE} DESC LIMIT 50"
                    )?.use { cursor ->
                        android.util.Log.d("ContactsRepository", "Found ${cursor.count} items in $uri")
                        val idIdx = cursor.getColumnIndex(CallLog.Calls._ID)
                        val nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                        val numberIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                        val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)
                        val dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE)
                        val photoIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)
                        val durationIdx = cursor.getColumnIndex(CallLog.Calls.DURATION)

                        while (cursor.moveToNext()) {
                            val id = if (idIdx != -1) cursor.getString(idIdx) else UUID.randomUUID().toString()
                            val number = if (numberIdx != -1) cursor.getString(numberIdx) else ""
                            val cachedName = if (nameIdx != -1) cursor.getString(nameIdx) else null
                            val cachedPhoto = if (photoIdx != -1) cursor.getString(photoIdx) else null
                            
                            var finalName = cachedName
                            var finalPhoto = cachedPhoto
                            
                            if (finalName.isNullOrBlank() || finalPhoto == null) {
                                val resolved = getContactInfo(number)
                                if (finalName.isNullOrBlank()) finalName = resolved.first ?: number
                                if (finalPhoto == null) finalPhoto = resolved.second
                            }

                            val callType = if (typeIdx != -1) cursor.getInt(typeIdx) else -1
                            val duration = if (durationIdx != -1) cursor.getLong(durationIdx) else 0L
                            
                            val summaryText = when (callType) {
                                CallLog.Calls.INCOMING_TYPE -> "Incoming (${formatDuration(duration)})"
                                CallLog.Calls.OUTGOING_TYPE -> "Outgoing (${formatDuration(duration)})"
                                CallLog.Calls.MISSED_TYPE -> "Missed call"
                                else -> "Call"
                            }

                            activityList.add(RecentActivity(
                                id = "call_$id",
                                type = ActivityType.CALL,
                                name = finalName,
                                summary = summaryText,
                                timestamp = if (dateIdx != -1) cursor.getLong(dateIdx) else System.currentTimeMillis(),
                                photoUri = finalPhoto,
                                address = number
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ContactsRepository", "Error fetching calls", e)
            }
        }

        // 2. SMS
        if (hasSms) {
            val smsUris = listOf(
                Telephony.Sms.CONTENT_URI,
                Telephony.Sms.Inbox.CONTENT_URI,
                Telephony.Sms.Sent.CONTENT_URI
            )
            
            for (uri in smsUris) {
                try {
                    android.util.Log.d("ContactsRepository", "Querying SMS URI: $uri")
                    val smsProjection = arrayOf(
                        Telephony.Sms._ID,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.DATE,
                        Telephony.Sms.THREAD_ID
                    )
                    context.contentResolver.query(
                        uri,
                        smsProjection,
                        null,
                        null,
                        "date DESC LIMIT 30"
                    )?.use { cursor ->
                        android.util.Log.d("ContactsRepository", "Found ${cursor.count} messages in $uri")
                        val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
                        val addrIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                        val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                        val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                        val threadIdx = cursor.getColumnIndex(Telephony.Sms.THREAD_ID)

                        while (cursor.moveToNext()) {
                            val id = if (idIdx != -1) cursor.getString(idIdx) else UUID.randomUUID().toString()
                            val address = if (addrIdx != -1) cursor.getString(addrIdx) else ""
                            val resolved = getContactInfo(address)
                            
                            activityList.add(RecentActivity(
                                id = "sms_$id",
                                type = ActivityType.MESSAGE,
                                name = resolved.first ?: address,
                                summary = if (bodyIdx != -1) (cursor.getString(bodyIdx) ?: "New message") else "New message",
                                timestamp = if (dateIdx != -1) cursor.getLong(dateIdx) else System.currentTimeMillis(),
                                photoUri = resolved.second,
                                data = if (threadIdx != -1) cursor.getString(threadIdx) else null,
                                address = address
                            ))
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("ContactsRepository", "Error querying SMS URI: $uri", e)
                }
            }
            
            // 3. MMS
            try {
                android.util.Log.d("ContactsRepository", "Querying MMS...")
                val mmsProjection = arrayOf("_id", "date", "thread_id")
                context.contentResolver.query(
                    Telephony.Mms.CONTENT_URI,
                    mmsProjection,
                    null,
                    null,
                    "date DESC LIMIT 30"
                )?.use { cursor ->
                    android.util.Log.d("ContactsRepository", "Found ${cursor.count} MMS")
                    val idIdx = cursor.getColumnIndex("_id")
                    val dateIdx = cursor.getColumnIndex("date")
                    val threadIdx = cursor.getColumnIndex("thread_id")

                    while (cursor.moveToNext()) {
                        val id = if (idIdx != -1) cursor.getString(idIdx) else UUID.randomUUID().toString()
                        val timestamp = if (dateIdx != -1) cursor.getLong(dateIdx) * 1000 else System.currentTimeMillis()
                        val address = getMmsAddress(id)
                        val resolved = getContactInfo(address ?: "")

                        activityList.add(RecentActivity(
                            id = "mms_$id",
                            type = ActivityType.MESSAGE,
                            name = resolved.first ?: address ?: "Multimedia Message",
                            summary = "Multimedia message",
                            timestamp = timestamp,
                            photoUri = resolved.second,
                            data = if (threadIdx != -1) cursor.getString(threadIdx) else null,
                            address = address
                        ))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ContactsRepository", "Error fetching MMS", e)
            }
        }

        // Final Sort and Dedup
        val sorted = activityList
            .distinctBy { 
                if (it.type == ActivityType.MESSAGE) {
                    "${it.address}_${it.summary}_${it.timestamp / 5000}" // Dedup messages by 5s window
                } else {
                    it.id
                }
            }
            .sortedByDescending { it.timestamp }
            .take(50)
            
        _recentActivity.value = sorted
        _lastSyncTime.value = System.currentTimeMillis()
        android.util.Log.d("ContactsRepository", "RecentActivity: Update complete. Final count: ${sorted.size} at ${_lastSyncTime.value}")
    }

    private fun formatDuration(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
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
