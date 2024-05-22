package org.jetbrains.plugins.template.ui.view

import javax.swing.DefaultCellEditor
import javax.swing.JTextField
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor

class TableCellsGraph(dependencies: MutableMap<Pair<Int, Int>, MutableSet<Pair<Int, Int>>>) {
    private data class Node(
        val id: Pair<Int, Int>,
        val children: MutableList<Node> = mutableListOf(),
        var state: State = State.NotVisited
    ) {
        enum class State { NotVisited, InProcess, Visited }
    }

    private val nodes: MutableSet<Node> = mutableSetOf()

    init {
        for ((fromCell, toCells) in dependencies) {
            for (toCell in toCells) {
                addEdgeFromDependency(fromCell, toCell)
            }
        }

        println("nodes: '$nodes'")
    }

    private fun addEdgeFromDependency(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        val fromNode = nodes.find { it.id == from } ?: Node(from).also { nodes.add(it) }
        val toNode = nodes.find { it.id == to } ?: Node(to).also { nodes.add(it) }
        fromNode.children.add(toNode)
    }

    private fun dfs(node: Node, topSort: MutableList<Pair<Int, Int>>) {
        node.state = Node.State.InProcess

        for (child in node.children) {
            when (child.state) {
                Node.State.NotVisited -> dfs(child, topSort)
                Node.State.InProcess -> throw Error("cycle")
                Node.State.Visited -> continue
            }
        }

        node.state = Node.State.Visited
        topSort.add(node.id)
    }

    private fun topSort(): List<Pair<Int, Int>> {
        val sorted = mutableListOf<Pair<Int, Int>>()
        try {
            for (node in nodes) {
                if (node.state == Node.State.NotVisited)
                    dfs(node, sorted)
            }
        } catch (e: Error) {
            throw e
        }

        return sorted.reversed()
    }

    fun getOrder(): List<Pair<Int, Int>> {
        return topSort()
    }
}

class CsvEditorTableModel(data: Array<Array<Any?>>, columnNames: Array<String>) : DefaultTableModel(data, columnNames) {
    val formulas: MutableMap<Pair<Int, Int>, String> = mutableMapOf()
    private val dependencies: MutableMap<Pair<Int, Int>, MutableSet<Pair<Int, Int>>> = mutableMapOf()
    private val formulasOrder
        get() = TableCellsGraph(dependencies).getOrder()

    override fun setValueAt(value: Any?, row: Int, column: Int) {
        if (value is String && value.startsWith("=")) {
            formulas[Pair(row, column)] = value
            super.setValueAt(evaluateFormula(value, row, column), row, column)
            return
        }

        formulas.remove(Pair(row, column))
        super.setValueAt(value, row, column)
        updateDependentCells(row, column)
    }

    private fun updateDependentCells(row: Int, column: Int) {
        println("update dependent cells $row, $column")
        println(formulas)
        val cell = Pair(row, column)
        dependencies[cell]?.forEach { dependentCell ->
            val (depRow, depCol) = dependentCell
            val formula = formulas[dependentCell]
            println("dependent cell: $depRow, $depCol, $formula")
            println("formulas order: $formulasOrder")
            if (formula != null) {
                val result = evaluateFormula(formula.substring(1), depRow, depCol)
                super.setValueAt(result, depRow, depCol)
            }
        }
    }

    private fun evaluateFormula(formula: String, formulaRow: Int, formulaColumn: Int): Any {
        val regex = Regex("([A-Z]+)([0-9]+)")
        val matchResult = regex.findAll(formula)
        var result = 0.0

        for (match in matchResult) {
            val column = match.groups[1]!!.value
            val row = match.groups[2]!!.value.toInt() - 1

            val colIndex = column[0] - 'A'
            val cellValue = getValueAt(row, colIndex)

            if (cellValue is Number) {
                result += cellValue.toDouble()
            } else if (cellValue is String) {
                result += cellValue.toDoubleOrNull() ?: 0.0
            }

            val dependentCell = Pair(formulaRow, formulaColumn)
            dependencies.getOrPut(Pair(row, colIndex)) { mutableSetOf() }.add(dependentCell)
        }

        return result
    }
}