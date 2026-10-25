#!/bin/sh
# Starts the Testlum MCP server for one client session in a container that is removed when the session ends.
# Point the MCP client config at this script. Build the image once, from the repository root:
#   docker build -f modules/mcp/Dockerfile -t testlum-mcp:0.5.0 .
#
#   start-testlum-mcp.sh [TESTS_DIR]   TESTS_DIR is mounted at /work; defaults to $TESTLUM_WORKDIR, then $PWD
#
# Env: TESTLUM_MCP_IMAGE (default testlum-mcp:0.5.0).
set -eu

image=${TESTLUM_MCP_IMAGE:-testlum-mcp:0.5.0}
tests=${1:-${TESTLUM_WORKDIR:-$PWD}}

if ! docker image inspect "$image" >/dev/null 2>&1; then
  echo "testlum-mcp: image $image not found, or Docker is not running. Build it from the repository root:" >&2
  echo "  docker build -f modules/mcp/Dockerfile -t $image ." >&2
  exit 1
fi

exec docker run --rm -i --name "testlum-mcp-$$" --label app=testlum-mcp --memory 2g --cpus 2 \
  --add-host=host.docker.internal:host-gateway \
  -e TESTLUM_HOST_WORKDIR="$tests" \
  -v "$tests:/work" "$image"
