package sdk.invoker;

/** How one language's SDK is called to exercise a given operation. */
public interface SdkInvocationStrategy {

    Invoker.Result invoke(OperationSpec spec, Object[] args);

    /**
     * Null when this SDK can be exercised on this machine; otherwise a
     * human-readable reason (missing toolchain, failed build, ...).
     * Tests use this to skip one SDK's cases gracefully instead of
     * letting a single missing toolchain sink the whole suite.
     */
    default String availabilityProblem() {
        return null;
    }
}
