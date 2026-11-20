# Testlum MCP server

An MCP server that helps agents write valid Testlum tests. It reads the engine's own XSD schemas, so it follows the engine and not the wiki. It has seven tools.

| Tool | What it does |
|---|---|
| `testlum_lookup` | Lists the commands a scenario may hold. With a path such as `http/get` or `assert/equal` it returns that element's definition from the XSD: attributes, allowed values, patterns and child elements. `kind` can be `global-config` or `integration`. |
| `testlum_scaffold` | Creates the folder layout the engine demands, with valid placeholders, and never overwrites a file. |
| `testlum_check` | Validates the global config, each `integration.xml` and each `scenario.xml` against the XSDs and reports file, line and column. It also fails when an enabled environment has no `integration.xml`. Aliases and includes are checked by the engine when it runs, so `testlum_run` has the final word. |
| `testlum_run` | Runs the engine headless and returns the exit code, the scenario counts and a pass or fail line per run read from `report/junit.xml`, plus the full failure text. Exit zero counts as passed only when the report shows runs, none failed and no testsuite declares failures. Run only some scenarios with `tags`. |
| `testlum_write_test` | Writes a scenario and its data files in one step: `expected_1.json` and friends next to `scenario.xml`, `data/<name>.csv` in the data folder, which is where `<settings><variations>` reads from. The scenario must pass the schema or nothing is written. Files are written beside their targets, the files being replaced are backed up, and then everything is moved in; if a step fails the originals are put back and the new files removed, and anything that could not be cleaned up is named. Refuses to replace an existing scenario or file unless `overwrite` is set, and never a csv that another scenario reads or that a scenario which cannot be read might read. |
| `testlum_list_tests` | Lists the scenarios in a tests folder: tags (ticket ids), variations file, description, every call as `VERB /endpoint`, every include. |
| `testlum_delete_test` | Deletes a scenario and the scenarios under it. Refuses while a scenario outside them includes one, found the way the engine finds an include (relative parts and the nearest scenario above a missing folder count; an include with a variable counts as naming any scenario it could), or while any scenario cannot be read. Never deletes data files: it names the csv files nothing uses any more and leaves them for you. |

An argument a tool does not know is an error, not ignored.

## Configuration

- `TESTLUM_ENGINE_JAR`: path to the engine jar. `testlum_run` runs it.
- `TESTLUM_XSD_DIR`: a folder of the engine's `schema/*.xsd`. The image sets both.
- The server runs until the client closes stdin, which also ends and removes its container.

The engine reads `--tags=a,b` (or `-t=a,b`) and then ignores the tag setting in the global config. `testlum_run` always passes `--junit`, so the JUnit report is written whether or not the global config has `<junit enabled="true"/>`, and its result contains a line `junit: report/junit.xml (under <tests folder>)` after the summary.

## JUnit XML report

