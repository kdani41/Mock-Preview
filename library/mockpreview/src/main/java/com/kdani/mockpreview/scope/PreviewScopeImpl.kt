package com.kdani.mockpreview.scope

import io.mockk.Answer
import io.mockk.CapturingSlot
import io.mockk.ConstantAnswer
import io.mockk.MockKGateway
import io.mockk.ThrowingAnswer

internal class PreviewScopeImpl<T>(
    private val answerOpportunity: MockKGateway.AnswerOpportunity<T>,
    private val lambda: CapturingSlot<Function<*>>
) : PreviewScope<T> {
    private infix fun answers(answer: Answer<T>): PreviewAdditionalAnswerScope<T> {
        answerOpportunity.provideAnswer(answer)
        return PreviewAdditionalAnswerScope(answerOpportunity, lambda)
    }

    override infix fun returns(returnValue: T) = answers(ConstantAnswer(returnValue))

    override infix fun throws(ex: Throwable) = answers(ThrowingAnswer(ex))
}