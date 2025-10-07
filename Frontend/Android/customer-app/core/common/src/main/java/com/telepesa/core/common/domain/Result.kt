/*
 * Copyright (c) 2025 Telepesa. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.telepesa.core.common.domain

/**
 * A generic class that holds a value or an error.
 * Used to represent the result of operations that can either succeed or fail.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or the
 * result of [onFailure] function for the encapsulated [Throwable] exception if it is
 * [Result.Error].
 */
inline fun <T> Result<T>.getOrElse(onFailure: (exception: Throwable) -> T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> onFailure(exception)
        is Result.Loading -> error("Result is still loading")
    }
}

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or `null`
 * if it is [Result.Error] or [Result.Loading].
 */
fun <T> Result<T>.getOrNull(): T? {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> null
        is Result.Loading -> null
    }
}

/**
 * Returns `true` if this instance represents [Result.Success] or `false` otherwise.
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * Returns `true` if this instance represents [Result.Error] or `false` otherwise.
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/**
 * Returns `true` if this instance represents [Result.Loading] or `false` otherwise.
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

/**
 * Maps the encapsulated value if this instance represents [Result.Success] or returns
 * the same [Result] if it is [Result.Error] or [Result.Loading].
 */
inline fun <T, R> Result<T>.map(transform: (value: T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(exception)
        is Result.Loading -> Result.Loading
    }
}

/**
 * Maps the encapsulated [Throwable] exception if this instance represents [Result.Error]
 * or returns the same [Result] if it is [Result.Success] or [Result.Loading].
 */
inline fun <T> Result<T>.mapError(transform: (exception: Throwable) -> Throwable): Result<T> {
    return when (this) {
        is Result.Success -> Result.Success(data)
        is Result.Error -> Result.Error(transform(exception))
        is Result.Loading -> Result.Loading
    }
}
