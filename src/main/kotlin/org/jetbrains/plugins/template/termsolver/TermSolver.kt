package org.jetbrains.plugins.template.termsolver

import java.util.*

object TermSolver {
    private fun isLikelyUnary(prev: Any?) =
        prev == null || prev == '('

    private fun getOperator(input: String, pos: Int, prev: Any?): Operators {
        val unaryOperator = UnaryOperators.values().find { input.drop(pos).startsWith(it.form) }
        if (unaryOperator != null && (!unaryOperator.isOnlyUnary() || isLikelyUnary(prev))) {
            return unaryOperator
        }

        return BinaryOperators.operatorByChar[input[pos]] ?: throw Error("Ivalid expression")
    }

    private fun strToRpnTokens(input: String): List<Any> {
        val tokens = mutableListOf<Any>()
        val stack = ArrayDeque<Any>()
        var prevOperand = false
        var prev: Any? = null

        var i = 0
        while (i < input.length) {
            val currChar = input[i]
            if (currChar.isDigit()) {
                var j = i + 1
                while (j < input.length && (input[j].isDigit() || input[j] == '.')) {
                    ++j
                }

                @OptIn(kotlin.ExperimentalStdlibApi::class)
                val operand = input.slice(i..<j).toDouble()
                tokens.add(operand)
                prev = operand
                i = j
                continue
            }

            if (currChar == '(') {
                stack.addLast('(')
            }

            if (currChar == ')') {
                while (stack.last != '(') {
                    tokens.add(stack.removeLast())
                }

                stack.removeLast()
            }

            when (val operator = getOperator(input, i, prev)) {
                is UnaryOperators -> {
                    stack.addLast(operator)
                }

                is BinaryOperators -> {
                    while (!stack.isEmpty() && stack.last is BinaryOperators && (stack.last as BinaryOperators).priority >= operator.priority) {
                        tokens.add(stack.removeLast())
                    }

                    stack.addLast(operator)
                }
            }

            prev = currChar
            ++i
        }

        while (!stack.isEmpty()) {
            tokens.add(stack.removeLast())
        }

        return tokens
    }

    private fun evaluateRpn(tokens: List<Any>): Double {
        val stack = ArrayDeque<Any>()
        for (token in tokens) {
            when (token) {
                is Double -> stack.addLast(token)
                is UnaryOperators -> {
                    stack.addLast(
                        token.compute(stack.removeLast() as Double)
                    )
                }

                is BinaryOperators -> {
                    val right = stack.removeLast()
                    val left = stack.removeLast()
                    stack.addLast(
                        token.compute(left as Double, right as Double)
                    )
                }
            }
        }

        return stack.removeLast() as Double
    }

    fun evaluate(expression: String) = evaluateRpn(strToRpnTokens(expression))
}