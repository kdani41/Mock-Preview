package com.kdani.mockpreview.scope

import io.mockk.Answer
import io.mockk.Call
import io.mockk.CapturingSlot
import io.mockk.FunctionAnswer
import io.mockk.MockKAnswerScope
import io.mockk.MockKGateway

internal class PreviewAdditionalAnswerScope<T>(
    private val answerOpportunity: MockKGateway.AnswerOpportunity<T>,
    private val lambda: CapturingSlot<Function<*>>
) {
    private infix fun andThenAnswer(answer: Answer<T>): PreviewAdditionalAnswerScope<T> {
        answerOpportunity.provideAnswer(answer)
        return this
    }

    infix fun andThen(answer: MockKAnswerScope<T, Any>.(Call) -> T) =
        andThenAnswer(FunctionAnswer { MockKAnswerScope<T, Any>(lambda, it).answer(it) })
}