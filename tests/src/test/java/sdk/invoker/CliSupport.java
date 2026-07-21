package sdk.invoker;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Shared CLI-invocation plumbing for the out-of-process SDK strategies.
 * Each language's strategy lives in its own sdk.invoker.&lt;language&gt;
 * package, so this is the public surface they call into to shell out to
 * their SDK's CLI.
 */
public final class CliSupport {

    /** Repo root = parent of the tests/ folder Maven runs from. */
    public static final Path ROOT = Paths.get(System.getProperty("user.dir")).getParent();

    private static final boolean WINDOWS =
            System.getProperty("os.name").toLowerCase().contains("win");

    public static final String PYTHON_CMD = WINDOWS ? "python" : "python3";

    /** Directory the .NET build output lands in, relative to dotnet/. */
    public static final Path DOTNET_BIN_DIR = Paths.get("bin", "Debug", "net8.0");

    private CliSupport() { }

    public static Invoker.Result runCli(File workingDir, String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command)
                    .directory(workingDir)
                    .redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new Invoker.Result(2, null, "TIMEOUT: " + String.join(" ", command));
            }
            String raw = output.toString().trim();
            return new Invoker.Result(process.exitValue(), parseResult(raw), raw);
        } catch (Exception e) {
            return new Invoker.Result(2, null, "INVOKER ERROR: " + e.getMessage());
        }
    }

    /** The SDKs print "<operation>(<args>) -> <result>" on success. */
    private static String parseResult(String stdout) {
        int arrow = stdout.lastIndexOf("-> ");
        return (arrow == -1) ? null : stdout.substring(arrow + 3).trim();
    }
}
