package com.kdani.mockpreview

import com.kdani.mockpreview.scope.PreviewScope
import io.mockk.MockKMatcherScope

interface PreviewStubbed {

    fun <T> everyPreview(
        mockBlock: (MockKMatcherScope.() -> T)?
    ): PreviewScope<T>
}