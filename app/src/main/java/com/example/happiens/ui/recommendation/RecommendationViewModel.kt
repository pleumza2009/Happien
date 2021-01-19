package com.example.happiens.ui.recommendation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecommendationViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Recommendation Fragment"
    }
    val text: LiveData<String> = _text
}