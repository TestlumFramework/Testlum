---
name: testlum-generate
description: Write Testlum API tests from a description, a work item or an API spec, including negative cases and variation files. Use when asked to create, add or extend Testlum tests.
---

# Generate Testlum tests

Uses the testlum MCP tools. They run in a container where the tests folder is mounted at `/work`. Leave `dir` out and every tool uses it; never pass a Windows or other host path.

Where the app runs: from inside the container the host machine is `host.docker.internal`, so a service published on port 18080 is `http://host.docker.internal:18080`. Never `localhost` (that is the container). Read the existing `config/<env>/integration.xml` before writing a new one, and reuse its aliases.

1. Find the input. It can be what the user told you, a work item (read it through the ADO tool if you have one, read-only, otherwise ask for the text) or an API description such as an OpenAPI file or a list of calls.
2. If there is no tests folder yet, call `testlum_scaffold`. Otherwise call `testlum_list_tests` first so you do not duplicate a test that exists.
3. Never guess syntax. Call `testlum_lookup` (no path for the command list, `http/get` or `assert/equal` for one command). The root element needs the xmlns that lookup prints at the end.
4. Write one scenario per behaviour with `testlum_write_test`. Every command needs a `comment` of 10+ characters. Tag the scenario with the ticket id when there is one (`PROJ-123,api`), so coverage and cleanup can find it later.
5. Names: scenario folder `<ticket>-<behaviour>` in lowercase with dashes (`sb14-no-match`), csv `<ticket>-<what-varies>.csv` (`sb14-assignees.csv`), tags start with the ticket id then a kind (`SB-14,api`). In an endpoint, write `&amp;` for `&` (it is XML) and `{{column}}` works there, for example `/api/stories?assignee={{who}}&amp;status={{status}}`.
6. Check fields, not whole bodies, when the body has ids or dates: use `assert/jsonPath` with `exists`, `equals` or `matches`. Use `expected_N.json` only for stable bodies. Filters work (`$[?(@.assignee=='Ana')]`), and the assertion reads the last HTTP response unless `fromVar` is set. To show every item matches, check `exists="false"` on the opposite filter and also check the count with `$.length()`, otherwise an empty list passes.
7. Negative tests: bad input, missing fields, unknown ids, wrong types. Put the real error message from the API in the assertion.
8. Several inputs with the same shape: make one scenario, use `{{column}}` in it, add `<variations>name.csv</variations>` to its settings and pass the rows to `testlum_write_test` as the file `data/name.csv`. Each row is one run.
9. Run `testlum_run`. Read the result; every variation row is one run. A test is not done until it passes against the real app. If it fails, use the testlum-triage skill, do not just edit the test until it goes green.

Expected values come from the ticket or the API description, never from what the app happened to return.
