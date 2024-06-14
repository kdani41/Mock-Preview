package com.kdani.mockpreview.gateway

import com.kdani.mockpreview.PreviewStubbed
import io.mockk.MockKGateway

/**
 * Builds a mock preview gateway using Mockk's gateway.
 */
interface MockPreviewGateway : MockKGateway {
    val previewStubbed: PreviewStubbed

    companion object {
        internal lateinit var implementation: () -> MockPreviewGateway

        fun <T> useImpl(block: (MockPreviewGateway) -> T): T {
            implementation = JvmMockPreviewGateway.defaultImplementationBuilder
            return block(implementation())
        }
    }
}