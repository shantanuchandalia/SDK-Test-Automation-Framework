package sdk.invoker;

/**
 * One operation's per-language binding: what to call in each SDK to
 * exercise it. This is the unit a new operation is added through -
 * register one of these in {@link OperationRegistry} and every
 * {@link SdkInvocationStrategy} knows how to invoke it.
 */
public final class OperationSpec {

    private final String name;
    private final String javaClass;
    private final String javaMethod;
    private final Class<?>[] javaParamTypes;
    private final String pythonScript;
    private final String dotnetDll;

    public OperationSpec(String name,
                          String javaClass, String javaMethod, Class<?>[] javaParamTypes,
                          String pythonScript, String dotnetDll) {
        this.name = name;
        this.javaClass = javaClass;
        this.javaMethod = javaMethod;
        this.javaParamTypes = javaParamTypes;
        this.pythonScript = pythonScript;
        this.dotnetDll = dotnetDll;
    }

    public String name() {
        return name;
    }

    public String javaClass() {
        return javaClass;
    }

    public String javaMethod() {
        return javaMethod;
    }

    public Class<?>[] javaParamTypes() {
        return javaParamTypes;
    }

    public String pythonScript() {
        return pythonScript;
    }

    public String dotnetDll() {
        return dotnetDll;
    }
}
