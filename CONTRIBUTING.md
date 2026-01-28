# Contributing to Testlum

Thank you for considering contributing to Testlum! We welcome all contributions: bug reports, feature requests, code improvements, documentation updates, and more.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Prerequisites](#prerequisites)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Code Style](#code-style)
- [How to Contribute](#how-to-contribute)
- [Pull Request Process](#pull-request-process)
- [Reporting Issues](#reporting-issues)
- [Getting Help](#getting-help)

## Code of Conduct

By participating in this project, you agree to maintain a welcoming and respectful environment for everyone. Please be considerate and constructive in all interactions.

## Prerequisites

Before contributing, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Git** for version control
- **Docker** (optional, for container-based testing)

Verify your setup:

```bash
java -version    # Should show Java 17+
mvn -version     # Should show Maven 3.8+
```

## Development Setup

1. **Fork the repository** on GitHub

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Testlum.git
   cd Testlum
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/TestlumFramework/Testlum.git
   ```

4. **Build the project**
   ```bash
   mvn clean install -Pprofessional -DskipTests
   ```

5. **Run tests**
   ```bash
   mvn test -Pprofessional
   ```

## Project Structure

```
Testlum/
├── engine/              # Core engine module
├── modules/             # Feature modules
│   ├── http/            # HTTP API testing
│   ├── ui/              # UI testing (web, mobile, native)
│   ├── postgres/        # PostgreSQL support
│   ├── mongo/           # MongoDB support
│   ├── kafka/           # Kafka messaging
│   ├── rabbit/          # RabbitMQ messaging
│   └── ...              # Other integrations
├── checkstyle.xml       # Code style rules
├── Dockerfile           # Docker build configuration
└── run-docker           # Docker run script
```

## Code Style

This project uses **Checkstyle** to enforce consistent code formatting. The rules are defined in `checkstyle.xml`.

### Key Guidelines

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Maximum 120 characters
- **Braces**: Always use braces for control statements
- **Naming conventions**:
    - Classes: `PascalCase`
    - Methods/variables: `camelCase`
    - Constants: `UPPER_SNAKE_CASE`
- **JavaDoc**: Required for all public classes and methods

### Verify Code Style

Checkstyle runs automatically during the build. To check manually:

```bash
mvn checkstyle:check
```

## How to Contribute

### 1. Create a Branch

Always create a new branch for your work:

```bash
git checkout -b <type>/<description>
```

Branch naming conventions:
- `feature/add-redis-support` - New features
- `fix/null-pointer-exception` - Bug fixes
- `docs/update-readme` - Documentation
- `refactor/simplify-parser` - Code refactoring
- `test/add-kafka-tests` - Test additions

### 2. Make Your Changes

- Write clean, readable code
- Follow the existing code style
- Include JavaDoc for public APIs
- Add unit tests for new functionality
- Keep commits small and focused

### 3. Commit Your Changes

Write clear, meaningful commit messages:

```bash
git commit -m "Add Redis connection pooling support"
```

Commit message guidelines:
- Use present tense ("Add feature" not "Added feature")
- Keep the first line under 72 characters
- Reference issues when applicable: "Fix #123: Handle null responses"

### 4. Sync with Upstream

Before submitting, sync with the latest changes:

```bash
git fetch upstream
git rebase upstream/main
```

### 5. Run Quality Checks

Ensure all checks pass before submitting:

```bash
mvn clean install -Pprofessional
```

### 6. Push and Create PR

```bash
git push origin <your-branch>
```

Then open a Pull Request on GitHub.

## Pull Request Process

1. **Fill out the PR template** with a clear description
2. **Link related issues** using keywords (e.g., "Closes #123")
3. **Ensure CI passes** - all checks must be green
4. **Respond to feedback** - be open to suggestions and changes
5. **Keep PR focused** - one feature/fix per PR

### PR Checklist

- [ ] Code follows the project style guidelines
- [ ] JavaDoc added for new public APIs
- [ ] All tests pass locally
- [ ] No merge conflicts with main branch
- [ ] PR description clearly explains the changes

## Reporting Issues

### Bug Reports

When reporting bugs, please include:

- **Description**: Clear summary of the issue
- **Steps to reproduce**: Detailed steps to recreate the bug
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **Environment**: Java version, OS, Testlum version
- **Logs/Screenshots**: Any relevant error messages or screenshots

### Feature Requests

For new features, please describe:

- **Use case**: Why is this feature needed?
- **Proposed solution**: How should it work?
- **Alternatives considered**: Other approaches you've thought of

Open issues at: [GitHub Issues](https://github.com/TestlumFramework/Testlum/issues/new)

## Getting Help

- **Discord**: [Join our community](https://discord.gg/JxfcZPqBNY)
- **Documentation**: [Testlum Docs](https://testlum.developerhub.io/version-2/overview)
- **GitHub Discussions**: Open a discussion for questions

## License

By contributing to Testlum, you agree that your contributions will be licensed under the [Apache 2.0 License](LICENSE).
