package sdk.invoker;

/**
 * The SDK languages under test. Carries no invocation logic itself -
 * it is only a dispatch key (see {@link Invoker#invoke}), a TestNG
 * data-provider parameter, and the source of the Extent report's
 * per-SDK grouping.
 *
 * {@code cli} marks SDKs exercised through their command-line entry
 * point (argument parsing included); the Java SDK is called in-JVM at
 * the library layer, so CLI-only scenarios (e.g. non-numeric input)
 * do not apply to it.
 */
public enum Sdk {

    JAVA("Java SDK", false),
    PYTHON("Python SDK", true),
    DOTNET(".NET SDK", true);

    private final String label;
    private final boolean cli;

    Sdk(String label, boolean cli) {
        this.label = label;
        this.cli = cli;
    }

    public String label() {
        return label;
    }

    /** True when this SDK is invoked through its CLI entry point. */
    public boolean cli() {
        return cli;
    }
}
