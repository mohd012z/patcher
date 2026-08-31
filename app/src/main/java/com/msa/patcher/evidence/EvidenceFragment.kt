package com.msa.patcher.evidence

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.msa.patcher.R
import com.msa.patcher.home.HomeViewModel

class EvidenceFragment : Fragment() {
    private val vm: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View = inflater.inflate(R.layout.fragment_evidence, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        val scroll = view.findViewById<ScrollView>(R.id.evidenceScroll)
        val evidenceText = view.findViewById<TextView>(R.id.evidenceText)
        val scrollStatus = view.findViewById<TextView>(R.id.evidenceScrollStatus)

        var evidenceCount = 0

        fun updateScrollStatus(scrollY: Int = scroll.scrollY) {
            val contentHeight = scroll.getChildAt(0)?.height ?: 0
            scrollStatus.text = EvidenceScrollState.label(
                scrollY = scrollY,
                contentHeight = contentHeight,
                viewportHeight = scroll.height,
                evidenceCount = evidenceCount
            )
        }

        scroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            updateScrollStatus(scrollY)
        }

        vm.scanResult.observe(viewLifecycleOwner) { result ->
            evidenceCount = result?.findings?.size ?: 0
            evidenceText.text = if (result == null) {
                "No scan evidence yet. Run Quick Scan or Deep Scan from Home."
            } else {
                EvidenceFormatter.format(result.findings)
            }

            scroll.post {
                scroll.scrollTo(0, 0)
                updateScrollStatus(0)
            }
        }
    }
}
