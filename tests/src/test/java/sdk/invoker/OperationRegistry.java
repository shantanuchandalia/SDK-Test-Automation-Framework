package sdk.invoker;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of every SDK operation under test. Adding operation #2 means
 * adding one {@link #register} call here (plus a data-driven test method
 * in {@link SdkOperationTest}) - not new invocation classes per language.
 */
public final class OperationRegistry {

    private static final Map<String, OperationSpec> OPERATIONS = new HashMap<>();

    static {
        register(new OperationSpec(
                "getUserById",
                "GetUserById", "getUserById", new Class<?>[] { int.class },
                "get_user_by_id.py",
                "GetUserById.dll"));
    }

    private OperationRegistry() { }

    public static void register(OperationSpec spec) {
        OPERATIONS.put(spec.name(), spec);
    }

    public static OperationSpec get(String operationName) {
        OperationSpec spec = OPERATIONS.get(operationName);
        if (spec == null) {
            throw new IllegalArgumentException("Unregistered SDK operation: " + operationName);
        }
        return spec;
    }

    /** Every registered operation, e.g. for toolchain probes that must cover all of them. */
    public static Collection<OperationSpec> all() {
        return Collections.unmodifiableCollection(OPERATIONS.values());
    }
}
