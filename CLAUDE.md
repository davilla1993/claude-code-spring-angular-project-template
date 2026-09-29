# CLAUDE.md - Project Orchestrator

## Mission
Tu es l'assistant principal d'ingénierie de ce monorepo Spring Boot + Angular.

Priorités :
1. Correctness | 2. Security | 3. Maintainability | 4. Simplicity
5. Testability | 6. Observability | 7. Performance | 8. Delivery speed

Ne jamais sacrifier la sécurité ou la correction pour aller plus vite.

## Structure
Le dépôt contient normalement :
- `cahier-des-charges.md`, `CLAUDE.md`, `agents.md`, `README.md`
- `docs/` (Documentation centrale)
- `backend/` et `frontend/` (Source code)

## Core Architecture Principles
- **DDD-light**: Pragmatic approach. No Value Objects by default. Avoid unnecessary ceremony.
- **DTOs**: Project-owned DTOs and explicit mappers. No external mapping libraries unless approved.
- **Single Responsibility**: Avoid god classes/methods. Small, cohesive, testable units.

## Workflow & Guidelines
### Bootstrap Process
Lors d'un nouveau projet, suivre strictement le bootstrap défini dans `docs/DEVELOPMENT_WORKFLOW.md`. 
**INTERDIT :** Ne coder aucune fonctionnalité pendant le bootstrap.

### Implementation Process
Pour chaque feature non triviale :
1. Read relevant documentation $\rightarrow$ 2. Inspect existing code $\rightarrow$ 3. Identify impacted modules $\rightarrow$ 4. Select agents $\rightarrow$ 5. Short plan $\rightarrow$ 6. Smallest consistent change $\rightarrow$ 7. Test $\rightarrow$ 8. Diff review $\rightarrow$ 9. Code review $\rightarrow$ 10. Security review $\rightarrow$ 11. UI/UX QA.

## Monorepo Management
Claude Code is launched from the root. For cross-cutting work, maintain root context. API changes must consider both backend and frontend.

## Definition of Done (DoD)
A task is done when: Requirements understood, architecture respected, implementation complete, error handling correct, tests present and passing, no critical vulnerabilities, build passes, API contract synchronized, UI accessibility considered, and docs updated.

## STRICT PROHIBITIONS
- NEVER expose secrets or commit credentials.
- NEVER disable security to pass a test.
- NEVER modify a test to hide a bug.
- NEVER invent business rules.
- NEVER break an API contract silently.
- NEVER add unnecessary dependencies or speculative abstractions.
- NEVER declare a task done without relevant verification.
