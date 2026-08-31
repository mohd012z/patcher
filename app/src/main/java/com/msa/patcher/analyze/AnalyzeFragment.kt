package com.msa.patcher.analyze

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msa.patcher.R
import com.msa.patcher.home.HomeViewModel

class AnalyzeFragment : Fragment() {
    private val vm: HomeViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_analyze, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        val resultText = view.findViewById<TextView>(R.id.analysisResultText)
        val list = view.findViewById<RecyclerView>(R.id.categoryList)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = CategoryAdapter(AnalysisPlan.ALL.mapIndexed { i, title -> AnalysisCategory(i + 1, title) })

        vm.scanResult.observe(viewLifecycleOwner) { result ->
            if (result == null) {
                resultText.text = "No scan result yet. Select an APK on Home and run Quick Scan or Deep Scan."
            } else {
                val hints = (result.frameworkHints + result.keywordHints).ifEmpty { setOf("No major heuristic indicators") }
                val findings = result.findings.joinToString("\n") { "• [${it.confidence}] ${it.category}: ${it.title}" }
                resultText.text = buildString {
                    append("${result.mode.name} SCAN COMPLETE\n")
                    append("Entries ${result.entriesScanned} • DEX ${result.dexCount} • Native ${result.nativeCount}\n")
                    append("ABI: ${if (result.abis.isEmpty()) "—" else result.abis.joinToString()}\n")
                    append("Indicators: ${hints.joinToString()}\n\n")
                    append(findings)
                }
            }
        }
    }
}
