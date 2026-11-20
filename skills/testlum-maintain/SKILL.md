---
name: testlum-maintain
description: Keep a Testlum test battery healthy: find stale tests, delete or regenerate them. Use when asked to clean up, refresh or review existing Testlum tests.
---

# Maintain Testlum tests

1. Call `testlum_list_tests`. It gives each scenario's tags, variations file, description, every call as `VERB /endpoint`, and includes.
2. A test is stale when one of these is true:
   - it calls an endpoint or verb that is gone from the API description or the code you were pointed at
   - its ticket tag points to a work item that was deleted, or changed so the test no longer matches it (read it through your issue tracker tool if you have one, read-only). A closed ticket alone is not a reason: finished work still needs its tests
   - it is marked INACTIVE and nobody can say why
3. Say what you found before changing anything: name, why it is stale, and what you suggest (delete, regenerate, leave).
4. Delete with `testlum_delete_test`. It refuses while another scenario includes the test, and never deletes data files; it names the csv files nothing uses any more, so tell the user which ones are left.
5. Regenerate with the testlum-generate skill from the current ticket or API description, then `testlum_write_test` with `overwrite` set.
6. Finish with `testlum_check` and `testlum_run`. Report what was deleted, what was rewritten and the run result.

Never delete a test only because it fails. A failing test is for the testlum-triage skill.
