package sdk.invoker.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import sdk.invoker.Invoker;
import sdk.invoker.OperationSpec;
import sdk.invoker.SdkInvocationStrategy;

/**
 * In-JVM reflection dispatch for the Java SDK. Reflection is used only
 * because the SDK classes live in the default package; the call itself
 * is a plain static method call once resolved.
 */
public final class JavaInvocationStrategy implements SdkInvocationStrategy {

    @Override
    public Invoker.Result invoke(OperationSpec spec, Object[] args) {
        try {
            Class<?> sdk = Class.forName(spec.javaClass());
            Method method = sdk.getMethod(spec.javaMethod(), spec.javaParamTypes());
            String result = (String) method.invoke(null, args);
            int exit = (result == null) ? 1 : 0;
            String raw = spec.name() + "(" + argsToString(args) + ") -> " + result;
            return new Invoker.Result(exit, result, raw);
        } catch (InvocationTargetException e) {
            return new Invoker.Result(2, null, "ERROR: " + e.getCause().getMessage());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Java SDK class not on classpath: " + spec.javaClass(), e);
        }
    }

    private static String argsToString(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
