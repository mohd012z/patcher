package com.msa.patcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.msa.patcher.analyze.AnalyzeFragment
import com.msa.patcher.evidence.EvidenceFragment
import com.msa.patcher.home.HomeFragment
import com.msa.patcher.modify.ModifyFragment
import com.msa.patcher.report.ReportFragment
import com.msa.patcher.tools.ToolsFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav = findViewById<BottomNavigationView>(R.id.bottomNav)
        nav.setOnItemSelectedListener { item ->
            showDestination(
                when (item.itemId) {
                    R.id.nav_analyze -> AnalyzeFragment()
                    R.id.nav_evidence -> EvidenceFragment()
                    R.id.nav_report -> ReportFragment()
                    R.id.nav_tools -> ToolsFragment()
                    else -> HomeFragment()
                }
            )
            true
        }
        if (savedInstanceState == null) {
            nav.selectedItemId = R.id.nav_home
        }
    }

    fun openModifyWorkspace() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contentFrame, ModifyFragment())
            .addToBackStack("modify_workspace")
            .commit()
    }

    private fun showDestination(fragment: Fragment) {
        supportFragmentManager.popBackStack()
        supportFragmentManager.beginTransaction()
            .replace(R.id.contentFrame, fragment)
            .commit()
    }
}
