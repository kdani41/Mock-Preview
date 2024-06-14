package com.kdani.mockpreview

import com.kdani.mockpreview.gateway.MockPreviewGateway
import com.kdani.mockpreview.helpers.internalMockkObject
import io.mockk.MockKMatcherScope
import kotlin.reflect.KClass

/**
 * Creates a mock preview object for allowing mocks in previews.
 *
 * @param name custom name for the mock
 * @param moreInterfaces additional interfaces required for mock.
 * @param block to provide custom behaviour for mocks
 * @param relaxed provides default value for mocks. true by default
 * @param relaxedUnitFun provides default value for functions mocks. true by default
 */
inline fun <reified T : Any> mockPreview(
    name: String? = null,
    vararg moreInterfaces: KClass<*>,
    relaxed: Boolean = true,
    relaxedUnitFun: Boolean = true,
    crossinline block: T.() -> Unit = {}
): T = MockPreviewGateway.useImpl {
    val mock = it.mockFactory.mockk(
        T::class,
        name,
        relaxed,
        moreInterfaces,
        relaxedUnitFun
    )
    block(mock)
    mock
}

/**
 * Allows mocking object class for previews.
 */
fun mockPreviewObject(vararg objects: Any) =
    MockPreviewGateway.useImpl {
        it.objectMockFactory.internalMockkObject(objects, recordPrivateCalls = false)
    }

/**
 * Starts a block of stubbing. Part of DSL.
 *
 * Used to define what behaviour is going to be mocked.
 *
 * @sample
 * interface Sample {
 *   val input: String
 * }
 *
 * val sampleObj = mockPreview<Sample>()
 * forThis { sampleObj.input } returns "blah"
 */
fun <T> forThis(stubBlock: MockKMatcherScope.() -> T) = MockPreviewGateway.useImpl {
    it.previewStubbed.everyPreview(stubBlock)
}