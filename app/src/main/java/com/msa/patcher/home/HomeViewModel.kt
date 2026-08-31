package com.msa.patcher.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msa.patcher.model.ApkSummary

class HomeViewModel : ViewModel() {
    private val _summary = MutableLiveData<ApkSummary?>(null)
    val summary: LiveData<ApkSummary?> = _summary
    fun setSummary(value: ApkSummary?) { _summary.value = value }
    fun clear() { _summary.value = null }
}
