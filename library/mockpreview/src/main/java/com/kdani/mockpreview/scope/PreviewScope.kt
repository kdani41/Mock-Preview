package com.kdani.mockpreview.scope

/**
 * Scope for preview compose.
 */
interface PreviewScope<T> {
    infix fun returns(returnValue: T): Any

    infix fun throws(ex: Throwable): Any
}
