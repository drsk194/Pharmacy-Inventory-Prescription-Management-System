# PIPMS Selenium Automation

A Selenium + pytest UI automation suite has been added under `selenium_tests/`.

Run prerequisites:
1. Start MySQL and the Spring Boot backend on `http://localhost:8080`.
2. Start the Vite frontend, normally on `http://localhost:8081`.
3. Install Python dependencies from `selenium_tests/requirements.txt`.
4. Set `PIPMS_ADMIN_IDENTIFIER` and `PIPMS_ADMIN_PASSWORD` for authenticated tests.

Example:
```powershell
python -m pip install -r selenium_tests/requirements.txt
$env:PIPMS_ADMIN_IDENTIFIER="admin@example.com"
$env:PIPMS_ADMIN_PASSWORD="your-password"
python -m pytest selenium_tests/tests --base-url http://localhost:8081 --headed
```

The suite deliberately skips authenticated tests when credentials are absent and saves screenshot/HTML artifacts on failures.
