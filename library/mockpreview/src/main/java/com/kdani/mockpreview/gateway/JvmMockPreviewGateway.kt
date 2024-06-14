package com.kdani.mockpreview.gateway

import com.kdani.mockpreview.EveryBlockEvaluator
import com.kdani.mockpreview.PreviewStubbed
import io.mockk.MockKGateway
import io.mockk.Ordering
import io.mockk.impl.InternalPlatform
import io.mockk.impl.annotations.JvmMockInitializer
import io.mockk.impl.eval.ExcludeBlockEvaluator
import io.mockk.impl.eval.VerifyBlockEvaluator
import io.mockk.impl.instantiation.AbstractMockFactory
import io.mockk.impl.instantiation.AnyValueGenerator
import io.mockk.impl.instantiation.CommonInstanceFactoryRegistry
import io.mockk.impl.instantiation.JvmAnyValueGenerator
import io.mockk.impl.instantiation.JvmConstructorMockFactory
import io.mockk.impl.instantiation.JvmInstantiator
import io.mockk.impl.instantiation.JvmMockFactory
import io.mockk.impl.instantiation.JvmMockTypeChecker
import io.mockk.impl.instantiation.JvmObjectMockFactory
import io.mockk.impl.instantiation.JvmStaticMockFactory
import io.mockk.impl.log.JvmLogging.adaptor
import io.mockk.impl.log.Logger
import io.mockk.impl.log.SafeToString
import io.mockk.impl.recording.CallRecorderFactories
import io.mockk.impl.recording.CallRoundBuilder
import io.mockk.impl.recording.ChainedCallDetector
import io.mockk.impl.recording.ChildHinter
import io.mockk.impl.recording.CommonCallRecorder
import io.mockk.impl.recording.CommonVerificationAcknowledger
import io.mockk.impl.recording.JvmAutoHinter
import io.mockk.impl.recording.JvmSignatureValueGenerator
import io.mockk.impl.recording.PermanentMocker
import io.mockk.impl.recording.SignatureMatcherDetector
import io.mockk.impl.recording.VerificationCallSorter
import io.mockk.impl.recording.states.AnsweringState
import io.mockk.impl.recording.states.ExclusionState
import io.mockk.impl.recording.states.SafeLoggingState
import io.mockk.impl.recording.states.StubbingAwaitingAnswerState
import io.mockk.impl.recording.states.StubbingState
import io.mockk.impl.recording.states.VerifyingState
import io.mockk.impl.stub.CommonClearer
import io.mockk.impl.stub.StubGatewayAccess
import io.mockk.impl.stub.StubRepository
import io.mockk.impl.verify.AllCallsCallVerifier
import io.mockk.impl.verify.OrderedCallVerifier
import io.mockk.impl.verify.SequenceCallVerifier
import io.mockk.impl.verify.TimeoutVerifier
import io.mockk.impl.verify.UnorderedCallVerifier
import io.mockk.proxy.MockKAgentFactory
import io.mockk.proxy.MockKAgentLogFactory
import java.security.SecureRandom

/**
 * Internal Gateway for previewing mocks via reflection.
 */
internal class JvmMockPreviewGateway : MockPreviewGateway {

    private val instanceFactoryRegistryIntrnl = CommonInstanceFactoryRegistry()
    private val safeToString: SafeToString = SafeToString { callRecorderTL.get() as CommonCallRecorder }
    private val agentFactory: MockKAgentFactory =
        if (InternalPlatform.isRunningAndroidInstrumentationTest())
            InternalPlatform.loadPlugin(
                "io.mockk.proxy.android.AndroidMockKAgentFactory",
                "Android instrumented test is running, " +
                        "include 'io.mockk:mockk-android' dependency " +
                        "instead 'io.mockk:mockk'"
            )
        else
            InternalPlatform.loadPlugin(
                "io.mockk.proxy.jvm.JvmMockKAgentFactory",
                "Check if you included 'io.mockk:mockk-android' dependency " +
                        "instead of 'io.mockk:mockk'"
            )


    init {
        agentFactory.init(object : MockKAgentLogFactory {
            override fun logger(cls: Class<*>) = Logger.loggerFactory(cls.kotlin).adaptor()
        })
    }

    private val stubRepo = StubRepository(safeToString)
    private val instantiator = JvmInstantiator(
        agentFactory.instantiator,
        instanceFactoryRegistryIntrnl
    )
    private val anyValueGeneratorProvider: () -> AnyValueGenerator = {
        anyValueGenerator ?: anyValueGeneratorFactory.invoke(instantiator.instantiate(Void::class))
    }

