package com.msa.patcher.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.msa.patcher.MainActivity
import com.msa.patcher.R
import com.msa.patcher.scripts.ScriptRegistry

class ToolsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_tools, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        val count = ScriptRegistry(requireContext()).legacyOnly().size
        view.findViewById<TextView>(R.id.legacyCount).text = "$count legacy utilities available"

        view.findViewById<View>(R.id.modifyWorkspaceCard).setOnClickListener {
            (requireActivity() as? MainActivity)?.openModifyWorkspace()
        }
    }
}
