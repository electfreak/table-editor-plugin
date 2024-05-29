package org.jetbrains.plugins.template.termsolver

object TermParser {
    private fun isLikelyUnary(previous: Token?) =
        previous == null || previous == Brackets.Left

    private fun getOperator(input: String, pos: Int, prev: Token?): Operator {
        val unaryOperator = UnaryOperator.values().find { input.drop(pos).startsWith(it.form) }

        if (unaryOperator != null && (unaryOperator.withBrackets || isLikelyUnary(prev))) {
            return unaryOperator
        }

        return BinaryOperator.operatorByChar[input[pos]] ?: error("Invalid expression")
    }

    private fun getCellReference(input: String, pos: Int): Cell {
        val letters = input.drop(pos).takeWhile { it.isLetter() }
        val digits = input.drop(pos + letters.length).takeWhile { it.isDigit() }.toInt()
        return Cell(letters, digits)
    }

    fun inputToTokens(input: String): List<Token> {
        val expression = input.filterNot { it.isWhitespace() }
        val tokens = mutableListOf<Token>()
        var pos = 0
        var prev: Token? = null

        while (pos < expression.length) {
            val currChar = expression[pos]
            when {
                currChar.isDigit() -> {
                    val parsedNumber = expression.drop(pos).takeWhile { it.isDigit() || it == '.' }
                    val operand = Literal(parsedNumber.toDouble())
                    tokens.add(operand)
                    prev = operand
                    pos += parsedNumber.length
                    continue
                }

                currChar == '(' -> {
                    tokens.add(Brackets.Left)
                    prev = Brackets.Left
                }

                currChar == ')' -> {
                    tokens.add(Brackets.Right)
                    prev = Brackets.Right
                }

                currChar.isLetter() && currChar.isUpperCase() -> {
                    val cellReference = getCellReference(expression, pos)
                    tokens.add(cellReference)
                    pos += cellReference.length
                    prev = cellReference
                    continue
                }

                else -> {
                    val operator = getOperator(expression, pos, prev)
                    if (operator is UnaryOperator) {
                        tokens.add(operator)
                        pos += operator.form.length
                        prev = operator
                        continue
                    }

                    if (operator is BinaryOperator) {
                        tokens.add(operator)
                        prev = operator
                    }
                }
            }

            ++pos
        }

        return tokens
    }
}