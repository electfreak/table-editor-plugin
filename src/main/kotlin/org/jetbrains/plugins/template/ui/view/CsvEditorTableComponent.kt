package org.jetbrains.plugins.template.ui.view

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.Dimension
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class CsvEditorTableComponent(val rows: Int, val cols: Int) {
    val scrollPane: JScrollPane
    private val table: JTable

    init {
        val data = Array(rows) {
            arrayOfNulls<Any>(
                cols
            )
        }

        val columnNames = arrayOfNulls<String>(cols)
        val model: DefaultTableModel = object : DefaultTableModel(data, columnNames) {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return true
            }
        }

        table = JBTable(model)
        table.apply {
            setRowHeight(25)
            autoResizeMode = JTable.AUTO_RESIZE_OFF
            tableHeader.reorderingAllowed = false
        }

        for (i in 0 until table.columnCount) {
            table.columnModel.getColumn(i).minWidth = 50
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

        model.addColumn(null)
        model.addRow(arrayOfNulls<Any>(cols))
        (rowHeaderTable.model as DefaultTableModel).addRow(arrayOfNulls<Any>(cols))

        scrollPane =
            JBScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

        scrollPane.setRowHeaderView(rowHeaderTable)
    }
}