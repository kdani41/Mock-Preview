package com.kdani.mockpreview

import kotlin.reflect.KClass

/**
 * Returns a no-op operation for mocks.
 * mocks are not supported for release.
 */
inline fun <reified T : Any> mockPreview(
    name: String? = null,
    vararg moreInterfaces: KClass<*>,
    relaxed: Boolean = true,
    relaxedUnitFun: Boolean = true,
    crossinline block: T.() -> Unit = {}
): T = Any() as T


/**
 * Returns a no-op operation for mocks.
 * mocks are not supported for release.
 */
fun <T> forThis(stubBlock: () -> T): PreviewScope<T> {
    throw NotImplementedError()
}