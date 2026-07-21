package sdk.invoker;

import java.util.EnumMap;
import java.util.Map;
import sdk.invoker.dotnet.DotnetInvocationStrategy;
import sdk.invoker.java.JavaInvocationStrategy;
import sdk.invoker.python.PythonInvocationStrategy;

/**
 * Single gateway between the test classes and the three SDK implementations.
 *
 * Dispatch is registry-driven: {@link #invoke} looks up the requested
 * operation's per-language binding from {@link OperationRegistry} and hands
 * it to the {@link SdkInvocationStrategy} for the requested {@link Sdk}.
 * Each language's strategy lives in its own sdk.invoker.&lt;language&gt;
 * subpackage ({@link JavaInvocationStrategy} calls in-JVM via reflection;
 * {@link PythonInvocationStrategy}/{@link DotnetInvocationStrategy} shell
 * out to the CLI via {@link CliSupport#runCli}). Adding a new SDK operation
 * means registering one more {@link OperationSpec} - not adding new methods
 * here.
 *
 * CLI responses are normalized into a {@link Result}: the exit code
 * (0 = found, 1 = not found, 2 = bad input/error) and the value parsed
 * from stdout (the token after "-> "), or null when not found.
 */
public final class Invoker {

    private static final Map<Sdk, SdkInvocationStrategy> STRATEGIES = new EnumMap<>(Sdk.class);
    static {
        STRATEGIES.put(Sdk.JAVA, new JavaInvocationStrategy());
        STRATEGIES.put(Sdk.PYTHON, new PythonInvocationStrategy());
        STRATEGIES.put(Sdk.DOTNET, new DotnetInvocationStrategy());
    }

    private Invoker() { }

    /** Normalized SDK response. */
    public static final class Result {
        public final int exitCode;
        public final String value;   // parsed operation result, null when not found
        public final String raw;     // full stdout, for report logging

        public Result(int exitCode, String value, String raw) {
            this.exitCode = exitCode;
            this.value = value;
            this.raw = raw;
        }
    }

    /** Invoke {@code operationName} against {@code sdk} with the given args. */
    public static Result invoke(Sdk sdk, String operationName, Object... args) {
        OperationSpec spec = OperationRegistry.get(operationName);
        return STRATEGIES.get(sdk).invoke(spec, args);
    }

    /**
     * Null when {@code sdk} can be exercised on this machine, otherwise the
     * human-readable reason it can't (e.g. .NET toolchain missing). Lets the
     * test layer skip that SDK's cases instead of failing the whole suite.
     */
    public static String availabilityProblem(Sdk sdk) {
        return STRATEGIES.get(sdk).availabilityProblem();
    }
}
