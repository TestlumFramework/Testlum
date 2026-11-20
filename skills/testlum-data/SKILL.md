---
name: testlum-data
description: Seed and reset the database or records Testlum tests rely on, so tests do not step on each other. Use when tests need setup data or leave data behind.
---

# Test data

1. Test data is set up and cleaned inside Testlum, not by hand. Setup and cleanup are their own scenarios tagged `include`, and the tests that need them `<include>` them.
2. Prefer the app's own API for seeding and cleanup (POST to create, DELETE to remove). It is the path the demo uses and it does not depend on the database layout.
3. To read the database, use `testlum_lookup` for `postgres` (a `query` compared to an expected file, shape `[{"query": "...", "content": [rows]}]`) and `var/sql` (SQL must be uppercase `SELECT ... FROM ... WHERE`). Whether the engine will run INSERT or DELETE through `postgres` is not proven. Try it on a scratch table first and say so if it does not work, then fall back to the API.
4. Mark seeded rows with a fixed, recognisable value (a prefix such as `test_` or the ticket id). Cleanup removes only rows with that marker. Never truncate a table or touch rows without it.
5. A test that creates data (a POST) removes it at the end, or relies on a unique value such as `{{suffix}}` so reruns do not collide.
6. Several tests on shared data: give each its own marker so they can run in any order.
7. Check with `testlum_check`, then run twice with `testlum_run`. The second run must pass too, otherwise something is left behind.

Never point seed or cleanup at a production database.
