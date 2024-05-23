package org.jetbrains.plugins.template.termsolver

import kotlin.math.pow

interface Token
data class Operand(val value: Double) : Token
enum class Brackets : Token {
    Left,
    Right
}

interface Operator : Token {
    val priority: Int
}

enum class BinaryOperator(
    override val priority: Int,
    val binaryOperator: (Double, Double) -> Double,
    val form: Char
) : Operator {
    PLUS(1, { l, r -> l + r }, '+'),
    MINUS(1, { l, r -> l - r }, '-'),
    MULTIPLY(2, { l, r -> l * r }, '*'),
    DIVIDE(2, { l, r -> l / r }, '/'),
    POW(3, { a, b -> a.pow(b) }, '^');

    fun compute(left: Double, right: Double): Double = binaryOperator(left, right)

    companion object {
        val operatorByChar = BinaryOperator.values().associateBy { it.form }
    }
}

enum class UnaryOperator(
    override val priority: Int,
    val unaryOperator: (Double) -> Double,
    val withBrackets: Boolean,
    val form: String,
) : Operator {
    NEGATION(4, { n -> -n }, false, "-"),
    SQRT(4, { n -> kotlin.math.sqrt(n) }, true, "sqrt");

    fun compute(n: Double) = unaryOperator(n)
}