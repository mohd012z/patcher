package com.msa.patcher.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.msa.patcher.R
import com.msa.patcher.home.HomeViewModel

class ReportFragment : Fragment() {
    private val vm: HomeViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_report, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        val coverage = view.findViewById<ProgressBar>(R.id.coverageProgress)
        val behaviour = view.findViewById<ProgressBar>(R.id.behaviourProgress)
        val text = view.findViewById<TextView>(R.id.reportText)

        vm.scanResult.observe(viewLifecycleOwner) { result ->
            if (result == null) {
                coverage.progress = 0
                behaviour.progress = 0
                text.text = "No report yet. Run a scan from Home."
            } else {
                coverage.progress = if (result.mode.name == "DEEP") 85 else 45
                behaviour.progress = 0
                text.text = buildString {
                    append("Mode: ${result.mode}\n")
                    append("Static entries scanned: ${result.entriesScanned}\n")
                    append("DEX: ${result.dexCount}\nNative libraries: ${result.nativeCount}\n")
                    append("ABI: ${if (result.abis.isEmpty()) "—" else result.abis.joinToString()}\n")
                    append("Indicators: ${(result.frameworkHints + result.keywordHints).ifEmpty { setOf("None from selected heuristics") }.joinToString()}\n\n")
                    append("Behaviour confidence remains 0% because this build performs static analysis only.")
                }
            }
        }
    }
}
