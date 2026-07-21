package sdk.invoker;

/** How one language's SDK is called to exercise a given operation. */
public interface SdkInvocationStrategy {

    Invoker.Result invoke(OperationSpec spec, Object[] args);
}
