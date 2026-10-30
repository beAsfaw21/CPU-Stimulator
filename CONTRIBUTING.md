# Contributing to 16-bit CPU Simulator

Thank you for your interest in contributing to the 16-bit CPU Simulator. Contributions that improve correctness, documentation, usability, and educational value are welcome.

## Before you start

Please check the existing issues and pull requests before starting significant work. For large changes, open an issue first to discuss the proposed approach.

Examples of changes that benefit from discussion:

- Adding or changing an instruction
- Changing register or memory behavior
- Changing the assembler syntax
- Changing the stack or subroutine model
- Adding a new user interface feature

## Development requirements

- JDK 17 or newer
- Git
- A graphical desktop environment for running the Swing GUI

The project does not use Maven or Gradle. It has no external runtime dependencies.

## Set up the project

Clone the repository and enter its directory:

```bash
git clone https://github.com/beAsfaw21/CPU-Stimulator.git
cd CPU-Stimulator
```

Compile the source files:

```bash
mkdir out
javac -d out *.java
```

Run the test suite:

```bash
java -cp out simulator.TestRunner
```

Run the graphical simulator:

```bash
java -cp out simulator.GUI
```

## Create a branch

Use a descriptive branch name:

```bash
git checkout -b feature/add-instruction
```

Recommended prefixes include:

- `feature/` for new functionality
- `fix/` for bug fixes
- `docs/` for documentation changes
- `test/` for test-only changes
- `refactor/` for structural improvements

## Making changes

Please keep changes focused and follow the existing Java style.

When adding or changing CPU behavior:

1. Update the relevant implementation.
2. Add or update tests in the appropriate test class.
3. Update the README or other documentation when user-visible behavior changes.
4. Consider invalid input and boundary conditions.
5. Run the complete test suite before opening a pull request.

Examples of behavior that should include tests:

- New instructions or operand forms
- New branch conditions
- ALU flag behavior
- Memory and stack operations
- Label parsing and resolution
- Error handling for invalid assembly

Do not commit generated files such as:

- `.class` files
- `out/`
- `target/`
- IDE metadata

These files are excluded by `.gitignore`.

## Testing checklist

Before submitting a pull request, run:

```bash
git diff --check
javac -d out *.java
java -cp out simulator.TestRunner
```

The test runner should finish without failures:

```text
Ran 30 tests, failures: 0
```

If you change the GUI, also launch it and manually verify the affected workflow:

```bash
java -cp out simulator.GUI
```

## Commit messages

Use a short, descriptive commit message written in the imperative mood:

```text
Add relative jump instruction tests
Fix stack boundary validation
Update assembly language documentation
```

Keep unrelated changes in separate commits when practical.

## Pull requests

A pull request should:

- Explain what changed and why.
- Reference a related issue when one exists.
- Include tests for new or changed behavior.
- Describe how the changes were tested.
- Include documentation updates for user-visible changes.
- Avoid unrelated refactoring or generated files.

Before submitting, confirm:

- [ ] The project compiles with JDK 17 or newer.
- [ ] The test suite passes.
- [ ] `git diff --check` reports no whitespace errors.
- [ ] New behavior has appropriate tests.
- [ ] Documentation is updated where needed.
- [ ] No generated `.class` files or IDE files are included.

## Reporting bugs

When reporting a bug, include:

- A clear description of the expected and actual behavior
- The Java version and operating system
- The assembly source that reproduces the issue
- The command used to compile or run the project
- Relevant error messages or stack traces

Small, self-contained examples are especially helpful.

## License

By contributing to this project, you agree that your contributions will be licensed under the [MIT License](LICENSE).
