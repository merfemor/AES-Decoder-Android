package com.merfemor.aesencryptor.util

open class Either<L, R> protected constructor(val left: L?, val right: R?) {

    init {
        if (left != null && right != null) {
            throw IllegalArgumentException("either left or right must be null")
        }
        if (left == null && right == null) {
            throw IllegalArgumentException("either left or right must be not null")
        }
    }

    fun getLeftSure() = left!!

    fun getRightSure() = right!!

    fun isLeft() = left != null

    fun isRight() = right != null

    fun <NL, NR> map(leftMap: (L) -> NL, rightMap: (R) -> NR): Either<NL, NR> {
        return if (left != null) left(leftMap(left)) else right(rightMap(right!!))
    }

    fun doOnLeft(leftConsumer: (L) -> Unit): Either<L, R> {
        if (left != null) {
            leftConsumer(left)
        }
        return this
    }

    fun doOnRight(rightConsumer: (R) -> Unit): Either<L, R> {
        if (right != null) {
            rightConsumer(right)
        }
        return this
    }

    fun <T> mapToResult(leftMap: (L) -> T, rightMap: (R) -> T): T {
        return if (left != null) leftMap(left) else rightMap(right!!)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Either<*, *>

        if (left != other.left) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left?.hashCode() ?: 0
        result = 31 * result + (right?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = if (left != null) "Left($left)" else "Right($right)"


    companion object {
        @JvmStatic
        fun <L, R> left(l: L): Either<L, R> {
            return Either(l, null)
        }

        @JvmStatic
        fun <L, R> right(r: R): Either<L, R> {
            return Either(null, r)
        }
    }
}
