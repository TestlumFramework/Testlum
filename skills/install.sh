#!/bin/sh
# Copies the Testlum skills to Claude Code (~/.claude/skills) and Codex (~/.agents/skills).
#   install.sh                 install the skills
#   install.sh --codex-mcp     also add the testlum server to ~/.codex/config.toml if it is not there
#   install.sh --remove-old    also remove the old single "testlum" skill
set -eu
here=$(cd "$(dirname "$0")" && pwd)
launcher="$here/../modules/mcp/launch/start-testlum-mcp.sh"

backup="$HOME/.testlum-skills-backup/$(date +%Y%m%d-%H%M%S)"

for dest in "$HOME/.claude/skills" "$HOME/.agents/skills"; do
  mkdir -p "$dest"
  for s in "$here"/testlum-*; do
    [ -d "$s" ] || continue
    if [ -e "$dest/$(basename "$s")" ]; then
      mkdir -p "$backup/$(basename "$(dirname "$dest")")"
      mv "$dest/$(basename "$s")" "$backup/$(basename "$(dirname "$dest")")/"
      echo "old $(basename "$s") saved in $backup"
    fi
    cp -R "$s" "$dest/"
  done
  echo "installed to $dest"
done

for arg in "$@"; do
  case "$arg" in
    --remove-old)
      for old in "$HOME/.claude/skills/testlum" "$HOME/.agents/skills/testlum"; do
        [ -e "$old" ] || continue
        mkdir -p "$backup/old"
        mv "$old" "$backup/old/$(basename "$(dirname "$(dirname "$old")")")-$(basename "$(dirname "$old")")"
      done
      echo "moved the old testlum skill to $backup" ;;
    --codex-mcp)
      cfg="$HOME/.codex/config.toml"
      mkdir -p "$HOME/.codex"
      [ -f "$cfg" ] && { mkdir -p "$backup"; cp "$cfg" "$backup/config.toml"; }
      if grep -q '^\[mcp_servers.testlum\]' "$cfg" 2>/dev/null; then
        echo "codex config already has testlum"
      else
        printf '\n[mcp_servers.testlum]\ncommand = "sh"\nargs = ["%s"]\nstartup_timeout_sec = 120\ntool_timeout_sec = 600\n' "$launcher" >> "$cfg"
        echo "added testlum to $cfg"
      fi ;;
  esac
done
