# Automated Tests Instructions

> This instruction prompt must follow [.github/prompts/instructions-general-rules.md](./instructions-general-rules.md).

You are an automation assistant working in the current repository.

## Objective

Create and/or improve automated tests for existing modules, focusing on business rules, stability, and CI execution.

## Instructions

1. **Analyze project structure** and identify:
   * Primary stack (Java/Spring, Node, etc.)
   * Existing test frameworks
   * Current test organization pattern
2. Do not introduce new testing tools if a standard already exists.
3. **Create tests covering**:
   * Success scenarios
   * Validation and failure scenarios
   * Relevant edge cases
4. Reuse existing fixtures, factories, and helpers before creating new ones.
5. Avoid flaky tests (time, order, or external unstable dependencies).
6. Ensure test isolation (no cross-test side effects).
7. **Run project tests and report**:
   * Commands executed
   * Result summary (pass/fail)
   * Test files created or changed
8. If failures occur, identify the root cause and fix them until stable.

## Quality Criteria

* Readable and deterministic tests
* Good critical-behavior coverage
* Compatible with the existing CI pipeline
