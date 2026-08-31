package com.msa.patcher.evidence

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.msa.patcher.R
import com.msa.patcher.home.HomeViewModel

class EvidenceFragment : Fragment() {
    private val vm: HomeViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_evidence, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        val evidenceText = view.findViewById<TextView>(R.id.evidenceText)
        vm.scanResult.observe(viewLifecycleOwner) { result ->
            evidenceText.text = if (result == null) {
                "No scan evidence yet. Run Quick Scan or Deep Scan from Home."
            } else {
                result.findings.joinToString("\n\n") {
                    "[${it.confidence}] ${it.category}\n${it.title}\n${it.detail}"
                }
            }
        }
    }
}
