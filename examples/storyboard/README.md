# Storyboard

A tiny story tracker (Node + Postgres) used to try Testlum end to end. The stories are made up.

Run the site and the tests with one command:

    docker compose run --build --rm tests

That builds the images the first time (the engine compile takes a while), starts Postgres and the site, then runs all 26 runs and exits with the engine's exit code. The site is also on http://localhost:18080 and Postgres on localhost:15432 (user, password and database are all `storyboard`) while it is up. The images rebuild on every run, so the site always matches the code. Stop it with `docker compose down -v --remove-orphans`.

Nine scenarios, 26 runs since each CSV row is a run (the first six are: listing, lookup by id (5 stories), not found, create, rejected input (5 bad payloads) and moving a story between statuses (3 runs)), then three for SB-14.

The tests were generated with the MCP tools in `../../modules/mcp`, not typed by hand. The one manual edit is the Postgres block in `tests/config/local/integration.xml`, which points at host.docker.internal.

## Seed data

Statuses are `todo`, `doing` and `done`. Points go from 1 to 13. The only assignees are Ana and Ben; some stories have none.

| id | title | status | points | assignee |
|---|---|---|---|---|
| 1 | Login page | done | 3 | Ana |
| 2 | Password reset email | doing | 5 | Ben |
| 3 | Dashboard totals | todo | 8 | none |
| 4 | Export to CSV | todo | 2 | Ana |
| 5 | Dark mode | todo | 1 | none |

## SB-14

`tickets/SB-14.md` is a made-up ticket for filtering by assignee. The site only builds it when started with `SB14=1`, so the same tests can be shown failing and then passing:

    docker compose run --build --rm tests            (SB-14 tests fail, 8 of 26 runs)
    SB14=1 docker compose run --build --rm tests     (all 26 pass)

After switching the flag, run `docker compose down -v --remove-orphans` first so the site restarts. The SB-14 tests are `sb14-*`, the older ones are tagged SB-1 to SB-4.

The tests reach the site as `host.docker.internal` on the published ports, which works from the compose test run and from the MCP container alike.
