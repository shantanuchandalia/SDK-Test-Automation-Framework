package sdk.tests;

import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import sdk.invoker.Invoker;
import sdk.invoker.OperationRegistry;
import sdk.invoker.Sdk;
import sdk.util.BaseTest;

/**
 * SDK operation tests, one {@link DataProvider}/{@link Test} pair per
 * operation, each run against every {@link Sdk} via {@link BaseTest#crossWithSdk}.
 * Replaces the former JavaSdkTest/PythonSdkTest/DotnetSdkTest trio: adding
 * operation #2 means adding one more pair here (plus registering it in
 * {@link OperationRegistry}), not copying a class per language.
 */
public class SdkOperationTest extends BaseTest {

    /**
     * Boundary Value Analysis over the valid ID range [101, 105].
     * expectedUsername == null means the SDK must report "not found" (exit 1).
     */
    @DataProvider(name = "getUserByIdCases", parallel = true)
    public Object[][] getUserByIdCases() {
        Object[][] rows = {
            { 100, null },            // lower boundary - 1  -> not found
            { 101, "alice.morgan" },  // lower boundary      -> found
            { 103, "carol.tan" },     // nominal mid value   -> found
            { 105, "eva.kapoor" },    // upper boundary      -> found
            { 106, null },            // upper boundary + 1  -> not found
            { 0,   null },            // zero                -> not found
            { -1,  null },            // negative            -> not found
        };
        return crossWithSdk(rows);
    }

    @Test(dataProvider = "getUserByIdCases")
    public void getUserById(Sdk sdk, int id, String expectedUsername) {
        requireSdk(sdk);
        assertResult(Invoker.invoke(sdk, "getUserById", id), id, expectedUsername);
    }

    /**
     * Bad-input contract (exit code 2): CLI SDKs must reject a non-numeric ID.
     * Only CLI-invoked SDKs apply - the Java SDK is called in-JVM with a typed
     * int, so its argument parsing lives in main() and is out of this path.
     */
    @DataProvider(name = "nonNumericIdCases", parallel = true)
    public Object[][] nonNumericIdCases() {
        List<Object[]> rows = new ArrayList<>();
        for (Sdk sdk : Sdk.values()) {
            if (sdk.cli()) {
                rows.add(new Object[] { sdk, "abc" });
            }
        }
        return rows.toArray(new Object[0][]);
    }

    @Test(dataProvider = "nonNumericIdCases")
    public void getUserByIdRejectsNonNumericId(Sdk sdk, String badId) {
        requireSdk(sdk);
        Invoker.Result actual = Invoker.invoke(sdk, "getUserById", badId);
        Assert.assertEquals(actual.exitCode, 2,
                "expected bad-input exit code for id '" + badId + "' | output: " + actual.raw);
        Assert.assertNull(actual.value,
                "expected no value for bad input '" + badId + "' | output: " + actual.raw);
    }
}
