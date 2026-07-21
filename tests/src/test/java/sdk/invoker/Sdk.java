package sdk.invoker;

/**
 * The SDK languages under test. Carries no invocation logic itself -
 * it is only a dispatch key (see {@link Invoker#invoke}), a TestNG
 * data-provider parameter, and the source of the Extent report's
 * per-SDK grouping (see {@link BaseTest#recordResult}).
 */
public enum Sdk {

    JAVA("Java SDK"),
    PYTHON("Python SDK"),
    DOTNET(".NET SDK");

    private final String label;

    Sdk(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
