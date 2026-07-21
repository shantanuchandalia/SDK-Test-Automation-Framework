package sdk.util;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.util.Arrays;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import sdk.invoker.Invoker;
import sdk.invoker.Sdk;

/**
 * Shared plumbing for the SDK test classes:
 * - ExtentReports lifecycle (report written to target/ExtentReport.html)
 * - {@link #crossWithSdk}, the helper that turns one operation's raw test
 *   data into {Sdk, ...row} cases across all three SDKs, so growing the
 *   suite is "add data rows / add operations", not "add test classes"
 */
public abstract class BaseTest {

    private static ExtentReports extent;

    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("target/ExtentReport.html");
            spark.config().setDocumentTitle("SDK Automation Framework");
            spark.config().setReportName("SDK Automation Suite (Java / Python / .NET)");
            extent = new ExtentReports();
            extent.attachReporter(spark);
        }
        Invoker.ensureDotnetBuilt();
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
                test.skip("Skipped");
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        if (extent != null) {
            extent.flush();
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

    /** Common assertion logic shared by every operation's test method. */
    protected void assertResult(Invoker.Result actual, int id, String expectedUsername) {
        if (expectedUsername == null) {
            org.testng.Assert.assertEquals(actual.exitCode, 1,
                    "expected 'not found' exit code for id " + id + " | output: " + actual.raw);
            org.testng.Assert.assertNull(actual.username,
                    "expected no username for id " + id + " | output: " + actual.raw);
        } else {
            org.testng.Assert.assertEquals(actual.exitCode, 0,
                    "expected success exit code for id " + id + " | output: " + actual.raw);
            org.testng.Assert.assertEquals(actual.username, expectedUsername,
                    "wrong username for id " + id);
        }
    }
}
