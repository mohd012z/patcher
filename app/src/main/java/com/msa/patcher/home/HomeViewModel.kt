package com.msa.patcher.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msa.patcher.analyze.ScanResult
import com.msa.patcher.model.ApkSummary

class HomeViewModel : ViewModel() {
    private val _summary = MutableLiveData<ApkSummary?>(null)
    val summary: LiveData<ApkSummary?> = _summary

    private val _selectedUri = MutableLiveData<Uri?>(null)
    val selectedUri: LiveData<Uri?> = _selectedUri

    private val _scanResult = MutableLiveData<ScanResult?>(null)
    val scanResult: LiveData<ScanResult?> = _scanResult

    private val _busyText = MutableLiveData<String?>(null)
    val busyText: LiveData<String?> = _busyText

    fun setSelected(uri: Uri, summary: ApkSummary) {
        _selectedUri.value = uri
        _summary.value = summary
        _scanResult.value = null
    }

    fun setScanResult(result: ScanResult) { _scanResult.value = result }
    fun setBusy(text: String?) { _busyText.value = text }

    fun clear() {
        _selectedUri.value = null
        _summary.value = null
        _scanResult.value = null
        _busyText.value = null
    }
}
