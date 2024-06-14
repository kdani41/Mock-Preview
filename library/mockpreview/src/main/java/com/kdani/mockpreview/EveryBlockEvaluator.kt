package com.kdani.mockpreview

import com.kdani.mockpreview.scope.PreviewScope
import com.kdani.mockpreview.scope.PreviewScopeImpl
import io.mockk.CapturingSlot
import io.mockk.MockKGateway
import io.mockk.MockKMatcherScope
import io.mockk.impl.eval.RecordedBlockEvaluator
import io.mockk.impl.recording.AutoHinter

internal class EveryBlockEvaluator(
    callRecorder: () -> MockKGateway.CallRecorder,
    autoHinterFactory: () -> AutoHinter
) : RecordedBlockEvaluator(callRecorder, autoHinterFactory), PreviewStubbed {

    @Suppress("UNCHECKED_CAST")
    override fun <T> everyPreview(
        mockBlock: (MockKMatcherScope.() -> T)?
    ): PreviewScope<T> {

        callRecorder().startStubbing()

        val lambda = CapturingSlot<Function<*>>()
        val scope = MockKMatcherScope(callRecorder(), lambda)

        record(scope, mockBlock, coMockBlock = null)

        val opportunity = callRecorder().answerOpportunity() as MockKGateway.AnswerOpportunity<T>

        return PreviewScopeImpl(opportunity, lambda)
    }
}