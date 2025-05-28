package com.example.careconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.careconnect.repository.SocialRepository

class SocialViewModelFactory(
    private val socialRepository: SocialRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialViewModel::class.java)) {
            return SocialViewModel(socialRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}