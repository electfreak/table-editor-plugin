package org.jetbrains.plugins.template.termsolver

import kotlin.math.pow

interface Operators {
    val priority: Int;
}

enum class BinaryOperators(
    override val priority: Int,
    val isRightAssociative: Boolean,
    val binaryOperator: (Double, Double) -> Double,
    val char: Char
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
    val brackets: Boolean,
    val form: String,
) : Operators {
    NEGATION(4, { n -> -n }, false, "-"),
    SQRT(4, { n -> kotlin.math.sqrt(n) }, true, "sqrt");

    fun compute(n: Double) = unaryOperator(n)
    fun isOnlyUnary() = BinaryOperators.values().map { it.char.toString() }.contains(form)
}

val mixedOperators =
    UnaryOperators.values().map { it.form }.intersect(
        BinaryOperators.values().map { it.char.toString() }.toSet()
    )