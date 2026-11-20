---
name: testlum-coverage
description: Show which ticket requirements have Testlum tests and which do not. Use when asked about test coverage for a ticket, story or epic.
---

# Coverage check

1. Get the requirements: the ticket text or acceptance rows (read through your issue tracker tool if you have one, read-only, otherwise ask for the text). Number each requirement.
2. Call `testlum_list_tests`. Tests are tied to a ticket by its id in the tags (`PROJ-123`). Also look at descriptions and endpoints, since older tests may not have the tag.
3. Build a short table: requirement, the tests that cover it, status. Status is covered, partly covered (say what is missing, for example no negative case) or not covered.
4. Run `testlum_run` with the ticket's tag and add whether those tests pass. Covered only counts when it passes.
5. List tests that match no requirement. They may be stale (testlum-maintain) or the ticket may be missing a requirement.
6. Offer to write the missing tests with testlum-generate. Do not write them unasked.

A ticket is done only when every requirement is covered and passing.
