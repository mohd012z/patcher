package com.msa.patcher.modify

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.msa.patcher.R
import com.msa.patcher.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ModifyFragment : Fragment() {
    private val homeVm: HomeViewModel by activityViewModels()

    private var sourceUri: Uri? = null
    private var sourceName: String = "selected.apk"
    private var engine: ApkWorkspaceEngine? = null
    private var rebuiltFile: File? = null
    private var pendingReplacePath: String? = null

    private lateinit var status: TextView
    private lateinit var mutationLog: TextView
    private lateinit var entrySpinner: Spinner
    private lateinit var textEditor: EditText
    private lateinit var versionName: EditText
    private lateinit var versionCode: EditText
    private lateinit var appLabel: EditText

    private val chooseApk = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            sourceUri = uri
            sourceName = queryName(uri) ?: "selected.apk"
            status.text = "Selected: $sourceName\nCreate Workspace to begin editing."
        }
    }

    private val chooseReplacement = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val path = pendingReplacePath
        pendingReplacePath = null
        if (uri != null && path != null) {
            runIo("Replacing $path…") {
                requireContext().contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Unable to open replacement file." }
                    requireEngine().replace(path, input, "Replacement imported from document picker")
                }
                "Replaced: $path"
            }
        }
    }

    private val exportApk = registerForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")) { uri ->
        val file = rebuiltFile
        if (uri != null && file != null) {
            runIo("Exporting rebuilt APK…") {
                requireContext().contentResolver.openOutputStream(uri, "w").use { out ->
                    requireNotNull(out) { "Unable to open export destination." }
                    file.inputStream().use { it.copyTo(out) }
                }
                "Exported rebuilt unsigned APK.\nSign it with your own authorized signing identity before installation."
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_modify, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        status = view.findViewById(R.id.modifyStatus)
        mutationLog = view.findViewById(R.id.modifyMutationLog)
        entrySpinner = view.findViewById(R.id.modifyEntrySpinner)
        textEditor = view.findViewById(R.id.modifyTextEditor)
        versionName = view.findViewById(R.id.modifyVersionName)
        versionCode = view.findViewById(R.id.modifyVersionCode)
        appLabel = view.findViewById(R.id.modifyAppLabel)

        sourceUri = homeVm.selectedUri.value
        if (sourceUri != null) {
            sourceName = queryName(sourceUri!!) ?: "selected.apk"
            status.text = "Using Home selection: $sourceName"
        } else {
            status.text = "Choose an APK or select one from Home first."
        }

        view.findViewById<Button>(R.id.modifyChooseApk).setOnClickListener {
            chooseApk.launch(arrayOf("application/vnd.android.package-archive", "application/zip", "*/*"))
        }

        view.findViewById<Button>(R.id.modifyCreateWorkspace).setOnClickListener {
            val uri = sourceUri
            if (uri == null) {
                status.text = "Choose an APK first."
                return@setOnClickListener
            }
            runIo("Creating isolated workspace…") {
                val workspace = File(requireContext().cacheDir, "modify_workspace")
                val newEngine = ApkWorkspaceEngine(workspace)
                requireContext().contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Unable to open APK." }
                    newEngine.extract(input, sourceName)
                }
                engine = newEngine
                rebuiltFile = null
                withContext(Dispatchers.Main) { refreshEntries() }
                "Workspace ready. Original APK remains unchanged."
            }
        }

        view.findViewById<Button>(R.id.modifyLoadText).setOnClickListener {
            val path = selectedPath() ?: return@setOnClickListener
            runIo("Loading $path…") {
                val text = requireEngine().readText(path)
                withContext(Dispatchers.Main) { textEditor.setText(text) }
                "Loaded plaintext entry: $path"
            }
        }

        view.findViewById<Button>(R.id.modifySaveText).setOnClickListener {
            val path = selectedPath() ?: return@setOnClickListener
            val text = textEditor.text.toString()
            runIo("Saving $path…") {
                requireEngine().writeText(path, text)
                withContext(Dispatchers.Main) { refreshEntries() }
                "Saved plaintext entry: $path"
            }
        }

        view.findViewById<Button>(R.id.modifyReplaceFile).setOnClickListener {
            val path = selectedPath() ?: return@setOnClickListener
            if (path == "AndroidManifest.xml") {
                status.text = "Use Manifest Metadata for AndroidManifest.xml."
                return@setOnClickListener
            }
            pendingReplacePath = path
            chooseReplacement.launch(arrayOf("*/*"))
        }

        view.findViewById<Button>(R.id.modifyManifestApply).setOnClickListener {
            val name = versionName.text.toString().trim().ifBlank { null }
            val code = versionCode.text.toString().trim().toLongOrNull()
            val label = appLabel.text.toString().trim().ifBlank { null }
            runIo("Updating plaintext manifest metadata…") {
                val message = requireEngine().updatePlaintextManifest(name, code, label)
                withContext(Dispatchers.Main) { refreshEntries() }
                message
            }
        }

        view.findViewById<Button>(R.id.modifyUndo).setOnClickListener {
            runIo("Undoing last mutation…") {
                val undone = requireEngine().undoLast()
                withContext(Dispatchers.Main) { refreshEntries() }
                if (undone) "Last mutation restored." else "Nothing to undo."
            }
        }

        view.findViewById<Button>(R.id.modifyRebuild).setOnClickListener {
            runIo("Rebuilding modified APK archive…") {
                val output = File(requireContext().cacheDir, "MSAPatcher_modified_unsigned.apk")
                rebuiltFile = requireEngine().rebuild(output)
                "Rebuild complete: ${output.name}\nUnsigned by design. Original APK was not overwritten."
            }
        }

        view.findViewById<Button>(R.id.modifyExport).setOnClickListener {
            val file = rebuiltFile
            if (file == null || !file.isFile) {
                status.text = "Rebuild the APK before export."
            } else {
                exportApk.launch("MSAPatcher_modified_unsigned.apk")
            }
        }

        view.findViewById<Button>(R.id.modifyBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun selectedPath(): String? {
        val value = entrySpinner.selectedItem?.toString()
        if (value.isNullOrBlank()) {
            status.text = "Create a workspace and select an editable entry first."
            return null
        }
        return value.substringBefore("  •  ")
    }

    private fun refreshEntries() {
        val e = engine ?: return
        val editable = e.editableEntries(sourceName)
        val labels = editable.map {
            buildString {
                append(it.path)
                append("  •  ")
                append(if (it.textEditable) "TEXT" else "FILE")
                append("  •  ")
                append(it.size)
                append(" B")
            }
        }
        entrySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            labels
        )
        mutationLog.text = e.mutationLog().ifEmpty { listOf("No modifications yet.") }.joinToString("\n")
    }

    private fun requireEngine(): ApkWorkspaceEngine =
        requireNotNull(engine) { "Create Workspace first." }

    private fun runIo(start: String, block: suspend () -> String) {
        status.text = start
        viewLifecycleOwner.lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { block() }
            }
            status.text = result.getOrElse { "Error: ${it.message ?: it.javaClass.simpleName}" }
            if (result.isSuccess) refreshEntries()
        }
    }

    private fun queryName(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null) ?: return null
        cursor.use {
            val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            return if (index >= 0 && it.moveToFirst()) it.getString(index) else null
        }
    }
}
