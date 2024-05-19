package org.jetbrains.plugins.template.ui.view

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import net.miginfocom.swing.MigLayout
import org.jetbrains.plugins.template.ui.view.controls.CsvEditorControlPanel
import java.awt.GridLayout
import javax.swing.BoxLayout
import javax.swing.JPanel

class CsvEditorViewComponent(project: Project, private val virtualFile: VirtualFile) : JPanel(), Disposable {
    val controlPanel = CsvEditorControlPanel()

    private val wrapperPanel = JPanel(MigLayout("flowy, fillx, ins 0, gap 0, hidemode 3")).apply {
        add(controlPanel, "growx, pushx")
    }

    init {
        Disposer.register(this, controlPanel)
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(JPanel(GridLayout(1, 1)).apply {
            add(wrapperPanel)
        })
    }

//    val controller = PdfPreviewControllerProvider.createViewController(project, virtualFile)
//    val searchPanel = PdfSearchPanel(this)
//
//    private val wrapperPanel = JPanel(MigLayout("flowy, fillx, ins 0, gap 0, hidemode 3")).apply {
//        add(controlPanel, "growx, pushx")
//        add(searchPanel, "growx, pushx")
//    }
//
//    init {
//        Disposer.register(this, controlPanel)
//        Disposer.register(this, searchPanel)
//        if (controller != null) {
//            Disposer.register(this, controller)
//        } else {
//            logger.warn("View controller is null!")
//        }
//        layout = BoxLayout(this, BoxLayout.Y_AXIS)
//        add(JPanel(GridLayout(1, 1)).apply {
//            add(wrapperPanel)
//        })
//        add(controller?.component ?: PdfUnsupportedViewPanel())
//        controller?.pipe?.subscribe<BrowserMessages.SearchResponse> {
//            searchPanel.updateResults(it.result.currentMatch, it.result.totalMatches)
//        }
//    }

    override fun dispose() = Unit

    companion object {
//        private val logger = logger<PdfEditorViewComponent>()
    }
}