package org.jetbrains.plugins.template.termsolver

import kotlin.math.pow

interface Operators {
    val priority: Int;
    val char: Char
}

enum class BinaryOperators(
    override val priority: Int,
    val isRightAssociative: Boolean,
    val binaryOperator: (Double, Double) -> Double,
    override val char: Char
) : Operators {
    PLUS(1, false, { l, r -> l + r }, '+'),
    MINUS(1, false, { l, r -> l - r }, '-'),
    MULTIPLY(2, false, { l, r -> l * r }, '*'),
    DIVIDE(2, false, { l, r -> l / r }, '/'),
    POW(3, true, { a, b -> a.pow(b) }, '^');

    fun compute(left: Double, right: Double): Double {
        return binaryOperator(left, right)
    }

    companion object {
        val operatorByChar = BinaryOperators.values().associateBy { it.char }
    }
}

enum class UnaryOperators(
    override val priority: Int,
    val unaryOperator: (Double) -> Double,
    override val char: Char,
) : Operators {
    NEGATION(4, { n -> -n }, '-');

    fun compute(n: Double) = unaryOperator(n)
}