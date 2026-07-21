# SDK Automation Framework — getUserById Demo

[![CI](https://github.com/shantanuchandalia/SDK-Test-Automation-Framework/actions/workflows/ci.yml/badge.svg)](https://github.com/shantanuchandalia/SDK-Test-Automation-Framework/actions/workflows/ci.yml)

Demo SDK method `getUserById` implemented in three languages, all reading
the same flat file (`data/users.json`) and returning the username for a
given user ID. One TestNG suite runs the same scenarios with the same
assertions against all three implementations.

## Structure

```
SDK Automation Framework/
├── .github/workflows/ci.yml  # GitHub Actions: full suite on every push/PR
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
│       │   └── BaseTest.java              # ExtentReports lifecycle, crossWithSdk(), requireSdk()
│       └── invoker/                   # invocation framework, kept apart from test classes
│           ├── Sdk.java                   # enum: JAVA, PYTHON, DOTNET
│           ├── OperationSpec.java         # one operation's per-language binding
│           ├── OperationRegistry.java     # registered operations (add here to add an operation)
│           ├── SdkInvocationStrategy.java # interface: how a language is invoked + availability
│           ├── Invoker.java               # gateway: invoke(sdk, operationName, args...)
│           ├── CliSupport.java            # repo-root discovery + shared CLI runner
│           ├── java/JavaInvocationStrategy.java       # in-JVM via reflection
│           ├── python/PythonInvocationStrategy.java   # CLI: python <script> <args>
│           └── dotnet/DotnetInvocationStrategy.java   # CLI: dotnet <dll> <args> + build probe
├── docs/
│   └── ExtentReport-sample.html  # sample of the generated HTML report
├── framework-flow.mermaid    # architecture flow diagram (simple overview)
└── framework-flow-detailed.mermaid  # same flow, drilldown to package/class level
```

## Running the test suite

From `tests/` (needs Maven, JDK 11+, Python 3; .NET 8 SDK optional — see below):

```
mvn test
```

- 23 tests: 7 BVA cases (100, 101, 103, 105, 106, 0, -1) x 3 languages,
  plus a bad-input (exit code 2) case for each CLI-invoked SDK
- The .NET SDK is built automatically once per suite if its DLL is missing
- **Missing toolchains skip, they don't fail:** if the .NET 8 SDK is not
  installed, the .NET cases are reported as skipped (with the reason) and
  the Java and Python legs still run
- Extent report: `tests/target/ExtentReport.html`
- TestNG/Surefire reports land in `tests/target/surefire-reports/`
- CI runs the full three-language suite on every push (see badge above)

### Testing model: in-JVM vs CLI

The Java SDK is tested **in-JVM at the library layer** (a direct call to
`getUserById(int)` — its CLI `main()` wrapper is not exercised). The
Python and .NET SDKs are tested **at the process level through their CLI
entry points**, which additionally covers their argument parsing and exit
codes. This is a deliberate trade-off (speed and simplicity for the
host-language SDK); it is why the bad-input test applies only to the two
CLI-invoked SDKs.

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
