package org.jetbrains.plugins.template.termsolver

import java.util.*

object TermSolver {
    private fun strToRpnTokens(input: String): List<Any> {
        val tokens = mutableListOf<Any>()
        val stack = ArrayDeque<Any>()

        var i = 0
        while (i < input.length) {
            val currChar = input[i]
            if (currChar.isDigit()) {
                var j = i + 1
                while (j < input.length && (input[j].isDigit() || input[j] == '.')) {
                    ++j
                }

                @OptIn(kotlin.ExperimentalStdlibApi::class)
                tokens.add(input.slice(i..<j).toDouble())
                i = j
                continue
            }

            if (currChar == '(') stack.addLast('(')
            if (currChar == ')') {
                while (stack.last != '(') {
                    tokens.add(stack.removeLast())
                }

                stack.removeLast()
            }

            if (currChar == '-') {

            }
//            val unaryOperator = UnaryOperators.operatorByChar[currChar]

            val operator = BinaryOperators.operatorByChar[currChar]
            if (operator != null) {
                while (!stack.isEmpty() && stack.last is BinaryOperators && (stack.last as BinaryOperators).priority >= operator.priority) {
                    tokens.add(stack.removeLast())
                }

                stack.addLast(operator)
            }

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