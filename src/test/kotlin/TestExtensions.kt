package com.example

import arrow.core.Either
import io.kotest.matchers.shouldBe
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun <A, B> Either<A, B>.shouldBeRight(): B {
    contract {
        returns() implies (this@shouldBeRight is Either.Right<B>)
    }
    return when (this) {
        is Either.Left -> throw AssertionError("Expected Right but was Left")
        is Either.Right -> this.value
    }
}

@OptIn(ExperimentalContracts::class)
infix fun <A, B> Either<A, B>.shouldBeRight(expected: B): B {
    contract {
        returns() implies (this@shouldBeRight is Either.Right<B>)
    }
    return this.shouldBeRight() shouldBe expected
}

@OptIn(ExperimentalContracts::class)
fun <A, B> Either<A, B>.shouldBeLeft(): A {
    contract {
        returns() implies (this@shouldBeLeft is Either.Left<A>)
    }
    return when (this) {
        is Either.Left -> this.value
        is Either.Right -> throw AssertionError("Expected Left but was Right")
    }
}

@OptIn(ExperimentalContracts::class)
infix fun <A, B> Either<A, B>.shouldBeLeft(expected: A): A {
    contract {
        returns() implies (this@shouldBeLeft is Either.Left<A>)
    }
    return this.shouldBeLeft() shouldBe expected
}
