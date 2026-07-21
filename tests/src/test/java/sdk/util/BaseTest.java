package sdk.util;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import sdk.invoker.CliSupport;
import sdk.invoker.Invoker;
import sdk.invoker.Sdk;

/**
 * Shared plumbing for the SDK test classes:
 * - ExtentReports lifecycle (report written to tests/target/ExtentReport.html)
 * - {@link #crossWithSdk}, the helper that turns one operation's raw test
 *   data into {Sdk, ...row} cases across all three SDKs, so growing the
 *   suite is "add data rows / add operations", not "add test classes"
 * - {@link #requireSdk}, which skips (not fails) an SDK's cases when its
 *   toolchain is unavailable on the machine, so e.g. a missing .NET SDK
 *   never blocks the Java and Python legs
 */
public abstract class BaseTest {

    private static ExtentReports extent;

    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() throws Exception {
        if (extent == null) {
            Path reportDir = CliSupport.ROOT.resolve("tests").resolve("target");
            Files.createDirectories(reportDir);
            ExtentSparkReporter spark =
                    new ExtentSparkReporter(reportDir.resolve("ExtentReport.html").toString());
            spark.config().setDocumentTitle("SDK Automation Framework");
            spark.config().setReportName("SDK Automation Suite (Java / Python / .NET)");
            extent = new ExtentReports();
            extent.attachReporter(spark);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void recordResult(ITestResult result) {
        String label = getClass().getSimpleName();
        for (Object param : result.getParameters()) {
            if (param instanceof Sdk) {
                label = ((Sdk) param).label();
                break;
            }
        }
        String name = label + " | " + result.getMethod().getMethodName()
                + " " + Arrays.toString(result.getParameters());
        ExtentTest test = extent.createTest(name)
                .assignCategory(label);
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                test.pass("Passed");
                break;
            case ITestResult.FAILURE:
                test.fail(result.getThrowable());
                break;
            default:
                test.skip(result.getThrowable() != null
                        ? result.getThrowable().getMessage() : "Skipped");
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        if (extent != null) {
            extent.flush();
        }
    }

    /**
     * Skips the current test (rather than failing it) when the given SDK
     * cannot run on this machine - e.g. the .NET toolchain is missing or
     * its build fails. Call as the first line of every test method.
     */
    protected static void requireSdk(Sdk sdk) {
        String problem = Invoker.availabilityProblem(sdk);
        if (problem != null) {
            throw new SkipException(sdk.label() + " unavailable on this machine: " + problem);
        }
    }

    /**
     * Cross-joins every {@link Sdk} with a set of raw data rows, prefixing
     * each row with its Sdk. E.g. 7 BVA rows -> 21 rows of {Sdk, id, expected}.
     * Every operation's data provider calls this instead of hand-copying
     * the same rows once per language.
     */
    protected static Object[][] crossWithSdk(Object[][] rows) {
        Sdk[] sdks = Sdk.values();
        Object[][] cases = new Object[sdks.length * rows.length][];
        int index = 0;
        for (Sdk sdk : sdks) {
            for (Object[] row : rows) {
                Object[] withSdk = new Object[row.length + 1];
                withSdk[0] = sdk;
                System.arraycopy(row, 0, withSdk, 1, row.length);
                cases[index++] = withSdk;
            }
        }
        return cases;
    }

    /** Common assertion logic for a lookup-style operation. */
    protected void assertResult(Invoker.Result actual, int id, String expectedValue) {
        if (expectedValue == null) {
            org.testng.Assert.assertEquals(actual.exitCode, 1,
                    "expected 'not found' exit code for id " + id + " | output: " + actual.raw);
            org.testng.Assert.assertNull(actual.value,
                    "expected no value for id " + id + " | output: " + actual.raw);
            // Exit code 1 alone is not proof of a clean "not found": an uncaught
            // exception in a CLI SDK also exits 1. Require the output to actually
            // look like a not-found response (CLI message, or in-JVM null return).
            org.testng.Assert.assertTrue(
                    actual.raw.contains("not found") || actual.raw.endsWith("-> null"),
                    "output does not look like a clean 'not found' response for id "
                            + id + " (SDK crash?) | output: " + actual.raw);
        } else {
            org.testng.Assert.assertEquals(actual.exitCode, 0,
                    "expected success exit code for id " + id + " | output: " + actual.raw);
            org.testng.Assert.assertEquals(actual.value, expectedValue,
                    "wrong value for id " + id);
        }
    }
}
