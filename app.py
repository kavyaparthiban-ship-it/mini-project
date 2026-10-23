#!/usr/bin/env python3
"""Start the CrowdSense backend and frontend on Windows."""

import http.client
import os
import shutil
import subprocess
import sys
import time
import urllib.error
import urllib.request
import webbrowser
from pathlib import Path

ROOT = Path(__file__).parent.resolve()
BACKEND_DIR = ROOT / "backend"
FRONTEND_DIR = ROOT / "frontend"
MAVEN_BIN = Path(r"C:\Users\KiTE\tools\apache-maven-3.9.9\bin")
BACKEND_URL = "http://localhost:8080/api/health"
FRONTEND_URL = "http://localhost:5500"


def wait_for_url(url, process, timeout, service_name):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        if process is not None and process.poll() is not None:
            raise RuntimeError(
                f"{service_name} stopped during startup. Check its console for errors."
            )
        try:
            with urllib.request.urlopen(url, timeout=2) as response:
                if response.status == 200:
                    return
        except (http.client.HTTPException, urllib.error.URLError, TimeoutError):
            pass
        time.sleep(1)

    raise TimeoutError(
        f"{service_name} did not respond within {timeout} seconds. "
        "Check its console and confirm MySQL is running with the CrowdSense "
        "schema and sample data loaded."
    )


def stop_process_tree(process):
    if process is not None and process.poll() is None:
        subprocess.run(
            ["taskkill", "/PID", str(process.pid), "/T", "/F"],
            check=False,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )


def main():
    if not BACKEND_DIR.is_dir() or not FRONTEND_DIR.is_dir():
        raise FileNotFoundError("The backend or frontend folder is missing.")

    maven_on_path = shutil.which("mvn.cmd") or shutil.which("mvn")
    if not MAVEN_BIN.is_dir() and not maven_on_path:
        raise FileNotFoundError(
            "Maven was not found. Install Maven or add its bin folder to PATH."
        )

    env = os.environ.copy()
    if MAVEN_BIN.is_dir():
        env["PATH"] = str(MAVEN_BIN) + os.pathsep + env.get("PATH", "")

    backend_process = None
    frontend_process = None
    new_console = getattr(subprocess, "CREATE_NEW_CONSOLE", 0)

    try:
        if not url_is_available(BACKEND_URL):
            print("Starting CrowdSense backend at http://localhost:8080 ...", flush=True)
            backend_process = subprocess.Popen(
                ["cmd.exe", "/k", "mvn.cmd", "spring-boot:run"],
                cwd=str(BACKEND_DIR),
                env=env,
                creationflags=new_console,
            )

            print("Waiting for the backend (up to 90 seconds)...", flush=True)
            wait_for_url(BACKEND_URL, backend_process, 90, "Backend")
        else:
            print("CrowdSense backend is already running.", flush=True)

        if not url_is_available(FRONTEND_URL):
            print("Starting CrowdSense frontend at http://localhost:5500 ...", flush=True)
            frontend_process = subprocess.Popen(
                [sys.executable, "-m", "http.server", "5500"],
                cwd=str(FRONTEND_DIR),
                creationflags=new_console,
            )
            wait_for_url(FRONTEND_URL, frontend_process, 15, "Frontend")
        else:
            print("CrowdSense frontend is already running.", flush=True)

        print("CrowdSense is ready. Opening the welcome page.", flush=True)
        webbrowser.open(f"{FRONTEND_URL}/")
        print("Keep the server windows open while using CrowdSense.", flush=True)
        while True:
            if frontend_process is not None and frontend_process.poll() is not None:
                raise RuntimeError("The frontend server stopped.")
            if not url_is_available(BACKEND_URL):
                raise RuntimeError("The backend stopped. Check its console for errors.")
            time.sleep(1)
    except KeyboardInterrupt:
        print("\nStopping CrowdSense...", flush=True)
    finally:
        stop_process_tree(frontend_process)
        stop_process_tree(backend_process)


def url_is_available(url):
    try:
        with urllib.request.urlopen(url, timeout=2) as response:
            return response.status == 200
    except (http.client.HTTPException, urllib.error.URLError, TimeoutError):
        return False


if __name__ == "__main__":
    try:
        main()
    except (FileNotFoundError, RuntimeError, TimeoutError) as error:
        print(f"\nCrowdSense could not start: {error}", file=sys.stderr, flush=True)
        sys.exit(1)
