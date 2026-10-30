# Roadmap

This roadmap describes possible future improvements for the 16-bit CPU Simulator. Items are directional rather than commitments and may change as the project evolves.

## Completed

- 16-bit CPU execution model
- Two-pass assembler with labels and comments
- Arithmetic, logical, comparison, and branch instructions
- Register, memory, stack, and subroutine support
- Swing-based source editor and execution interface
- Sample assembly programs
- Automated tests and GitHub Actions validation
- Explicit validation for invalid memory addresses
- Clear GUI errors for file, assembly, execution, and step-limit failures
- GUI reset control for restarting execution without reopening the source file
- Configurable maximum execution-step limit
- In-app instruction-set reference
- Source-line breakpoint control
- Register and flag change highlighting
- Complete instruction and ALU edge-case test coverage
- Stack boundary and subroutine edge-case validation
- Contributor, support, security, and documentation guidance

## Near-term improvements

- Improve interactive lessons and beginner examples

## Future possibilities

- Add a formal instruction encoding or bytecode format
- Add export and import for simulator state
- Add more sample programs and interactive lessons
- Add a packaged desktop distribution
- Add broader cross-platform validation

## Contributing to the roadmap

Before implementing a larger roadmap item:

1. Search existing issues and pull requests.
2. Open an issue describing the problem and proposed behavior.
3. Discuss compatibility with the current assembler and CPU model.
4. Add tests and documentation with the implementation.

See the [development guide](development-guide.md) and [contributing guide](../CONTRIBUTING.md) for project conventions.
