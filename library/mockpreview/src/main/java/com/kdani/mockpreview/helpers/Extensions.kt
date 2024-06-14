package com.kdani.mockpreview.helpers

import com.kdani.mockpreview.gateway.MockPreviewGateway
import io.mockk.MockKCancellationRegistry
import io.mockk.MockKGateway

/**
 * Sets up mocking for object class for previews.
 */
internal fun MockKGateway.ObjectMockFactory.internalMockkObject(
    objects: Array<out Any>,
    recordPrivateCalls: Boolean = false
) {
    objects.forEach {
        val cancellation = this.objectMockk(it, recordPrivateCalls)
        MockPreviewGateway.implementation().clearer.clear(
            arrayOf(it),
            MockKGateway.ClearOptions(
                answers = true,
                recordedCalls = true,
                childMocks = true,
                verificationMarks = true,
                exclusionRules = true
            )
        )
        MockKCancellationRegistry
            .subRegistry(MockKCancellationRegistry.Type.OBJECT)
            .cancelPut(it, cancellation)
    }
}