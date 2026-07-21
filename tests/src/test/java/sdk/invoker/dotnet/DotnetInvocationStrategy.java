package sdk.invoker.dotnet;

import java.nio.file.Files;
import java.nio.file.Path;
import sdk.invoker.CliSupport;
import sdk.invoker.Invoker;
import sdk.invoker.OperationRegistry;
import sdk.invoker.OperationSpec;
import sdk.invoker.SdkInvocationStrategy;

/** CLI dispatch for the .NET SDK: dotnet bin/Debug/net8.0/&lt;dll&gt; &lt;args...&gt; */
public final class DotnetInvocationStrategy implements SdkInvocationStrategy {

    private boolean probed;
    private String problem;

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

    /**
     * Probes (once) whether the .NET leg can run: builds the dotnet/ project
     * if any registered operation's DLL is missing. A failed probe makes the
     * .NET cases skip with this reason instead of sinking the whole suite.
     */
    @Override
    public synchronized String availabilityProblem() {
        if (!probed) {
            probed = true;
            problem = probe();
        }
        return problem;
    }

    private static String probe() {
        Path dotnetDir = CliSupport.ROOT.resolve("dotnet");
        if (!Files.isDirectory(dotnetDir)) {
            return "dotnet/ project folder not found under " + CliSupport.ROOT;
        }
        if (allDllsPresent(dotnetDir)) {
            return null;
        }
        Invoker.Result build = CliSupport.runCli(dotnetDir.toFile(),
                "dotnet", "build", "--verbosity", "quiet");
        if (build.exitCode != 0) {
            return ".NET build failed (is the .NET 8 SDK installed?): " + build.raw;
        }
        if (!allDllsPresent(dotnetDir)) {
            return "build succeeded but expected DLL(s) still missing under " + CliSupport.DOTNET_BIN_DIR;
        }
        return null;
    }

    private static boolean allDllsPresent(Path dotnetDir) {
        for (OperationSpec spec : OperationRegistry.all()) {
            if (!Files.exists(dotnetDir.resolve(CliSupport.DOTNET_BIN_DIR).resolve(spec.dotnetDll()))) {
                return false;
            }
        }
        return true;
    }
}
