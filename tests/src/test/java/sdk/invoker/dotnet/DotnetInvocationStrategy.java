package sdk.invoker.dotnet;

import sdk.invoker.CliSupport;
import sdk.invoker.Invoker;
import sdk.invoker.OperationSpec;
import sdk.invoker.SdkInvocationStrategy;

/** CLI dispatch for the .NET SDK: dotnet bin/Debug/net8.0/&lt;dll&gt; &lt;args...&gt; */
public final class DotnetInvocationStrategy implements SdkInvocationStrategy {

    @Override
    public Invoker.Result invoke(OperationSpec spec, Object[] args) {
        String dllPath = CliSupport.DOTNET_BIN_DIR.resolve(spec.dotnetDll()).toString();
        String[] command = new String[args.length + 2];
        command[0] = "dotnet";
        command[1] = dllPath;
        for (int i = 0; i < args.length; i++) {
            command[i + 2] = String.valueOf(args[i]);
        }
        return CliSupport.runCli(CliSupport.ROOT.resolve("dotnet").toFile(), command);
    }
}
