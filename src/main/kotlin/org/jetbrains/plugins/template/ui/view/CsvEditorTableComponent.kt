package org.jetbrains.plugins.template.ui.view

import com.intellij.ui.components.JBScrollPane
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseEvent
import javax.swing.AbstractCellEditor
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

class FormulaCellRenderer(private val model: CsvEditorTableModel, private val defaultRenderer: TableCellRenderer) :
    TableCellRenderer {
    private val textField = JTextField()
    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        return if (hasFocus) {
            val formula = model.formulas[Pair(row, column)] // TODO data class
            textField.text = formula ?: value?.toString() ?: ""
            textField
        } else {
            defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        }
    }
}

class FormulaCellEditor(private val model: CsvEditorTableModel) : AbstractCellEditor(), TableCellEditor {
    private val textField = JTextField()

    override fun getCellEditorValue(): Any {
        return textField.text
    }

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        val formula = model.formulas[Pair(row, column)] // TODO data class
        textField.text = formula ?: value?.toString() ?: ""
        return textField
    }

    override fun isCellEditable(anEvent: java.util.EventObject?): Boolean {
        if (anEvent is MouseEvent) {
            return anEvent.clickCount > 1
        }
        return true
    }
}

class CsvEditorTableComponent(private var rows: Int, private var cols: Int) {
    val scrollPane: JScrollPane
    private val table: JTable

    init {
        val data = Array(rows) {
            arrayOfNulls<Any>(
                cols
            )
        }

        val columnNames = Array(cols) { i -> getColumnHeaderName(i) }
        val model = CsvEditorTableModel(data, columnNames)
        table = JTable(model)

        table.apply {
            setRowHeight(25)
            autoResizeMode = JTable.AUTO_RESIZE_OFF
            tableHeader.reorderingAllowed = false
            putClientProperty("JTable.autoStartsEdit", false)
        }

        for (i in 0 until table.columnCount) {
            table.columnModel.getColumn(i).apply {
                minWidth = 50
                cellRenderer = FormulaCellRenderer(model, DefaultTableCellRenderer())
                cellEditor = FormulaCellEditor(model)
            }
        }

        val rowHeaderTable: JTable = object : JTable(DefaultTableModel(rows, 1)) {
            override fun getValueAt(row: Int, column: Int): Any {
                return row + 1
            }
        }

        rowHeaderTable.apply {
            columnModel.getColumn(0).cellRenderer = table.tableHeader.defaultRenderer
            preferredScrollableViewportSize = Dimension(70, 0)
            setRowHeight(25)
            setEnabled(false)
            tableHeader.setReorderingAllowed(false)
            autoResizeMode = JTable.AUTO_RESIZE_OFF
        }
//        How to add column/row:
//        model.addColumn(null)
//        model.addRow(arrayOfNulls<Any>(cols))
//        (rowHeaderTable.model as DefaultTableModel).addRow(arrayOfNulls<Any>(cols))

        scrollPane =
            JBScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

        scrollPane.setRowHeaderView(rowHeaderTable)
    }

    private fun getColumnHeaderName(idx: Int): String =
        if (idx < 26)
            ('A' + idx).toString()
        else
            getColumnHeaderName(idx / 26 - 1) + ('A' + idx % 26).toString()
}