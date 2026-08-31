package com.msa.patcher.home

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.msa.patcher.R
import com.msa.patcher.analyze.ArchitectureClassifier
import com.msa.patcher.analyze.PrecheckAnalyzer

class HomeFragment : Fragment() {
    private val vm: HomeViewModel by activityViewModels()
    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        runCatching { requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        runCatching {
            val summary = PrecheckAnalyzer.analyze(requireContext(), uri, displayName(uri))
            vm.setSummary(summary)
        }.onFailure { Toast.makeText(requireContext(), "Unable to read APK: ${it.message}", Toast.LENGTH_LONG).show() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        val name = view.findViewById<TextView>(R.id.apkName)
        val meta = view.findViewById<TextView>(R.id.apkMeta)
        val architecture = view.findViewById<TextView>(R.id.architectureText)
        val inventory = view.findViewById<TextView>(R.id.inventoryText)
        val confidence = view.findViewById<TextView>(R.id.confidenceText)
        val quick = view.findViewById<MaterialButton>(R.id.quickScanButton)
        val deep = view.findViewById<MaterialButton>(R.id.deepScanButton)
        view.findViewById<MaterialButton>(R.id.selectApkButton).setOnClickListener { picker.launch(arrayOf("application/vnd.android.package-archive", "application/zip", "*/*")) }
        vm.summary.observe(viewLifecycleOwner) { s ->
            if (s == null) return@observe
            name.text = "📦 ${s.fileName}"
            meta.text = "${s.sizeLabel}  •  SHA-256 ${s.sha256.take(12)}…${s.sha256.takeLast(8)}"
            architecture.text = "Architecture  ${ArchitectureClassifier.classify(s).label}"
            inventory.text = "DEX ${s.dexCount}   Native ${s.nativeCount}   ABI ${s.abiLabel}"
            confidence.text = "Coverage precheck ready   •   Behaviour pending deep scan"
            quick.isEnabled = true
            deep.isEnabled = true
        }
    }

    private fun displayName(uri: Uri): String {
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (i >= 0 && c.moveToFirst()) return c.getString(i)
        }
        return uri.lastPathSegment ?: "selected.apk"
    }
}