    override fun verifier(params: MockKGateway.VerificationParameters): MockKGateway.CallVerifier {

        val verifier = when (params.ordering) {
            Ordering.UNORDERED -> UnorderedCallVerifier(stubRepo, safeToString)
            Ordering.ALL -> AllCallsCallVerifier(stubRepo, safeToString)
            Ordering.ORDERED -> OrderedCallVerifier(stubRepo, safeToString)
            Ordering.SEQUENCE -> SequenceCallVerifier(stubRepo, safeToString)
        }

        return if (params.timeout > 0) {
            TimeoutVerifier(stubRepo, verifier)
        } else {
            verifier
        }
    }

    private val signatureValueGenerator = JvmSignatureValueGenerator(SecureRandom())

    private val gatewayAccess =
        StubGatewayAccess({ callRecorder }, anyValueGeneratorProvider, stubRepo, safeToString)

    override val mockFactory: AbstractMockFactory = JvmMockFactory(
        agentFactory.proxyMaker,
        instantiator,
        stubRepo,
        gatewayAccess
    )

    override val verificationAcknowledger = CommonVerificationAcknowledger(stubRepo, safeToString)

    private val callRecorderFactories = CallRecorderFactories(
        { SignatureMatcherDetector(safeToString) { ChainedCallDetector(safeToString) } },
        { CallRoundBuilder(safeToString) },
        ::ChildHinter,
        this::verifier,
        { PermanentMocker(stubRepo, safeToString) },
        ::VerificationCallSorter,
        ::AnsweringState,
        ::StubbingState,
        ::VerifyingState,
        ::ExclusionState,
        ::StubbingAwaitingAnswerState,
        ::SafeLoggingState
    )

    override val callRecorder: MockKGateway.CallRecorder
        get() = callRecorderTL.get() as MockKGateway.CallRecorder

    override val verifier: MockKGateway.Verifier
        get() = VerifyBlockEvaluator(callRecorderTL::get as () -> MockKGateway.CallRecorder, stubRepo, ::JvmAutoHinter)

    private val gatewayAccessWithFactory = gatewayAccess.copy(mockFactory = mockFactory)

    override val clearer = CommonClearer(stubRepo, safeToString)

    override val constructorMockFactory = JvmConstructorMockFactory(
        agentFactory.constructorProxyMaker,
        clearer,
        mockFactory,
        agentFactory.proxyMaker,
        gatewayAccessWithFactory
    )
    override val instanceFactoryRegistry: MockKGateway.InstanceFactoryRegistry =
        instanceFactoryRegistryIntrnl

    private val callRecorderTL = object : ThreadLocal<CommonCallRecorder>() {
        override fun initialValue(): CommonCallRecorder = CommonCallRecorder(
            stubRepo,
            instantiator,
            signatureValueGenerator,
            mockFactory,
            anyValueGeneratorProvider,
            safeToString,
            callRecorderFactories,
            { recorder -> callRecorderFactories.answeringState(recorder) },
            verificationAcknowledger
        )
    }
    override val excluder: MockKGateway.Excluder =
        ExcludeBlockEvaluator(callRecorderTL::get as () -> MockKGateway.CallRecorder, stubRepo, ::JvmAutoHinter)

    override val previewStubbed: PreviewStubbed = EveryBlockEvaluator(
        callRecorderTL::get as () -> MockKGateway.CallRecorder,
        ::JvmAutoHinter
    )

    override val mockInitializer = JvmMockInitializer(this)
    override val mockTypeChecker = JvmMockTypeChecker(
        stubRepo
    ) {
        constructorMockFactory.isMock(it)
    }
    override val objectMockFactory = JvmObjectMockFactory(
        agentFactory.proxyMaker,
        stubRepo,
        gatewayAccessWithFactory
    )
    override val staticMockFactory = JvmStaticMockFactory(
        agentFactory.staticProxyMaker,
        stubRepo,
        gatewayAccessWithFactory
    )
    override val stubber: MockKGateway.Stubber
        get() = throw NotImplementedError()

    companion object {
        private var anyValueGenerator: AnyValueGenerator? = null
        var anyValueGeneratorFactory: (voidInstance: Any) -> JvmAnyValueGenerator =
            { voidInstance -> JvmAnyValueGenerator(voidInstance) }
            set(value) {
                anyValueGenerator = null
                field = value
            }

        private val defaultImplementation = JvmMockPreviewGateway()
        val defaultImplementationBuilder: () -> MockPreviewGateway = { defaultImplementation }
    }
}