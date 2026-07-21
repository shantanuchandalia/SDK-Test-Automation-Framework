# SDK Automation Framework — getUserById Demo

Demo SDK method `getUserById` implemented in three languages, all reading
the same flat file (`data/users.json`) and returning the username for a
given user ID.

## Structure

```
SDK Automation Framework/
├── data/
│   └── users.json            # shared flat file (single source of truth)
├── java/
│   └── GetUserById.java      # plain JDK 11+, no dependencies
├── dotnet/
│   ├── GetUserById.cs        # C# console app, System.Text.Json (built-in)
│   ├── GetUserById.csproj    # .NET 8
│   └── NuGet.config          # clears package sources (offline build, zero deps)
├── python/
│   └── get_user_by_id.py     # Python 3.8+, stdlib only
├── tests/                    # Maven + TestNG automation framework
│   ├── pom.xml               # TestNG 7.10.2 + ExtentReports 5.1.2
│   ├── testng.xml            # suite: sdk.tests.SdkOperationTest
│   └── src/test/java/sdk/
│       ├── tests/                     # actual test classes (@Test methods)
│       │   └── SdkOperationTest.java      # one @DataProvider/@Test pair per operation
│       ├── util/                      # shared test utilities, not tests themselves
│       │   └── BaseTest.java              # ExtentReports lifecycle + crossWithSdk() helper
│       └── invoker/                   # invocation framework, kept apart from test classes
│           ├── Sdk.java                   # enum: JAVA, PYTHON, DOTNET
│           ├── OperationSpec.java         # one operation's per-language binding
│           ├── OperationRegistry.java     # registered operations (add here to add an operation)
│           ├── SdkInvocationStrategy.java # interface: how a language is invoked
│           ├── Invoker.java               # gateway: invoke(sdk, operationName, args...)
│           ├── CliSupport.java            # shared CLI-invocation helper (Python/.NET strategies)
│           ├── java/JavaInvocationStrategy.java       # in-JVM via reflection
│           ├── python/PythonInvocationStrategy.java   # CLI: python <script> <args>
│           └── dotnet/DotnetInvocationStrategy.java   # CLI: dotnet <dll> <args>
├── framework-flow.mermaid    # architecture flow diagram (simple overview)
└── framework-flow-detailed.mermaid  # same flow, drilldown to package/class level
```

## Running the test suite

From `tests/` (needs Maven, JDK 11+, Python 3, .NET 8 SDK on PATH):

```
mvn test
```

- 21 tests: 7 BVA cases (100, 101, 103, 105, 106, 0, -1) x 3 languages
- The .NET SDK is built automatically once per suite if its DLL is missing
- Extent report: `tests/target/ExtentReport.html`
- TestNG/Surefire reports land in `tests/target/surefire-reports/`

## How to run

Each implementation accepts an ID as a CLI argument; with no argument it
runs a built-in demo lookup for ID 101. Exit codes: 0 = found, 1 = not
found, 2 = bad input / data file missing.

**Python** (from `python/`):

```
python get_user_by_id.py 103
```

**Java** (from `java/`):

```
javac GetUserById.java
java GetUserById 102
```

**.NET** (from `dotnet/`, requires .NET 8 SDK):

```
dotnet run -- 104
```

## Sample data

| id  | username      |
|-----|---------------|
| 101 | alice.morgan  |
| 102 | bob.sharma    |
| 103 | carol.tan     |
| 104 | david.oconnor |
| 105 | eva.kapoor    |

All three were compiled/executed and verified: valid IDs return the
correct username, unknown IDs report "not found", and non-numeric input
is rejected.
