"""End to end check: drive the MCP server over stdio against a real engine jar and a local stub API.

Usage: python3 e2e.py <testlum-mcp.jar> <engine.jar>
"""
import json
import os
import shutil
import subprocess
import sys
import tempfile
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path

server_jar, engine_jar = sys.argv[1], sys.argv[2]


class Stub(BaseHTTPRequestHandler):
    def do_GET(self):
        body = json.dumps({"id": 1, "name": "Leanne"}).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, *a):
        pass


httpd = HTTPServer(("127.0.0.1", 0), Stub)
threading.Thread(target=httpd.serve_forever, daemon=True).start()
port = httpd.server_address[1]

xsd_dir = Path(__file__).resolve().parents[3] / "engine" / "src" / "main" / "resources" / "schema"
env = dict(os.environ, TESTLUM_ENGINE_JAR=engine_jar, TESTLUM_XSD_DIR=str(xsd_dir))
proc = subprocess.Popen(["java", "-jar", server_jar], stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE, text=True, env=env)
_id = 0


def rpc(method, params=None):
    global _id
    _id += 1
    proc.stdin.write(json.dumps({"jsonrpc": "2.0", "id": _id, "method": method, "params": params or {}}) + "\n")
    proc.stdin.flush()
    return json.loads(proc.stdout.readline())["result"]


def call(tool, **args):
    r = rpc("tools/call", {"name": tool, "arguments": args})
    return r["isError"], r["content"][0]["text"]


def expect(cond, label, detail=""):
    print(("ok   " if cond else "FAIL ") + label)
    if not cond:
        print(detail)
        proc.kill()
        sys.exit(1)


init = rpc("initialize", {"protocolVersion": "2025-03-26"})
expect(init["serverInfo"]["name"] == "testlum-mcp", "initialize")
names = [t["name"] for t in rpc("tools/list")["tools"]]
expect(names == ["testlum_lookup", "testlum_scaffold", "testlum_check", "testlum_run", "testlum_write_test", "testlum_list_tests", "testlum_delete_test"], "tools/list", names)

err, text = call("testlum_lookup")
expect(not err and "http" in text and "assert" in text, "lookup lists scenario commands", text)
err, text = call("testlum_lookup", path="http/get")
expect(not err and "endpoint" in text and "required" in text, "lookup http/get shows endpoint", text)
err, text = call("testlum_lookup", path="nonsense")
expect(err and "No element 'nonsense'" in text, "lookup unknown element shows the parent definition", text)

work = Path(tempfile.mkdtemp(prefix="testlum-mcp-e2e-"))
tests = work / "tests"
err, text = call("testlum_scaffold", dir=str(tests), apiUrl=f"http://127.0.0.1:{port}")
expect(not err and "created" in text, "scaffold creates the folder", text)
err, text = call("testlum_scaffold", dir=str(tests))
expect("kept" in text and "created" not in text, "scaffold never overwrites", text)

SCENARIO = """<?xml version="1.0" encoding="UTF-8"?>
<scenario xmlns="http://www.testlum.com/testing/model/scenario">
    <overview><description>{name} reads a user from the stub</description><name>{name}</name></overview>
    <settings>{settings}</settings>
    <http comment="Get a user from the stub" alias="DEFAULT">
        <get endpoint="{endpoint}"><response code="200" file="expected_1.json"/></get>
    </http>
</scenario>
"""
single = SCENARIO.format(name="get-user", settings="<tags>smoke</tags>", endpoint="/users/1")
err, text = call("testlum_write_test", dir=str(tests), name="get-user", scenario=single.replace("/users/1", "users"))
expect(err and "endpoint" in text and not (tests / "scenarios" / "get-user").exists(), "write refuses a scenario the schema rejects", text)
err, text = call("testlum_write_test", dir=str(tests), name="get-user", scenario=single,
                 files={"expected_1.json": '{"id": 1, "name": "Leanne"}'})
expect(not err and "get-user/expected_1.json" in text, "write_test writes the scenario and its data", text)
many = SCENARIO.format(name="users", settings="<variations>users.csv</variations><tags>api</tags>", endpoint="/users/{{id}}")
err, text = call("testlum_write_test", dir=str(tests), name="group/users", scenario=many,
                 files={"expected_1.json": '{"id": 1, "name": "Leanne"}', "data/users.csv": "id\n1\n2\n"})
expect(not err and (tests / "data" / "users.csv").exists(), "write_test writes a nested scenario and its csv", text)
err, text = call("testlum_check", dir=str(tests))
expect(not err and text.startswith("PASS"), "check passes", text)
err, text = call("testlum_check", dir=str(tests), path="/work")
expect(err and "Unknown argument 'path'" in text, "an unknown argument is refused", text)
err, text = call("testlum_list_tests", dir=str(tests))
expect("group/users" in text and "variations: users.csv" in text, "list_tests shows both scenarios", text)

err, text = call("testlum_run", dir=str(tests), timeoutSeconds=300)
expect(not err and text.startswith("exit 0") and "3 total, 3 passed" in text and "PASS users #2" in text, "run passes", text)
err, text = call("testlum_run", dir=str(tests), tags="smoke", timeoutSeconds=300)
expect(text.startswith("exit 0") and "1 passed, 0 failed, 1 skipped" in text, "run with tags runs only those", text)

(tests / "scenarios" / "get-user" / "expected_1.json").write_text('{"id": 1, "name": "Somebody Else"}')
err, text = call("testlum_run", dir=str(tests), timeoutSeconds=300)
expect(text.startswith("exit 1") and "1 failed" in text and "FAIL get-user" in text, "run reports the failure", text)

err, text = call("testlum_run", dir=str(tests), tags="nomatch", timeoutSeconds=300)
expect(text.startswith("exit 1:") and "0 passed, 0 failed, 2 skipped" in text, "tags that match nothing fail the run", text)

err, text = call("testlum_delete_test", dir=str(tests), name="group/users")
expect(not err and "data/users.csv" in text and (tests / "data" / "users.csv").exists()
       and not (tests / "scenarios" / "group" / "users").exists(), "delete removes the scenario, names the csv and keeps it", text)

proc.stdin.close()
code = proc.wait(timeout=20)
expect(code == 0, "server exits when the client closes stdin", code)
shutil.rmtree(work, ignore_errors=True)
httpd.shutdown()
print("all ok")
