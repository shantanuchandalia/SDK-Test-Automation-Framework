package sdk.invoker.python;

import sdk.invoker.CliSupport;
import sdk.invoker.Invoker;
import sdk.invoker.OperationSpec;
import sdk.invoker.SdkInvocationStrategy;

/** CLI dispatch for the Python SDK: python &lt;script&gt; &lt;args...&gt; */
public final class PythonInvocationStrategy implements SdkInvocationStrategy {

    @Override
    public Invoker.Result invoke(OperationSpec spec, Object[] args) {
        String[] command = new String[args.length + 2];
        command[0] = CliSupport.PYTHON_CMD;
        command[1] = spec.pythonScript();
        for (int i = 0; i < args.length; i++) {
            command[i + 2] = String.valueOf(args[i]);
        }
        return CliSupport.runCli(CliSupport.ROOT.resolve("python").toFile(), command);
    }
}
