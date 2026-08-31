package com.msa.patcher.modify

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.msa.patcher.R
import com.msa.patcher.home.HomeViewModel
import com.msa.patcher.modify.assistant.AssistantContext
import com.msa.patcher.modify.assistant.LocalAssistant
import com.msa.patcher.modify.preflight.BuildPreflight
import com.msa.patcher.modify.code.SmaliQuickCode
import com.msa.patcher.modify.converter.DataConverter
import com.msa.patcher.modify.converter.DetectedInputType
import com.msa.patcher.modify.converter.LanguageRequest
import com.msa.patcher.modify.converter.MlKitLanguageTranslator
import com.msa.patcher.modify.help.FieldHelp
import com.msa.patcher.modify.help.SuggestionContext
import com.msa.patcher.modify.help.WorkspaceSuggestions
import com.msa.patcher.modify.search.SearchEntry
import com.msa.patcher.modify.search.SearchHit
import com.msa.patcher.modify.search.SearchScope
import com.msa.patcher.modify.search.WorkspaceSearch
import com.msa.patcher.modify.settings.ButtonSize
import com.msa.patcher.modify.settings.SharedPreferencesSettingsBackend
import com.msa.patcher.modify.settings.WorkspaceUiSettings
import com.msa.patcher.modify.settings.WorkspaceUiSettingsStore
import com.msa.patcher.modify.settings.WorkspaceViewModeSetting
import com.msa.patcher.modify.ui.CommandHubAction
import com.msa.patcher.modify.ui.BottomToolbarController
import com.msa.patcher.modify.ui.CommandHubController
import com.msa.patcher.modify.ui.PerViewZoomController
import com.msa.patcher.modify.ui.ZoomViewKey
import com.msa.patcher.modify.ui.WorkspaceViewController
import com.msa.patcher.modify.ui.WorkspaceViewMode
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
    private var entryPaths: List<String> = emptyList()
    private var lastSearchHits: List<SearchHit> = emptyList()
    private val translator = MlKitLanguageTranslator()

    private lateinit var status: TextView
    private lateinit var mutationLog: TextView
    private lateinit var entrySpinner: Spinner
    private lateinit var textEditor: EditText
    private lateinit var fileList: TextView
    private lateinit var versionName: EditText
    private lateinit var versionCode: EditText
    private lateinit var appLabel: EditText
    private lateinit var manifestState: TextView
    private lateinit var searchQuery: EditText
    private lateinit var searchScope: Spinner
    private lateinit var searchResults: TextView
    private lateinit var converterInput: EditText
    private lateinit var converterType: Spinner
    private lateinit var converterOutput: TextView
    private lateinit var languageInput: EditText
    private lateinit var languageSource: Spinner
    private lateinit var languageTarget: Spinner
    private lateinit var languagePreserve: CheckBox
    private lateinit var languageOutput: TextView
    private lateinit var smaliDescriptor: EditText
    private lateinit var smaliExplainInput: EditText
    private lateinit var smaliSnippet: Spinner
    private lateinit var smaliPreview: TextView
    private lateinit var diffOutput: TextView
    private lateinit var buildOutput: TextView
    private lateinit var outputName: EditText
    private lateinit var assistantPanel: LinearLayout
    private lateinit var assistantBubble: Button
    private lateinit var assistantQuestion: EditText
    private lateinit var assistantAnswer: TextView
    private lateinit var commandHubButton: Button
    private lateinit var viewModeButton: Button
    private lateinit var bottomToolbar: LinearLayout
    private lateinit var splitSnapshot: TextView
    private lateinit var splitHost: LinearLayout
    private lateinit var uiSettingsStore: WorkspaceUiSettingsStore
    private val commandHubController = CommandHubController()
    private val bottomToolbarController = BottomToolbarController()
    private val workspaceViewController = WorkspaceViewController()
    private val zoomController = PerViewZoomController()
    private val zoomBaseSp = mutableMapOf<ZoomViewKey, Float>()
    private lateinit var zoomPrefs: android.content.SharedPreferences
    private var rememberZoomPerView: Boolean = true
    private var currentViewMode = WorkspaceViewMode.FOCUS
    private var loadedSnapshotText: String = ""
    private var loadedSnapshotPath: String? = null

    private val panels = linkedMapOf<WorkspaceSection, View>()

    private val chooseApk = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            sourceUri = uri
            sourceName = queryName(uri) ?: "selected.apk"
            status.text = "Selected: $sourceName\nCreate Workspace to begin editing."
            outputName.setText(sourceName.substringBeforeLast('.') + "_modified_unsigned.apk")
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
                "Exported rebuilt unsigned APK. Sign it with your own authorized signing identity before installation."
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_modify, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        bindViews(view)
        initializeV85Ui(view)
        bindSections(view)
        bindWorkspaceActions(view)
        bindManifestActions(view)
        bindSearchActions(view)
        bindConverterActions(view)
        bindCodeTools(view)
        bindDiffActions(view)
        bindBuildActions(view)
        bindAssistant(view)
        bindCommandHub()
        bindViewMode()
        bindPinchZoom()
        bindHelpAndSuggestions(view)

        sourceUri = homeVm.selectedUri.value
        if (sourceUri != null) {
            sourceName = queryName(sourceUri!!) ?: "selected.apk"
            status.text = "Using Home selection: $sourceName"
            outputName.setText(sourceName.substringBeforeLast('.') + "_modified_unsigned.apk")
        } else {
            status.text = "Choose an APK or select one from Home first."
        }
        showSection(WorkspaceSection.FILES)
    }

    private fun bindViews(view: View) {
        status = view.findViewById(R.id.modifyStatus)
        mutationLog = view.findViewById(R.id.modifyMutationLog)
        entrySpinner = view.findViewById(R.id.modifyEntrySpinner)
        textEditor = view.findViewById(R.id.modifyTextEditor)
        fileList = view.findViewById(R.id.modifyFileList)
        versionName = view.findViewById(R.id.modifyVersionName)
        versionCode = view.findViewById(R.id.modifyVersionCode)
        appLabel = view.findViewById(R.id.modifyAppLabel)
        manifestState = view.findViewById(R.id.manifestState)
        searchQuery = view.findViewById(R.id.searchQuery)
        searchScope = view.findViewById(R.id.searchScope)
        searchResults = view.findViewById(R.id.searchResults)
        converterInput = view.findViewById(R.id.converterInput)
        converterType = view.findViewById(R.id.converterType)
        converterOutput = view.findViewById(R.id.converterOutput)
        languageInput = view.findViewById(R.id.languageInput)
        languageSource = view.findViewById(R.id.languageSource)
        languageTarget = view.findViewById(R.id.languageTarget)
        languagePreserve = view.findViewById(R.id.languagePreserve)
        languageOutput = view.findViewById(R.id.languageOutput)
        smaliDescriptor = view.findViewById(R.id.smaliDescriptor)
        smaliExplainInput = view.findViewById(R.id.smaliExplainInput)
        smaliSnippet = view.findViewById(R.id.smaliSnippet)
        smaliPreview = view.findViewById(R.id.smaliPreview)
        diffOutput = view.findViewById(R.id.diffOutput)
        buildOutput = view.findViewById(R.id.buildOutput)
        outputName = view.findViewById(R.id.outputName)
        assistantPanel = view.findViewById(R.id.assistantPanel)
        assistantBubble = view.findViewById(R.id.assistantBubble)
        assistantQuestion = view.findViewById(R.id.assistantQuestion)
        assistantAnswer = view.findViewById(R.id.assistantAnswer)

        searchScope.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, SearchScope.values().map { it.name })
        converterType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("Auto", "Decimal", "Hex", "Binary", "Octal", "Base64", "Text"))
        languageSource.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, MlKitLanguageTranslator.supportedNames)
        languageTarget.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, MlKitLanguageTranslator.supportedNames.filterNot { it == "Auto" })
        languageTarget.setSelection(MlKitLanguageTranslator.supportedNames.filterNot { it == "Auto" }.indexOf("Malay").coerceAtLeast(0))
        smaliSnippet.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, SmaliQuickCode.catalog.map { "${it.category} • ${it.title}" })
    }

    private fun bindSections(view: View) {
        panels[WorkspaceSection.FILES] = view.findViewById(R.id.panelFiles)
        panels[WorkspaceSection.MANIFEST] = view.findViewById(R.id.panelManifest)
        panels[WorkspaceSection.SEARCH] = view.findViewById(R.id.panelSearch)
        panels[WorkspaceSection.CONVERTER] = view.findViewById(R.id.panelConverter)
        panels[WorkspaceSection.CODE_TOOLS] = view.findViewById(R.id.panelCodeTools)
        panels[WorkspaceSection.DIFF] = view.findViewById(R.id.panelDiff)
        panels[WorkspaceSection.BUILD] = view.findViewById(R.id.panelBuild)
        mapOf(
            R.id.tabFiles to WorkspaceSection.FILES,
            R.id.tabManifest to WorkspaceSection.MANIFEST,
            R.id.tabSearch to WorkspaceSection.SEARCH,
            R.id.tabConverter to WorkspaceSection.CONVERTER,
            R.id.tabCodeTools to WorkspaceSection.CODE_TOOLS,
            R.id.tabDiff to WorkspaceSection.DIFF,
            R.id.tabBuild to WorkspaceSection.BUILD
        ).forEach { (id, section) -> view.findViewById<Button>(id).setOnClickListener { showSection(section) } }

        // V8.5 uses the floating Command Hub as the primary navigation.
        // Keep legacy tab IDs in the layout for compatibility but remove the permanent row from view.
        (view.findViewById<Button>(R.id.tabFiles).parent?.parent as? View)?.visibility = View.GONE
    }

    private fun bindWorkspaceActions(view: View) {
        view.findViewById<Button>(R.id.modifyChooseApk).setOnClickListener {
            chooseApk.launch(arrayOf("application/vnd.android.package-archive", "application/zip", "*/*"))
        }
        view.findViewById<Button>(R.id.modifyCreateWorkspace).setOnClickListener {
            val uri = sourceUri
            if (uri == null) { status.text = "Choose an APK first."; return@setOnClickListener }
            runIo("Creating isolated workspace…") {
                val workspace = File(requireContext().cacheDir, "modify_workspace_v84")
                val newEngine = ApkWorkspaceEngine(workspace)
                requireContext().contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Unable to open APK." }
                    newEngine.extract(input, sourceName)
                }
                engine = newEngine
                rebuiltFile = null
                "Workspace ready. Original APK remains unchanged."
            }
        }
        view.findViewById<Button>(R.id.modifyLoadText).setOnClickListener {
            val path = selectedPath() ?: return@setOnClickListener
            runIo("Loading $path…") {
                val entry = requireEngine().allEntries(sourceName).firstOrNull { it.path == path }
                    ?: error("Entry not found.")
                if (entry.textEditable) {
                    val text = requireEngine().readText(path)
                    withContext(Dispatchers.Main) {
                        textEditor.setText(text)
                        loadedSnapshotText = text
                        loadedSnapshotPath = path
                        renderLoadedSnapshot()
                    }
                    "Loaded editable plaintext: $path"
                } else {
                    val bytes = requireEngine().readEntryBytes(path)
                    val message = if (bytes == null) "Preview unavailable or entry exceeds preview limit." else "Read-only/binary entry: $path (${bytes.size} bytes). Use Analyze/Search for static evidence."
                    withContext(Dispatchers.Main) {
                        textEditor.setText("")
                        loadedSnapshotText = ""
                        loadedSnapshotPath = path
                        renderLoadedSnapshot()
                    }
                    message
                }
            }
        }
        view.findViewById<Button>(R.id.modifySaveText).setOnClickListener {
            val path = selectedPath() ?: return@setOnClickListener
            val text = textEditor.text.toString()
            runIo("Saving $path…") {
                requireEngine().writeText(path, text)
                "Saved plaintext entry: $path"
            }
        }
        view.findViewById<Button>(R.id.modifyReplaceFile).setOnClickListener {
            val path = selectedPath() ?: return@setOnClickListener
            val entry = engine?.allEntries(sourceName)?.firstOrNull { it.path == path }
            if (entry?.editable != true || path == "AndroidManifest.xml") {
                status.text = "Selected entry is protected/LIMITED or requires the Manifest panel."
                return@setOnClickListener
            }
            pendingReplacePath = path
            chooseReplacement.launch(arrayOf("*/*"))
        }
        view.findViewById<Button>(R.id.modifyBack).setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun bindManifestActions(view: View) {
        view.findViewById<Button>(R.id.modifyManifestApply).setOnClickListener {
            val name = versionName.text.toString().trim().ifBlank { null }
            val codeRaw = versionCode.text.toString().trim()
            val code = if (codeRaw.isBlank()) null else codeRaw.toLongOrNull()
            if (codeRaw.isNotBlank() && (code == null || code < 0)) { status.text = "versionCode must be a non-negative integer."; return@setOnClickListener }
            val label = appLabel.text.toString().trim().ifBlank { null }
            AlertDialog.Builder(requireContext())
                .setTitle("Apply plaintext manifest changes?")
                .setMessage("versionName=${name ?: "unchanged"}\nversionCode=${code ?: "unchanged"}\nappLabel=${label ?: "unchanged"}\n\nBinary AXML remains LIMITED.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply") { _, _ ->
                    runIo("Updating plaintext manifest metadata…") {
                        requireEngine().updatePlaintextManifest(name, code, label)
                    }
                }.show()
        }
    }

    private fun bindSearchActions(view: View) {
        view.findViewById<Button>(R.id.searchPathButton).setOnClickListener { runPathSearch() }
        view.findViewById<Button>(R.id.searchContentButton).setOnClickListener { runContentSearch() }
        view.findViewById<Button>(R.id.searchOpenFirst).setOnClickListener {
            val path = lastSearchHits.firstOrNull()?.path
            if (path == null) status.text = "No search result to open." else {
                selectEntry(path); showSection(WorkspaceSection.FILES); status.text = "Selected from search: $path"
            }
        }
    }

    private fun bindConverterActions(view: View) {
        view.findViewById<Button>(R.id.converterRun).setOnClickListener {
            val raw = converterInput.text.toString()
            val forced = when (converterType.selectedItemPosition) {
                1 -> DetectedInputType.DECIMAL
                2 -> DetectedInputType.HEX
                3 -> DetectedInputType.BINARY
                4 -> DetectedInputType.OCTAL
                5 -> DetectedInputType.BASE64
                6 -> DetectedInputType.TEXT
                else -> null
            }
            val result = runCatching { DataConverter.convert(raw, forced) }
            converterOutput.text = result.fold(onSuccess = { formatConversion(it) }, onFailure = { "Conversion error: ${it.message}" })
        }
        view.findViewById<Button>(R.id.converterCopy).setOnClickListener { copyText("conversion", converterOutput.text.toString()) }
        view.findViewById<Button>(R.id.converterInsert).setOnClickListener { insertAtCursor(converterOutput.text.toString()) }
        view.findViewById<Button>(R.id.converterSearch).setOnClickListener {
            searchQuery.setText(converterInput.text.toString())
            showSection(WorkspaceSection.SEARCH)
            runContentSearch()
        }
        view.findViewById<Button>(R.id.languageTranslate).setOnClickListener {
            val request = LanguageRequest(
                text = languageInput.text.toString(),
                sourceLanguage = languageSource.selectedItem?.toString() ?: "Auto",
                targetLanguage = languageTarget.selectedItem?.toString() ?: "Malay",
                preserveFormat = languagePreserve.isChecked
            )
            status.text = "Preparing language conversion…"
            translator.translate(request, onStatus = { message -> if (isAdded) status.text = message }) { result ->
                if (!isAdded) return@translate
                requireActivity().runOnUiThread {
                    languageOutput.text = result.fold(onSuccess = { it }, onFailure = { "Translation error: ${it.message}" })
                    status.text = if (result.isSuccess) "Translation complete. Review before inserting/applying." else "Translation failed."
                }
            }
        }
        view.findViewById<Button>(R.id.languageCopy).setOnClickListener { copyText("translation", languageOutput.text.toString()) }
        view.findViewById<Button>(R.id.languageInsert).setOnClickListener { insertAtCursor(languageOutput.text.toString()) }
    }

    private fun bindCodeTools(view: View) {
        view.findViewById<Button>(R.id.smaliSuggestReturn).setOnClickListener {
            val suggestion = SmaliQuickCode.suggestReturn(smaliDescriptor.text.toString())
            val snippet = suggestion.snippet
            if (snippet == null) smaliPreview.text = suggestion.reason else {
                val index = SmaliQuickCode.catalog.indexOfFirst { it.id == snippet.id }
                if (index >= 0) smaliSnippet.setSelection(index)
                smaliPreview.text = formatSnippet(snippet.code, snippet.explanation, snippet.registers, suggestion.reason)
            }
        }
        view.findViewById<Button>(R.id.smaliExplain).setOnClickListener {
            val snippet = SmaliQuickCode.catalog.getOrNull(smaliSnippet.selectedItemPosition)
            val line = smaliExplainInput.text.toString().trim().ifBlank { snippet?.code.orEmpty() }
            smaliPreview.text = formatSnippet(snippet?.code.orEmpty(), SmaliQuickCode.explain(line), snippet?.registers ?: "n/a", "Preview only until Insert is pressed.")
        }
        view.findViewById<Button>(R.id.smaliInsert).setOnClickListener {
            val snippet = SmaliQuickCode.catalog.getOrNull(smaliSnippet.selectedItemPosition)
            if (snippet == null) status.text = "Choose a snippet first." else {
                insertAtCursor(snippet.code)
                status.text = "Snippet inserted into text editor scratchpad. It is not applied to DEX/native code automatically."
            }
        }
    }

    private fun bindDiffActions(view: View) {
        view.findViewById<Button>(R.id.diffRefresh).setOnClickListener { refreshDiff() }
        view.findViewById<Button>(R.id.modifyUndo).setOnClickListener {
            runIo("Undoing last mutation…") { if (requireEngine().undoLast()) "Last mutation restored." else "Nothing to undo." }
        }
        view.findViewById<Button>(R.id.diffUndoSelected).setOnClickListener {
            val path = selectedPath() ?: return@setOnClickListener
            runIo("Undoing latest change for $path…") { if (requireEngine().undoLatestForPath(path)) "Restored latest change for $path." else "No mutation found for $path." }
        }
    }

    private fun bindBuildActions(view: View) {
        view.findViewById<Button>(R.id.buildValidate).setOnClickListener { refreshPreflight() }
        view.findViewById<Button>(R.id.modifyRebuild).setOnClickListener {
            val e = engine
            val report = BuildPreflight.check(e?.isReady() == true, e?.allEntries(sourceName)?.size ?: 0, e?.mutationCount() ?: 0)
            refreshPreflight(report)
            if (!report.ready) { status.text = "Build blocked by preflight."; return@setOnClickListener }
            val requestedName = safeOutputName(outputName.text.toString())
            runIo("Rebuilding modified APK archive…") {
                val output = File(requireContext().cacheDir, requestedName)
                rebuiltFile = requireEngine().rebuild(output)
                "Rebuild complete: ${output.name}\nUnsigned by design. Original APK was not overwritten."
            }
        }
        view.findViewById<Button>(R.id.modifyExport).setOnClickListener {
            val file = rebuiltFile
            if (file == null || !file.isFile) status.text = "Rebuild the APK before export." else exportApk.launch(safeOutputName(outputName.text.toString()))
        }
    }

    private fun bindAssistant(view: View) {
        view.findViewById<Button>(R.id.assistantClose).setOnClickListener { assistantPanel.visibility = View.GONE }
        view.findViewById<Button>(R.id.assistantSend).setOnClickListener { sendAssistantQuestion() }
        view.findViewById<Button>(R.id.assistantConvert).setOnClickListener {
            assistantQuestion.setText("convert ${converterInput.text}")
            sendAssistantQuestion()
        }
        view.findViewById<Button>(R.id.assistantSearch).setOnClickListener {
            val value = assistantQuestion.text.toString().trim().ifBlank { converterInput.text.toString().trim() }
            searchQuery.setText(value)
            assistantPanel.visibility = View.GONE
            showSection(WorkspaceSection.SEARCH)
            runContentSearch()
        }
        var downX = 0f; var downY = 0f; var startTx = 0f; var startTy = 0f; var moved = false
        assistantBubble.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY; startTx = v.translationX; startTy = v.translationY; moved = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX; val dy = event.rawY - downY
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) moved = true
                    v.translationX = startTx + dx; v.translationY = startTy + dy; true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) assistantPanel.visibility = if (assistantPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    true
                }
                else -> false
            }
        }
    }

    private fun bindHelpAndSuggestions(view: View) {
        view.findViewById<Button>(R.id.helpSearch).setOnClickListener { showHelp("search", searchQuery) }
        view.findViewById<Button>(R.id.helpVersionName).setOnClickListener { showHelp("versionName", versionName) }
        view.findViewById<Button>(R.id.helpVersionCode).setOnClickListener { showHelp("versionCode", versionCode) }
        view.findViewById<Button>(R.id.helpAppLabel).setOnClickListener { showHelp("appLabel", appLabel) }
        view.findViewById<Button>(R.id.helpConverter).setOnClickListener { showHelp("converter", converterInput) }
        view.findViewById<Button>(R.id.helpLanguage).setOnClickListener { showHelp("language", languageInput) }
        view.findViewById<Button>(R.id.helpSmali).setOnClickListener { showHelp("smali", smaliDescriptor) }
        view.findViewById<Button>(R.id.helpOutputName).setOnClickListener { showHelp("outputName", outputName) }

        view.findViewById<Button>(R.id.suggestSearch).setOnClickListener { applySuggestion("search", searchQuery) }
        view.findViewById<Button>(R.id.suggestVersionName).setOnClickListener { applySuggestion("versionName", versionName) }
        view.findViewById<Button>(R.id.suggestVersionCode).setOnClickListener { applySuggestion("versionCode", versionCode) }
        view.findViewById<Button>(R.id.suggestAppLabel).setOnClickListener { applySuggestion("appLabel", appLabel) }
        view.findViewById<Button>(R.id.suggestConverter).setOnClickListener { applySuggestion("converter", converterInput) }
        view.findViewById<Button>(R.id.suggestLanguage).setOnClickListener { applySuggestion("language", languageInput) }
        view.findViewById<Button>(R.id.suggestSmali).setOnClickListener { applySuggestion("smali", smaliDescriptor) }
        view.findViewById<Button>(R.id.suggestOutputName).setOnClickListener { applySuggestion("outputName", outputName) }
    }

    private fun initializeV85Ui(view: View) {
        val prefs = requireContext().getSharedPreferences("workspace_ui_v85", Context.MODE_PRIVATE)
        uiSettingsStore = WorkspaceUiSettingsStore(SharedPreferencesSettingsBackend(prefs))
        val settings = uiSettingsStore.load()
        applyCompactUi(view, settings)
        zoomPrefs = requireContext().getSharedPreferences("workspace_zoom_v85", Context.MODE_PRIVATE)
        rememberZoomPerView = settings.rememberZoomPerView

        val root = view as? FrameLayout ?: return

        installSplitHost(settings)

        viewModeButton = Button(requireContext()).apply {
            text = "Focus"
            contentDescription = "Change workspace view mode"
            textSize = (11f * settings.uiScalePercent / 100f).coerceIn(10f, 14f)
            minWidth = 0
            minHeight = 0
            setPadding(dp(10), 0, dp(10), 0)
        }

        commandHubButton = Button(requireContext()).apply {
            text = bottomToolbarController.commandHubLabel()
            contentDescription = "Open Command Hub"
            textSize = (11f * settings.uiScalePercent / 100f).coerceIn(10f, 14f)
            minWidth = 0
            minHeight = 0
            setPadding(dp(10), 0, dp(10), 0)
        }

        bottomToolbar = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
            elevation = dp(8).toFloat()
            addView(
                viewModeButton,
                LinearLayout.LayoutParams(0, dp(42), 1f).apply {
                    marginEnd = dp(4)
                }
            )
            addView(
                commandHubButton,
                LinearLayout.LayoutParams(0, dp(42), 1f).apply {
                    marginStart = dp(4)
                }
            )
        }

        root.addView(
            bottomToolbar,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(50),
                Gravity.BOTTOM
            )
        )

        view.findViewById<View>(R.id.modifyMainScroll)?.apply {
            setPadding(paddingLeft, paddingTop, paddingRight, dp(58))
        }

        currentViewMode = settings.defaultViewMode.toRuntimeViewMode()
    }

    private fun bindCommandHub() {
        if (!::commandHubButton.isInitialized) return
        commandHubButton.setOnClickListener { showCommandHub() }
    }

    private fun installSplitHost(settings: WorkspaceUiSettings) {
        val parent = textEditor.parent as? LinearLayout ?: return
        val editorIndex = parent.indexOfChild(textEditor)
        if (editorIndex < 0) return

        parent.removeView(textEditor)

        splitSnapshot = TextView(requireContext()).apply {
            text = "No loaded snapshot yet."
            textSize = (settings.editorFontSp * settings.uiScalePercent / 100f).coerceIn(9f, 24f)
            setTextColor(textEditor.currentTextColor)
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setTextIsSelectable(true)
            setHorizontallyScrolling(false)
            visibility = View.GONE
        }

        val widthDp = resources.configuration.screenWidthDp
        val horizontalSplit = widthDp >= 600 || resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        splitHost = LinearLayout(requireContext()).apply {
            orientation = if (horizontalSplit) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        }

        if (horizontalSplit) {
            splitHost.addView(
                splitSnapshot,
                LinearLayout.LayoutParams(0, dp(260), 1f).apply { marginEnd = dp(6) }
            )
            splitHost.addView(
                textEditor,
                LinearLayout.LayoutParams(0, dp(260), 1f)
            )
        } else {
            splitHost.addView(
                splitSnapshot,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(180)).apply {
                    bottomMargin = dp(6)
                }
            )
            splitHost.addView(
                textEditor,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(240))
            )
        }

        parent.addView(
            splitHost,
            editorIndex,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun bindViewMode() {
        if (!::viewModeButton.isInitialized) return
        viewModeButton.setOnClickListener { showViewModePicker() }
        applyViewMode(currentViewMode, persist = false)
    }

    private fun showViewModePicker() {
        val modes = WorkspaceViewMode.values()
        val labels = modes.map(workspaceViewController::titleFor).toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Workspace View")
            .setSingleChoiceItems(labels, currentViewMode.ordinal) { dialog, which ->
                applyViewMode(modes[which], persist = true)
                dialog.dismiss()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun applyViewMode(mode: WorkspaceViewMode, persist: Boolean) {
        currentViewMode = mode
        val state = workspaceViewController.stateFor(mode)

        entrySpinner.visibility = if (state.showNavigator) View.VISIBLE else View.GONE
        view?.findViewById<Button>(R.id.modifyLoadText)?.visibility =
            if (state.showNavigator) View.VISIBLE else View.GONE
        view?.findViewById<Button>(R.id.modifyReplaceFile)?.visibility =
            if (state.showNavigator) View.VISIBLE else View.GONE
        fileList.visibility = if (state.showInventory) View.VISIBLE else View.GONE
        splitSnapshot.visibility = if (state.showSnapshot) View.VISIBLE else View.GONE
        textEditor.visibility = if (state.showEditor) View.VISIBLE else View.GONE

        if (state.showSnapshot) renderLoadedSnapshot()

        viewModeButton.text = bottomToolbarController.viewLabel(mode)
        viewModeButton.contentDescription = "Workspace view: ${state.label}"

        if (persist) {
            val saved = uiSettingsStore.load()
            uiSettingsStore.save(saved.copy(defaultViewMode = mode.toSettingViewMode()))
        }

        status.text = when (mode) {
            WorkspaceViewMode.FOCUS -> "Focus view: editor prioritized; navigator and inventory hidden."
            WorkspaceViewMode.SPLIT -> "Split view: loaded snapshot and current editor are shown together."
            WorkspaceViewMode.INSPECT -> "Inspect view: navigator, editor, and file inventory are visible."
        }
    }

    private fun renderLoadedSnapshot() {
        if (!::splitSnapshot.isInitialized) return
        splitSnapshot.text = if (loadedSnapshotText.isBlank()) {
            "No plaintext snapshot loaded for ${loadedSnapshotPath ?: "this selection"}."
        } else {
            buildString {
                append("LOADED SNAPSHOT")
                loadedSnapshotPath?.let { append(" — ").append(it) }
                append("\n\n")
                append(loadedSnapshotText)
            }
        }
    }

    private fun WorkspaceViewModeSetting.toRuntimeViewMode(): WorkspaceViewMode = when (this) {
        WorkspaceViewModeSetting.FOCUS -> WorkspaceViewMode.FOCUS
        WorkspaceViewModeSetting.SPLIT -> WorkspaceViewMode.SPLIT
        WorkspaceViewModeSetting.INSPECT -> WorkspaceViewMode.INSPECT
    }

    private fun WorkspaceViewMode.toSettingViewMode(): WorkspaceViewModeSetting = when (this) {
        WorkspaceViewMode.FOCUS -> WorkspaceViewModeSetting.FOCUS
        WorkspaceViewMode.SPLIT -> WorkspaceViewModeSetting.SPLIT
        WorkspaceViewMode.INSPECT -> WorkspaceViewModeSetting.INSPECT
    }

    private fun bindPinchZoom() {
        val targets = listOf(
            Triple(ZoomViewKey.EDITOR, textEditor as TextView, "Editor"),
            Triple(ZoomViewKey.SNAPSHOT, splitSnapshot, "Snapshot"),
            Triple(ZoomViewKey.SEARCH, searchResults, "Search"),
            Triple(ZoomViewKey.DIFF, diffOutput, "Diff"),
            Triple(ZoomViewKey.BUILD, buildOutput, "Build"),
            Triple(ZoomViewKey.AI, assistantAnswer, "AI")
        )
        targets.forEach { (key, target, label) ->
            attachPinchZoom(key, target, label)
        }
    }

    private fun attachPinchZoom(key: ZoomViewKey, target: TextView, label: String) {
        val scaledDensity = resources.displayMetrics.scaledDensity.coerceAtLeast(0.1f)
        zoomBaseSp.putIfAbsent(key, target.textSize / scaledDensity)

        val restored = if (rememberZoomPerView) {
            zoomPrefs.getInt("zoom_${key.storageKey}", 100)
        } else {
            100
        }
        zoomController.setPercent(key, restored)
        applyZoomToView(key, target)

        var doubleTapTriggered = false
        val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    doubleTapTriggered = true
                    val percent = zoomController.reset(key)
                    applyZoomToView(key, target)
                    saveZoomPercent(key, percent)
                    status.text = "$label zoom: 100% (reset)"
                    return true
                }
            }
        )

        val scaleDetector = ScaleGestureDetector(
            requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val percent = zoomController.scale(key, detector.scaleFactor)
                    applyZoomToView(key, target)
                    status.text = "$label zoom: ${percent}% • double-tap to reset"
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    saveZoomPercent(key, zoomController.percent(key))
                }
            }
        )

        target.setOnTouchListener { _, event ->
            doubleTapTriggered = false
            gestureDetector.onTouchEvent(event)
            scaleDetector.onTouchEvent(event)
            doubleTapTriggered || scaleDetector.isInProgress
        }
    }

    private fun applyZoomToView(key: ZoomViewKey, target: TextView) {
        val baseSp = zoomBaseSp[key] ?: return
        val percent = zoomController.percent(key)
        target.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            baseSp * percent / 100f
        )
    }

    private fun saveZoomPercent(key: ZoomViewKey, percent: Int) {
        if (!rememberZoomPerView) return
        zoomPrefs.edit().putInt("zoom_${key.storageKey}", percent).apply()
    }

    private fun showCommandHub() {
        val actions = commandHubController.visibleActions()
        val labels = actions.map(commandHubController::titleFor).toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("\u26A1 Command Hub")
            .setItems(labels) { _, which -> handleCommandHubAction(actions[which]) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun handleCommandHubAction(action: CommandHubAction) {
        when (action) {
            CommandHubAction.OPEN_FILE -> showSection(WorkspaceSection.FILES)
            CommandHubAction.MANIFEST -> showSection(WorkspaceSection.MANIFEST)
            CommandHubAction.SEARCH -> showSection(WorkspaceSection.SEARCH)
            CommandHubAction.REPLACE -> {
                showSection(WorkspaceSection.FILES)
                status.text = "Select an existing replaceable resource/asset, then use Replace."
            }
            CommandHubAction.CONVERTER,
            CommandHubAction.LANGUAGE -> showSection(WorkspaceSection.CONVERTER)
            CommandHubAction.CODE -> showSection(WorkspaceSection.CODE_TOOLS)
            CommandHubAction.DIFF -> showSection(WorkspaceSection.DIFF)
            CommandHubAction.BUILD -> showSection(WorkspaceSection.BUILD)
            CommandHubAction.AI -> {
                assistantPanel.visibility = if (assistantPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            CommandHubAction.SETTINGS -> showUiSettingsSummary()
            CommandHubAction.RECENT,
            CommandHubAction.FAVORITES,
            CommandHubAction.CRYPTO,
            CommandHubAction.COLOR -> {
                status.text = "${action.title} is staged for the next V8.5 milestone."
            }
        }
    }

    private fun showUiSettingsSummary() {
        val settings = uiSettingsStore.load()
        AlertDialog.Builder(requireContext())
            .setTitle("V8.5 Workspace UI")
            .setMessage(
                "UI scale: ${settings.uiScalePercent}%\n" +
                    "Font: ${settings.fontSp}sp\n" +
                    "Editor: ${settings.editorFontSp}sp\n" +
                    "Buttons: ${settings.buttonSize}\n" +
                    "Default view: ${settings.defaultViewMode}\n" +
                    "Remember zoom per view: ${settings.rememberZoomPerView}\n" +
                    "Auto-hide UI: ${settings.autoHideUi}\n\n" +
                    "Full settings editor is added in the next workspace task."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun applyCompactUi(view: View, settings: WorkspaceUiSettings) {
        val scale = settings.uiScalePercent / 100f
        if (view is Button) {
            view.textSize = (settings.fontSp * scale).coerceIn(10f, 22f)
            view.minHeight = dp(
                when (settings.buttonSize) {
                    ButtonSize.SMALL -> 40
                    ButtonSize.MEDIUM -> 48
                    ButtonSize.LARGE -> 56
                }
            )
            val horizontal = dp(if (settings.buttonSize == ButtonSize.SMALL) 8 else 12)
            val vertical = dp(if (settings.buttonSize == ButtonSize.SMALL) 3 else 6)
            view.setPadding(horizontal, vertical, horizontal, vertical)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyCompactUi(view.getChildAt(i), settings)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()
    private fun showSection(section: WorkspaceSection) {
        panels.forEach { (key, panel) -> panel.visibility = if (key == section) View.VISIBLE else View.GONE }
        when (section) {
            WorkspaceSection.DIFF -> refreshDiff()
            WorkspaceSection.BUILD -> refreshPreflight()
            WorkspaceSection.MANIFEST -> refreshManifest()
            else -> Unit
        }
    }

    private fun runPathSearch() {
        val e = engine ?: run { searchResults.text = "Create workspace first."; return }
        val entries = e.allEntries(sourceName).map { SearchEntry(it.path, it.size, it.editable, it.textEditable) }
        lastSearchHits = WorkspaceSearch.searchPaths(entries, searchQuery.text.toString(), selectedScope())
        renderSearchHits()
    }

    private fun runContentSearch() {
        val e = engine ?: run { searchResults.text = "Create workspace first."; return }
        val entries = e.allEntries(sourceName).map { SearchEntry(it.path, it.size, it.editable, it.textEditable) }
        lastSearchHits = WorkspaceSearch.searchContent(entries, searchQuery.text.toString(), selectedScope()) { path, max -> e.readEntryBytes(path, max) }
        renderSearchHits()
    }

    private fun renderSearchHits() {
        searchResults.text = if (lastSearchHits.isEmpty()) "No matches." else lastSearchHits.take(100).mapIndexed { index, hit ->
            "${index + 1}. [${hit.kind}] ${hit.path}\n   matches=${hit.matchCount} • ${if (hit.textEditable) "EDITABLE TEXT" else if (hit.editable) "REPLACEABLE" else "READ-ONLY"}\n   ${hit.context}"
        }.joinToString("\n\n") + if (lastSearchHits.size > 100) "\n\n… ${lastSearchHits.size - 100} more result(s) not displayed." else ""
        status.text = "Search complete: ${lastSearchHits.size} result(s)."
    }

    private fun selectedScope(): SearchScope = SearchScope.values().getOrElse(searchScope.selectedItemPosition) { SearchScope.ALL }

    private fun refreshAll() {
        refreshEntries(); refreshManifest(); refreshDiff(); refreshPreflight()
    }

    private fun refreshEntries() {
        val e = engine ?: return
        val entries = e.allEntries(sourceName)
        entryPaths = entries.map { it.path }
        val labels = entries.map { entry ->
            val state = when {
                entry.textEditable -> "TEXT"
                entry.editable -> "FILE"
                else -> "READ-ONLY"
            }
            "${entry.path}  •  $state  •  ${entry.size} B"
        }
        entrySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)
        fileList.text = entries.take(160).joinToString("\n") { entry ->
            val state = when { entry.textEditable -> "EDIT"; entry.editable -> "REPLACE"; else -> "RO" }
            "[$state] ${entry.path} (${entry.size} B)"
        } + if (entries.size > 160) "\n… ${entries.size - 160} more entries. Use Search for exact files." else ""
        mutationLog.text = e.mutationLog().ifEmpty { listOf("No modifications yet.") }.joinToString("\n")
    }

    private fun refreshManifest() {
        val e = engine ?: run { manifestState.text = "Create workspace first."; return }
        val meta = runCatching { e.manifestMetadata() }.getOrNull()
        if (meta == null || !meta.plaintext) {
            manifestState.text = "LIMITED: AndroidManifest.xml is binary AXML or unavailable. Static analysis remains readable; arbitrary binary manifest rewrite is not claimed."
            return
        }
        manifestState.text = "EDITABLE plaintext manifest. Current values loaded below."
        if (!versionName.hasFocus()) versionName.setText(meta.versionName.orEmpty())
        if (!versionCode.hasFocus()) versionCode.setText(meta.versionCode?.toString().orEmpty())
        if (!appLabel.hasFocus()) appLabel.setText(meta.appLabel.orEmpty())
    }

    private fun refreshDiff() {
        val diffs = engine?.diffEntries().orEmpty()
        diffOutput.text = if (diffs.isEmpty()) "No modifications yet." else diffs.joinToString("\n\n") { d ->
            "${d.operation}: ${d.path}\n${d.beforeSize} B → ${d.afterSize} B • ${d.validationState}\n${d.preview}"
        }
    }

    private fun refreshPreflight(reportOverride: com.msa.patcher.modify.preflight.PreflightReport? = null) {
        val e = engine
        val report = reportOverride ?: BuildPreflight.check(e?.isReady() == true, e?.allEntries(sourceName)?.size ?: 0, e?.mutationCount() ?: 0)
        buildOutput.text = buildString {
            append(report.summary)
            if (report.errors.isNotEmpty()) append("\nErrors:\n").append(report.errors.joinToString("\n") { "• $it" })
            if (report.warnings.isNotEmpty()) append("\nWarnings:\n").append(report.warnings.joinToString("\n") { "• $it" })
        }
    }

    private fun showHelp(id: String, target: EditText?) {
        val spec = FieldHelp.get(id) ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(spec.title)
            .setMessage("${spec.description}\n\n${spec.example}")
            .setNegativeButton("Close", null)
            .setPositiveButton("Insert Example") { _, _ ->
                target?.setText(spec.example.substringAfter("Example:", spec.example).trim())
                target?.setSelection(target.text.length)
            }.show()
    }

    private fun applySuggestion(fieldId: String, target: EditText) {
        val meta = engine?.let { runCatching { it.manifestMetadata() }.getOrNull() }
        val ctx = SuggestionContext(sourceName, meta?.versionName, meta?.versionCode, meta?.appLabel, selectedPathSilently(), textEditor.text.toString().take(2000))
        val suggestions = WorkspaceSuggestions.forField(fieldId, ctx)
        if (suggestions.isEmpty()) { status.text = "No suggestion available for this field."; return }
        val labels = suggestions.map { "${it.value} — ${it.reason}" }.toTypedArray()
        AlertDialog.Builder(requireContext()).setTitle("Auto Suggest").setItems(labels) { _, which ->
            target.setText(suggestions[which].value); target.setSelection(target.text.length)
            status.text = "Suggestion inserted only. Press Apply/Save when ready."
        }.show()
    }

    private fun sendAssistantQuestion() {
        val question = assistantQuestion.text.toString().trim()
        if (question.isBlank()) { assistantAnswer.text = "Enter a question first."; return }
        assistantAnswer.text = LocalAssistant.answer(question, currentAssistantContext())
    }

    private fun currentAssistantContext(): AssistantContext = AssistantContext(
        sourceName = sourceName,
        selectedPath = selectedPathSilently(),
        selectedText = textEditor.text.toString().take(2000),
        searchQuery = searchQuery.text.toString().takeIf { it.isNotBlank() },
        converterValue = converterInput.text.toString().takeIf { it.isNotBlank() },
        buildError = buildOutput.text.toString().takeIf { it.contains("BLOCKED", true) || it.contains("error", true) }
    )

    private fun formatConversion(r: com.msa.patcher.modify.converter.ConversionResult): String = listOfNotNull(
        r.decimal?.let { "Decimal: $it" },
        r.hexadecimal?.let { "Hex: $it" },
        r.binary?.let { "Binary: $it" },
        r.octal?.let { "Octal: $it" },
        r.text?.let { "Text: $it" },
        r.base64?.let { "Base64: $it" },
        r.hexBytes?.let { "Hex bytes: $it" },
        r.urlEncoded?.let { "URL encoded: $it" },
        r.bigEndianHex?.let { "Big-endian hex: $it" },
        r.littleEndianHex?.let { "Little-endian hex: $it" },
        r.note.takeIf { it.isNotBlank() }?.let { "Note: $it" }
    ).joinToString("\n")

    private fun formatSnippet(code: String, explanation: String, registers: String, reason: String): String =
        "Snippet:\n$code\n\nRegisters: $registers\nExplanation: $explanation\n$reason"

    private fun copyText(label: String, text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        status.text = "Copied $label to clipboard."
    }

    private fun insertAtCursor(value: String) {
        val start = textEditor.selectionStart.coerceAtLeast(0)
        textEditor.text.insert(start, if (start > 0 && textEditor.text.getOrNull(start - 1) != '\n') "\n$value" else value)
        showSection(WorkspaceSection.FILES)
    }

    private fun selectEntry(path: String) {
        val index = entryPaths.indexOf(path)
        if (index >= 0) entrySpinner.setSelection(index)
    }

    private fun selectedPath(): String? {
        val path = selectedPathSilently()
        if (path == null) status.text = "Create a workspace and select an entry first."
        return path
    }

    private fun selectedPathSilently(): String? = entryPaths.getOrNull(entrySpinner.selectedItemPosition)

    private fun safeOutputName(raw: String): String {
        val base = raw.trim().ifBlank { sourceName.substringBeforeLast('.') + "_modified_unsigned.apk" }
            .replace('/', '_').replace('\\', '_')
        return if (base.endsWith(".apk", true)) base else "$base.apk"
    }

    private fun requireEngine(): ApkWorkspaceEngine = requireNotNull(engine) { "Create Workspace first." }

    private fun runIo(start: String, block: suspend () -> String) {
        status.text = start
        viewLifecycleOwner.lifecycleScope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { block() } }
            status.text = result.getOrElse { "Error: ${it.message ?: it.javaClass.simpleName}" }
            if (result.isSuccess) refreshAll()
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
