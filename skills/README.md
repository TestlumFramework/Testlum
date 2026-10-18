# Testlum skills

Five skills that teach an agent how to work with Testlum through the MCP server in `modules/mcp/`. They are plain `SKILL.md` files, so the same copy works in Claude Code and in Codex. They use the testlum tools by name and do not rely on anything specific to one agent.

| Skill | Use it to | Example prompt |
|---|---|---|
| testlum-generate | Write tests from your description, a work item or an API spec. Negative cases and variation files are part of it. | "Write Testlum tests for SB-14. Include a not-found case and a few spellings of the name." |
| testlum-maintain | Find stale tests (an endpoint that is gone, a ticket that was deleted) and delete or regenerate them. | "Which Testlum tests are stale? Tell me before you change anything." |
| testlum-triage | Work out whether a failing test or the app is wrong. Fixes the test, or writes up the bug. | "The nightly run failed. Triage it." |
| testlum-coverage | Show which requirements of a ticket have passing tests. | "Coverage check for SB-14." |
| testlum-data | Seed and clean the data tests rely on so they do not collide. | "The move-story test leaves rows behind. Fix the cleanup." |

## Conventions the skills rely on

- Tests carry their ticket id first in the tags, like `SB-14,api`. Coverage and staleness match on it.
- Tools run in a container where the tests folder is `/work`. Leave `dir` out and they use it.
- Expected values come from the ticket or the API description, never from what the app returned.
- Work items are read through an issue tracker tool if the agent has one (read-only). Without one, paste the text.

## Install

From the repo root, `skills/install.sh` (macOS, Linux) or `skills/Install-TestlumSkills.ps1` (Windows) copies the skills to `~/.claude/skills` for Claude Code and `~/.agents/skills` for Codex. Codex reads user skills from that folder and Claude Code reads the first one.

Optional switches: `--remove-old` or `-RemoveOld` removes the earlier single `testlum` skill. `--codex-mcp` or `-CodexMcp` adds the server to `~/.codex/config.toml` as `[mcp_servers.testlum]` running the launcher, if it is not already there. To register the server with Claude Code, run one line (adjust the path to your checkout): `claude mcp add --scope user testlum -- /path/to/Testlum/modules/mcp/launch/start-testlum-mcp.sh`. On Windows use `claude mcp add --scope user testlum -- pwsh -NoProfile -File C:\path\to\Testlum\modules\mcp\launch\Start-TestlumMcp.ps1`.
