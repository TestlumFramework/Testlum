<#
Starts the Testlum MCP server for one client session in a container that is removed when the session ends.
Point the MCP client config at this script. Build the image once, from the repository root:
  docker build -f modules/mcp/Dockerfile -t testlum-mcp:0.5.0 .

  Start-TestlumMcp.ps1 [-TestsDir <folder>]   TestsDir is mounted at /work; defaults to $env:TESTLUM_WORKDIR, then the current folder

Env: TESTLUM_MCP_IMAGE (default testlum-mcp:0.5.0), TESTLUM_MCP_IDLE_SECONDS (default 900).
#>
param(
    [string]$TestsDir = $(if ($env:TESTLUM_WORKDIR) { $env:TESTLUM_WORKDIR } else { (Get-Location).Path })
)

$image = if ($env:TESTLUM_MCP_IMAGE) { $env:TESTLUM_MCP_IMAGE } else { 'testlum-mcp:0.5.0' }
$idle = if ($env:TESTLUM_MCP_IDLE_SECONDS) { $env:TESTLUM_MCP_IDLE_SECONDS } else { '900' }

& docker image inspect $image *> $null
if ($LASTEXITCODE -ne 0) {
    [Console]::Error.WriteLine("testlum-mcp: image $image not found, or Docker is not running. Build it from the repository root:")
    [Console]::Error.WriteLine("  docker build -f modules/mcp/Dockerfile -t $image .")
    exit 1
}

& docker run --rm -i --name "testlum-mcp-$PID" --label app=testlum-mcp --memory 2g --cpus 2 `
    --add-host=host.docker.internal:host-gateway -e "TESTLUM_MCP_IDLE_SECONDS=$idle" `
    -e "TESTLUM_HOST_WORKDIR=$TestsDir" -v "${TestsDir}:/work" $image
exit $LASTEXITCODE
