# Contribution Guide for the project

## Git Workflow
We use a simplified Git Flow:
- `main`: Production-ready code.
- `develop`: Integration branch for features.
- `feature/name`: Individual feature development.
- `fix/name`: Bug fixes.

### Commit Conventions
We follow Conventional Commits:
- `feat:` (new feature)
- `fix:` (bug fix)
- `docs:` (documentation changes)
- `style:` (formatting, missing semi-colons, etc; no code change)
- `refactor:` (refactoring production code)
- `test:` (adding missing tests, refactoring tests)
- `chore:` (updating grunt tasks etc; no production code change)

Example: `feat(auth): implement JWT token validation`

## Development Process
1. Create a `feature/` branch from `develop`.
2. Implement the change according to `docs/PROJECT_RULES.md`.
3. Write tests for the new functionality.
4. Create a Pull Request (PR) towards `develop`.
5. Ensure CI pipeline passes.
6. After review and approval, merge into `develop`.

## Definition of Done (DoD)
A task is considered done when:
- It fulfills the acceptance criteria in the PRD.
- It is covered by unit and integration tests.
- It follows the project's architectural principles (DDD-light).
- The API contract is updated and synchronized between backend and frontend.
- The code has been reviewed and approved.
