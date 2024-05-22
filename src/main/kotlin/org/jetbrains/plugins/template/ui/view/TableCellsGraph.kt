package org.jetbrains.plugins.template.ui.view

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
    }

    private fun addEdgeFromDependency(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        if (from == to) {
            throw Error("Recursive dependency")
        }

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