- Switch it on with the engine argument `--junit`, or with `<junit enabled="true"/>` in the `<report>` of the global config (`testlum_scaffold` writes that element). Either one is enough. The XML is written together with the HTML report, so `<report>` still needs its `<extentReports>` element.
- The file is `report/junit.xml` under the `--path` folder. The engine removes the file of an earlier run before it starts, so a file found after a run was written by that run.
- Each scenario is one testcase. A failed scenario records, in this order: a message line `step <n> <command>: <first line of the cause>`, then the labeled lines `step:`, `command:`, `comment:` (only when the step has one), `error-type:` (the exception class) and `http:` (expected and actual status, HTTP steps only), then the full cause text. For include, repeat and assert wrappers the innermost failing step is named, and `inner step:` gives its position. A scenario that fails before its first step (for example a missing variable in its overview) is a failed testcase with step `before step 1`. A body mismatch starts with the comparator's own message, followed by the first 150 characters of the expected and actual bodies.
- Missing-variable messages list the variable names, never their values.
- The HTML report prints the request header values in clear text. Do not publish the HTML report when a live token is sent in a header.
- `junit.xml` can still carry a secret: a response body that echoes a token or credential appears in the failure text, as does a secret built into a URL or typed as a literal in a scenario. Response header values can reach it too: a header mismatch prints the response headers (up to 150 characters) and the expected headers, so a `Set-Cookie` value can appear. Known exposure: variables are injected into expected bodies, expected headers and URLs before they are printed, so a variable's value reaches `junit.xml` when it sits in any of those. There is no masking; keep secrets out of expected bodies, expected headers and URLs, or do not publish the file. Check before publishing: run the pipeline once with a dummy marker token such as `TLMARKER-7f3a91c2-DO-NOT-USE` (also URL-encoded, JSON-escaped and base64) through the same injection route as the real token, with a failing HTTP step against a service that echoes the Authorization header, a misspelled `{{variable}}`, a failing assert and a scenario with `{{missing}}` in its overview. Search every file you intend to publish and the task log for the marker. Publish only when `junit.xml` has no hit, its `tests` count equals the scenarios run, and every failure shows `step:` and `command:`. Repeat the check when the image, a scenario or the injection route changes. Treat a missing `junit.xml` as a failure.

## Docker

Build the image once, from the repository root. The first build compiles the engine and takes a while.

    docker build -f modules/mcp/Dockerfile -t testlum-mcp:0.5.0 .

The client config points at a launcher, not at a running container. The launcher stops with a clear message if the image is missing, then runs `docker run -i --rm`. The container lives for one client session and is removed when it ends. It is labelled `app=testlum-mcp`.

- Windows: `launch/Start-TestlumMcp.ps1 [-TestsDir <folder>]`
- macOS and Linux: `launch/start-testlum-mcp.sh [tests-dir]`

Names are compared ignoring case on every system. Case-variant names are refused in Docker Desktop on Windows over NTFS, which was run on a Windows PC. The tests folder is mounted at `/work`, so tool arguments use `/work/...` paths. With no argument the launchers use `TESTLUM_WORKDIR`, then the folder the MCP client starts them in, so one registration serves every project.

MCP client config (Windows example):

    { "command": "pwsh", "args": ["-NoProfile", "-File", "C:\\path\\to\\modules\\mcp\\launch\\Start-TestlumMcp.ps1"] }

macOS or Linux:

    { "command": "/path/to/modules/mcp/launch/start-testlum-mcp.sh" }

Claude Code gives an MCP server 30 seconds to start (`MCP_TIMEOUT`, in milliseconds). The launcher exits at once when Docker is not running, so a start that runs past the limit is the container and the JVM coming up. If the server shows as failed on the first start after a reboot and works when retried, raise the limit in the `env` block of the user settings file `~/.claude/settings.json` (`%USERPROFILE%\.claude\settings.json` on Windows), the same syntax on every system, and restart Claude Code:

    { "env": { "MCP_TIMEOUT": "60000" } }

Inside the container `127.0.0.1` is the container itself. A stub on the host must bind `0.0.0.0` and the tests must reach it at `host.docker.internal`. The launchers add the host mapping, so the name also works on Linux.

## Build

The server is a module of the Testlum reactor, off by default so a normal engine build is unchanged. The `mcp` profile adds it.

    mvn -Pmcp -pl modules/mcp package

That runs checkstyle and the unit tests. The module uses Spring like the engine does.

## Demo

`examples/storyboard` holds a small site, a Postgres database and a set of tests. From that folder, with Docker running:

    docker compose run --build --rm tests

See its README for what it shows and how to switch the sample bug on and off.

## Tests

- `mvn -Pmcp -pl modules/mcp test`: unit tests against the XSDs in this repo.
- `python3 modules/mcp/e2e/e2e.py modules/mcp/target/testlum-mcp.jar <engine.jar>`: drives the server over stdio against a real engine and a local stub API.
