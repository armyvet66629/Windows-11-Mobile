package com.example.windows11mobile.ui.people

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.windows11mobile.data.Contact
import com.example.windows11mobile.data.ContactsRepository
import com.example.windows11mobile.data.RecentActivity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PeopleViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = ContactsRepository.getInstance(application.applicationContext)
    val contacts: StateFlow<List<Contact>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivity: StateFlow<List<RecentActivity>> = repository.recentActivity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
        
        // Auto-refresh periodically to pick up new calls/messages
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30000) // Every 30 seconds
                repository.updateRecentActivity()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.updateContacts()
            repository.updateRecentActivity()
        }
    }

    fun toggleStarred(contact: Contact) {
        viewModelScope.launch {
            repository.toggleStarred(contact)
        }
    }
}

class PeopleViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeopleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PeopleViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
