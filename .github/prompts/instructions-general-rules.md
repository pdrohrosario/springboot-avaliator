# General Rules Instructions

You are a technical assistant working in this repository.

## Objective

Define global rules that ALL instruction prompts in `.github/prompts/` must follow.

## Mandatory Engineering Principles

### 1. Object-Oriented Design
* Prefer object-oriented modeling when it fits the codebase.
* Ensure encapsulation, high cohesion, and low coupling.
* Avoid classes with multiple unrelated responsibilities.

### 2. SOLID
* **S (Single Responsibility)**: Keep each class/module focused on one responsibility.
* **O (Open/Closed)**: Extend behavior without changing stable code when possible.
* **L (Liskov Substitution)**: Preserve substitution contracts between abstractions and implementations.
* **I (Interface Segregation)**: Prefer small, focused interfaces.
* **D (Dependency Inversion)**: Depend on abstractions, not concrete implementations.

### 3. DRY (Don't Repeat Yourself)
* Reuse existing code before adding new implementations.
* Extract shared helpers when duplication appears.
* Keep business rules centralized.

### 4. Design Patterns
* Apply patterns only when they improve clarity and maintainability.
* Prefer patterns already used in the repository.
* Do not force patterns for simple problems.

### 5. KISS (Keep It Simple, Stupid)
* Choose the simplest correct solution.
* Avoid unnecessary abstractions and accidental complexity.

### 6. No Premature Optimization
* Prioritize correctness, readability, and maintainability first.
* Optimize only with concrete evidence (measured bottleneck or explicit NFR).
* Document why optimization is needed.

## Additional Prompt Rules (Required)

### 7. YAGNI (You Aren't Gonna Need It)
* Do not implement speculative features.

### 8. Explicit Error Handling
* Do not hide failures.
* Surface clear errors and keep failure modes observable.

### 9. Scope and Safety
* Keep changes minimal, safe, and idempotent.
* Do not expand scope without explicit request.
* Do not run destructive actions without confirmation.

### 10. Verification and Traceability
* Run existing validation commands when applicable (tests/build/lint).
* Report what changed, why it changed, and how it was validated.

## Final Checklist

- [ ] Is the solution simple and correct?
- [ ] Is avoidable duplication removed?
- [ ] Are SOLID principles reasonably respected?
- [ ] Were patterns used only when justified?
- [ ] Was premature optimization avoided?
- [ ] Is error handling explicit and observable?
