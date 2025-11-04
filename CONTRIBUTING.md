# Contributing to KAgentic

Thank you for your interest in contributing to KAgentic! We welcome contributions from the community.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

## 📜 Code of Conduct

This project adheres to a code of conduct that all contributors are expected to follow:

- Be respectful and inclusive
- Welcome newcomers and help them get started
- Focus on what is best for the community
- Show empathy towards other community members

## 🚀 Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/KAgentic.git
   cd KAgentic
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/anoopg87/KAgentic.git
   ```
4. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## 🤝 How to Contribute

### Types of Contributions

We welcome various types of contributions:

- **Bug fixes**: Fix issues or bugs in the codebase
- **New features**: Add new LLM providers, tools, or agent capabilities
- **Documentation**: Improve README, KDoc comments, or examples
- **Tests**: Add or improve test coverage
- **Performance**: Optimize code for better performance
- **Examples**: Create new example projects

### Areas That Need Help

- Adding more LLM provider integrations
- Creating additional tools
- Improving error messages and logging
- Writing more comprehensive tests
- Adding vector database support
- Implementing streaming responses
- Improving documentation and examples

## 🛠️ Development Setup

### Prerequisites

- JDK 17 or higher
- Gradle 8.2 or higher
- Git
- Your favorite Kotlin IDE (IntelliJ IDEA recommended)

### Building the Project

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Build without tests
./gradlew build -x test
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "llm.LLMProviderTest"

# Run tests with coverage
./gradlew test jacocoTestReport
```

## 📏 Coding Standards

### Kotlin Style Guide

We follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Follow naming conventions:
  - Classes: `PascalCase`
  - Functions: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`

### Code Quality

- **Clean code**: Write self-documenting code with clear intent
- **SOLID principles**: Follow object-oriented design principles
- **DRY**: Don't repeat yourself
- **KISS**: Keep it simple and straightforward
- **Error handling**: Always handle errors gracefully

### Documentation

- Add KDoc comments for all public APIs
- Include usage examples in KDoc
- Update README.md for significant changes
- Add entries to CHANGELOG.md

## 🧪 Testing Guidelines

### Writing Tests

- Write tests for all new features
- Aim for at least 70% code coverage
- Use descriptive test names: `testFunctionName_Scenario_ExpectedResult`
- Include edge cases and error scenarios
- Use mock objects for external dependencies

### Test Structure

```kotlin
class MyFeatureTest {
    @Test
    fun testFeature_ValidInput_ReturnsExpectedResult() {
        // Arrange
        val input = "test input"
        val expected = "expected output"

        // Act
        val result = myFeature.process(input)

        // Assert
        assertEquals(expected, result)
    }
}
```

## 📝 Pull Request Process

1. **Update your fork** with the latest changes from upstream:
   ```bash
   git fetch upstream
   git rebase upstream/master
   ```

2. **Make your changes** in your feature branch

3. **Write or update tests** for your changes

4. **Update documentation**:
   - Add/update KDoc comments
   - Update README.md if needed
   - Add entry to CHANGELOG.md under "Unreleased"

5. **Run tests and ensure they pass**:
   ```bash
   ./gradlew test
   ```

6. **Commit your changes** with a clear message:
   ```bash
   git commit -m "Add feature: description of your feature"
   ```

   Follow conventional commit format:
   - `feat:` for new features
   - `fix:` for bug fixes
   - `docs:` for documentation changes
   - `test:` for test additions/changes
   - `refactor:` for code refactoring
   - `chore:` for maintenance tasks

7. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

8. **Create a Pull Request** on GitHub:
   - Provide a clear title and description
   - Reference any related issues
   - Describe what testing you've done
   - Add screenshots/examples if applicable

### Pull Request Checklist

Before submitting, ensure:

- [ ] Code follows the project's style guidelines
- [ ] Tests are added and passing
- [ ] Documentation is updated
- [ ] CHANGELOG.md is updated
- [ ] No merge conflicts with master
- [ ] Commit messages are clear and descriptive
- [ ] All CI checks pass

## 🐛 Issue Reporting

### Before Creating an Issue

1. **Search existing issues** to avoid duplicates
2. **Check documentation** to ensure it's not a usage question
3. **Update to latest version** to see if the issue is already fixed

### Creating a Good Issue

Include the following information:

**For Bug Reports:**
- Clear, descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Environment details (OS, JDK version, Kotlin version)
- Code samples or logs
- Screenshots if applicable

**For Feature Requests:**
- Clear, descriptive title
- Problem statement: What problem does this solve?
- Proposed solution: How should it work?
- Alternatives considered
- Additional context

### Issue Labels

We use labels to categorize issues:

- `bug`: Something isn't working
- `enhancement`: New feature or improvement
- `documentation`: Documentation improvements
- `good first issue`: Good for newcomers
- `help wanted`: Extra attention needed
- `question`: Further information requested

## 🎯 Project Goals

When contributing, keep these project goals in mind:

1. **Simplicity**: Keep APIs simple and intuitive
2. **Extensibility**: Make it easy to add new LLMs and tools
3. **Performance**: Optimize for production use
4. **Reliability**: Handle errors gracefully with retries
5. **Documentation**: Maintain excellent documentation
6. **Testing**: Ensure high test coverage

## 💡 Getting Help

If you need help:

- Check the [README](README.md) and [examples](examples/)
- Ask questions in [GitHub Discussions](https://github.com/anoopg87/KAgentic/discussions)
- Open an issue with the `question` label
- Reach out to maintainers

## 🏆 Recognition

Contributors will be:

- Listed in the project's contributors
- Mentioned in release notes for significant contributions
- Thanked in the community

## 📄 License

By contributing to KAgentic, you agree that your contributions will be licensed under the Apache License 2.0.

---

Thank you for contributing to KAgentic! 🎉
