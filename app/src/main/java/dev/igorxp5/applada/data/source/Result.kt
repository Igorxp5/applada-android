package dev.igorxp5.applada.data.source

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error<out T>(val exception: Exception, val fallbackResult: Result<T>? = null) : Result<T>()
}
