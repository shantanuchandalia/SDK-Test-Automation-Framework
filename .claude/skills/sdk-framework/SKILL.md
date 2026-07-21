---
name: sdk-framework
description: Extend or operate this multi-language SDK test framework — add a new SDK operation across Java/Python/.NET, add a new language, or run and debug the TestNG parity suite. Use when adding operations, tests, or languages to this repo, or when diagnosing parity-test failures.
---

# SDK Test Framework workflows

One TestNG suite (Java) asserts identical behaviour across every language
implementation of an SDK. Tests call `Invoker.invoke(sdk, "operationName",
args...)`; the registry maps the operation to per-language bindings; a
per-language strategy executes it; output is normalized into
`Invoker.Result {exitCode, value, raw}`.

Key files (all under `tests/src/test/java/sdk/` unless noted):

| File | Role |
|------|------|
| `invoker/OperationRegistry.java` | one `register(...)` entry per operation |
| `invoker/OperationSpec.java` | per-language binding (Java class/method/params, Python script, .NET dll) |
| `invoker/Sdk.java` | enum of languages; `cli()` = invoked via CLI entry point |
| `invoker/Invoker.java` | gateway + strategy map |
| `invoker/<lang>/…InvocationStrategy.java` | how each language is executed |
| `tests/SdkOperationTest.java` | one `@DataProvider`/`@Test` pair per operation |
| `util/BaseTest.java` | reporting, `crossWithSdk()`, `requireSdk()`, shared assertions |
| `java/`, `python/`, `dotnet/` (repo root) | the SDK implementations under test |

## The cross-language contract

Every SDK implementation must honour this, or the shared assertions cannot
treat languages identically:

| Behaviour | Exit code | Stdout |
|-----------|-----------|--------|
| Success | 0 | `operation(args) -> result` |
| Not found | 1 | message containing `not found`, no `-> ` marker |
| Bad input / error | 2 | `ERROR: ...` |

The `-> ` marker is what `CliSupport.parseResult` extracts `value` from.

## Workflow: add an SDK operation

1. **Implement it in each language**, following the contract above:
   - `java/<ClassName>.java` — **default package** (no package statement);
     a public static method; compiled into the tests automatically via
     build-helper. The test layer calls this method in-JVM (reflection),
     not the class's `main`.
   - `python/<script_name>.py` — CLI entry point taking positional args.
   - .NET: see the layout constraint below before adding a second project.
2. **Register it** — one entry in `OperationRegistry`'s static block:
   ```java
   register(new OperationSpec(
           "operationName",
           "JavaClassName", "javaMethodName", new Class<?>[] { int.class },
           "python_script.py",
           "DotnetAssembly.dll"));
   ```
3. **Test it** — one pair in `SdkOperationTest`:
   ```java
   @DataProvider(name = "operationNameCases", parallel = true)
   public Object[][] operationNameCases() {
       Object[][] rows = { { arg, "expected" }, ... };
       return crossWithSdk(rows);   // fans rows out across every Sdk
   }

   @Test(dataProvider = "operationNameCases")
   public void operationName(Sdk sdk, int arg, String expected) {
       requireSdk(sdk);   // ALWAYS the first line — enables toolchain skips
       assertResult(Invoker.invoke(sdk, "operationName", arg), arg, expected);
   }
   ```
4. **Verify** — see Run & verify below. No `testng.xml` change is needed.

Rules:
- Hardcode expected values in test data (independent oracle). Never derive
  them by reading the same fixture the SDKs read — that's a tautology.
- `expected == null` in `assertResult` means "must report not-found (exit 1)".
- A result shape other than value-or-not-found needs its own assertion
  helper in `BaseTest` — do not force it through `assertResult`.

## Workflow: add a language

1. Implement `SdkInvocationStrategy` in a new subpackage
   `invoker/<language>/` (copy `PythonInvocationStrategy` for the CLI
   pattern, ~20 lines). Override `availabilityProblem()` (lazy, cached)
   if the toolchain may be absent — return a reason string, never throw.
2. Add a constant to `Sdk` (label + `cli` flag) and register the strategy
   in `Invoker`'s `STRATEGIES` map.
3. Add the binding field(s) for the new language to `OperationSpec` and
   each registry entry. Every existing test then runs against it.

## Run & verify

```
cd tests && mvn test
```

- Suite currently: 23 tests (7 BVA cases x 3 languages + 1 bad-input case
  per CLI SDK). All must pass; skips are legitimate ONLY for missing
  toolchains (the skip reason names the SDK and cause).
- The .NET project auto-builds once per suite if its DLL is missing.
- Extent report: `tests/target/ExtentReport.html`, grouped per SDK.
- Repo root is auto-discovered by walking up to `data/users.json`;
  override with `-Dsdk.root=<path>` if the fixture moves.
- On failure, read `Result.raw` in the assertion message first — it is
  the SDK's actual stdout.
- CI (`.github/workflows/ci.yml`) runs the full suite on every push; keep
  it green.

## Gotchas

- **.NET layout constraint:** `dotnet/` holds ONE console project
  (`GetUserById.csproj` → `bin/Debug/net8.0/GetUserById.dll`). A second
  .NET operation needs a layout decision: either a project subfolder per
  operation (then `CliSupport.DOTNET_BIN_DIR` and the build probe in
  `DotnetInvocationStrategy` must resolve per-project paths) or one
  assembly dispatching on an operation argument (then the strategy must
  prepend it). Pick one deliberately; neither works out of the box.
- The Java SDK classes are in the **default package** — `Class.forName`
  in `JavaInvocationStrategy` uses the bare class name.
- Data providers run `parallel = true` on 3 threads: SDK operations must
  stay stateless (read-only fixtures) or the parallelism must be removed.
- The bad-input (exit 2) test applies only to `cli()` SDKs — the Java SDK
  is called in-JVM with typed args, so its arg parsing is not exercised.
