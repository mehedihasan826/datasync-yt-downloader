# Contributing to DataSync YT Downloader

We welcome contributions to DataSync YT Downloader! Please read the guidelines below to get started.

## Code of Conduct
Please be polite and respectful in issues, pull requests, and discussions.

## Getting Started
1. Fork the repository.
2. Clone your fork locally:
   ```bash
   git clone https://github.com/your-username/datasync-yt-downloader.git
   ```
3. Run the development environment:
   - macOS: `./scripts/run-macos.sh`
   - Windows: `.\scripts\run-windows.ps1`
4. Verify changes by running tests:
   - macOS: `./mvnw clean test`
   - Windows: `.\mvnw.cmd clean test`

## Pull Request Guidelines
- Always write clean, self-contained Java/JavaScript code.
- Maintain localization support in `src/main/resources/i18n/`.
- Do not commit your private `.env` configurations to Git.
- Document any new features or environment variables.
