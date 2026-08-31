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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View = inflater.inflate(R.layout.fragment_analyze, container, false)
    override fun onViewCreated(view: View, state: Bundle?) {
        val resultText = view.findViewById<TextView>(R.id.analysisResultText)
        val list = view.findViewById<RecyclerView>(R.id.categoryList)
        val initialRows = AnalysisPlan.ALL.mapIndexed { index, title -> AnalysisCategory(index + 1, title, "NOT RUN", "Run Quick Scan or Deep Scan from Home first.") }
        val adapter = CategoryAdapter(initialRows) { item ->
            resultText.text = buildString { append("${item.title}\n"); append("Status: ${item.state}\n\n"); append(item.detail) }
        }
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter
        vm.scanResult.observe(viewLifecycleOwner) { result ->
            if (result == null) {
                adapter.submitItems(initialRows)
                resultText.text = "No scan result yet. Select an APK on Home and run Quick Scan or Deep Scan."
                return@observe
            }
            val rows = AnalysisStateMapper.map(result)
            adapter.submitItems(rows)
            val found = rows.count { it.state == "FOUND" }
            val clean = rows.count { it.state == "CLEAN" }
            val limited = rows.count { it.state == "LIMITED" }
            val hints = (result.frameworkHints + result.keywordHints).ifEmpty { setOf("No major heuristic indicators") }
            resultText.text = buildString {
                append("${result.mode.name} ANALYSIS READY\n")
                append("Entries ${result.entriesScanned} • DEX ${result.dexCount} • Native ${result.nativeCount}\n")
                append("Categories: $found found • $clean clean • $limited limited\n")
                append("ABI: ${if (result.abis.isEmpty()) "—" else result.abis.joinToString()}\n")
                append("Indicators: ${hints.joinToString()}\n\n")
                append("Tap any category below to open its evidence/detail.")
            }
        }
    }
}
