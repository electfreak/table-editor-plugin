package org.jetbrains.plugins.template.ui.view

class CyclicDependencyError(s: String, val cell: Cell) : Exception(s)

class TableCellsGraph(dependencies: MutableMap<Cell, MutableSet<Cell>>) {
    private data class Node(
        val id: Cell,
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
    }

    private fun addEdgeFromDependency(from: Cell, to: Cell) {
        if (from == to) {
            throw CyclicDependencyError("Recursive dependency", from)
        }

        val fromNode = nodes.find { it.id == from } ?: Node(from).also { nodes.add(it) }
        val toNode = nodes.find { it.id == to } ?: Node(to).also { nodes.add(it) }
        fromNode.children.add(toNode)
    }

    private fun dfs(node: Node, topSort: MutableList<Cell>) {
        node.state = Node.State.InProcess

        for (child in node.children) {
            when (child.state) {
                Node.State.NotVisited -> dfs(child, topSort)
                Node.State.InProcess -> throw CyclicDependencyError("cycle", node.id)
                Node.State.Visited -> continue
            }
        }

        node.state = Node.State.Visited
        topSort.add(node.id)
    }

    private fun topSort(): List<Cell> {
        val sorted = mutableListOf<Cell>()
        for (node in nodes) {
            if (node.state == Node.State.NotVisited)
                dfs(node, sorted)
        }

        return sorted.reversed()
    }

    fun getOrder(): List<Cell> {
        return topSort()
    }
}