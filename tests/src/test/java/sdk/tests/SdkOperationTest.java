package sdk.tests;

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
        assertResult(Invoker.invoke(sdk, "getUserById", id), id, expectedUsername);
    }
}
