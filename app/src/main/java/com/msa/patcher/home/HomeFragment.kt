package com.msa.patcher.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.msa.patcher.R
import com.msa.patcher.analyze.ArchitectureClassifier
import com.msa.patcher.analyze.PrecheckAnalyzer
import com.msa.patcher.analyze.ScanMode
import com.msa.patcher.analyze.StaticApkScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private val vm: HomeViewModel by activityViewModels()

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        runCatching {
            requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        loadApk(uri)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        val name = view.findViewById<TextView>(R.id.apkName)
        val meta = view.findViewById<TextView>(R.id.apkMeta)
        val architecture = view.findViewById<TextView>(R.id.architectureText)
        val inventory = view.findViewById<TextView>(R.id.inventoryText)
        val confidence = view.findViewById<TextView>(R.id.confidenceText)
        val status = view.findViewById<TextView>(R.id.scanStatusText)
        val progress = view.findViewById<ProgressBar>(R.id.scanProgress)
        val quick = view.findViewById<MaterialButton>(R.id.quickScanButton)
        val deep = view.findViewById<MaterialButton>(R.id.deepScanButton)

        view.findViewById<MaterialButton>(R.id.selectApkButton).setOnClickListener {
            picker.launch(arrayOf("application/vnd.android.package-archive", "application/zip", "*/*"))
        }

        quick.setOnClickListener { runScan(ScanMode.QUICK) }
        deep.setOnClickListener { runScan(ScanMode.DEEP) }

        vm.summary.observe(viewLifecycleOwner) { s ->
            if (s == null) return@observe
            name.text = "📦 ${s.fileName}"
            meta.text = "${s.sizeLabel}  •  SHA-256 ${s.sha256.take(12)}…${s.sha256.takeLast(8)}"
            architecture.text = "Architecture  ${ArchitectureClassifier.classify(s).label}"
            inventory.text = "DEX ${s.dexCount}   Native ${s.nativeCount}   ABI ${s.abiLabel}"
            confidence.text = "Coverage precheck ready   •   Behaviour not inferred"
            quick.isEnabled = vm.busyText.value == null
            deep.isEnabled = vm.busyText.value == null
        }

        vm.scanResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            status.text = "${result.mode.name} complete • ${result.findings.size} findings • ${result.durationMs} ms"
            confidence.text = "${result.coverageLabel}   •   Behaviour requires runtime evidence"
        }

        vm.busyText.observe(viewLifecycleOwner) { text ->
            val busy = text != null
            progress.visibility = if (busy) View.VISIBLE else View.GONE
            if (busy) status.text = text
            quick.isEnabled = !busy && vm.summary.value != null
            deep.isEnabled = !busy && vm.summary.value != null
        }
    }

    private fun loadApk(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.setBusy("Reading APK and running precheck…")
            runCatching {
                withContext(Dispatchers.IO) {
                    PrecheckAnalyzer.analyze(requireContext(), uri, displayName(uri))
                }
            }.onSuccess { summary ->
                vm.setSelected(uri, summary)
            }.onFailure {
                Toast.makeText(requireContext(), "Unable to read APK: ${it.message}", Toast.LENGTH_LONG).show()
            }
            vm.setBusy(null)
        }
    }

    private fun runScan(mode: ScanMode) {
        val uri = vm.selectedUri.value ?: run {
            Toast.makeText(requireContext(), "Select an APK first", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.setBusy(if (mode == ScanMode.DEEP) "Deep static scan running…" else "Quick static scan running…")
            runCatching {
                withContext(Dispatchers.IO) { StaticApkScanner.scan(requireContext(), uri, mode) }
            }.onSuccess { result ->
                vm.setScanResult(result)
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_analyze
            }.onFailure {
                Toast.makeText(requireContext(), "Scan failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
            vm.setBusy(null)
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
