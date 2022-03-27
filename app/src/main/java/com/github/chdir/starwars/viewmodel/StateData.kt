package com.github.chdir.starwars.viewmodel;

sealed class StateData<T> {
    protected val result: T?
    protected val error: Throwable?

    constructor(error: Throwable) {
        this.error = error
        result = null
    }

    constructor(arg: T) {
        this.error = null
        result = arg
    }

    public class Loading<P>() : StateData<P>(null as P) {
    }

    public class Failure<F>(fail: Throwable) : StateData<F>(fail) {
        public fun getThrowable(): Throwable {
            return error!!
        }
    }

    public class Success<S>(value: S) : StateData<S>(value) {
        public fun getValue(): S {
            return result!!
        }
    }
}
