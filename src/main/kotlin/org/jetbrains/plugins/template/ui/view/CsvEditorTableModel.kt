package org.jetbrains.plugins.template.ui.view

import org.jetbrains.plugins.template.termsolver.Cell
import org.jetbrains.plugins.template.termsolver.TermParser
import org.jetbrains.plugins.template.termsolver.TermSolver
import javax.swing.table.DefaultTableModel

class CsvEditorTableModel(data: Array<Array<Any?>>, columnNames: Array<String>) : DefaultTableModel(data, columnNames) {
    val formulas: MutableMap<Cell, String> = mutableMapOf()
    private val dependencies: MutableMap<Cell, Set<Cell>> = mutableMapOf()

    override fun setValueAt(value: Any?, row: Int, column: Int) {
        if (value is String && value.startsWith("=")) {
            formulas[Cell(row, column)] = value
            updateDependencies(value.substring(1), Cell(row, column))
        } else {
            formulas.remove(Cell(row, column))
            super.setValueAt(value, row, column)
        }

        updateFormulas()
    }

    private fun updateFormulas() {
        val cellsOrdered = try {
            TableCellsGraph(dependencies).getOrder()
        } catch (e: CyclicDependencyError) {
            formulas.remove(e.cell)
            dependencies.remove(e.cell)
            super.setValueAt("<${e.localizedMessage}>", e.cell.row, e.cell.col)
            TableCellsGraph(dependencies).getOrder()
        }

        cellsOrdered.forEach { (row, col) ->
            val formula = formulas[Cell(row, col)] ?: return@forEach
            val result = try {
                evaluateFormula(formula.substring(1))
            } catch (e: Exception) {
                formulas.remove(Cell(row, col))
                dependencies.remove(Cell(row, col))
                super.setValueAt("<could not execute formula>", row, col)
                return@forEach
            }

            super.setValueAt(result, row, col)
        }
    }

    private fun updateDependencies(formula: String, formulaCell: Cell) {
        dependencies[formulaCell] = TermParser
            .inputToTokens(formula)
            .filterIsInstance<Cell>()
            .toSet()
    }

    private fun getValueByCellReference(cell: Cell): Double {
        val value = getValueAt(cell.row, cell.col)?.toString() ?: error("empty cell")
        return value.toDouble()
    }

    private fun evaluateFormula(formula: String): Any {
        return TermSolver.evaluate(formula, ::getValueByCellReference)
    }
}