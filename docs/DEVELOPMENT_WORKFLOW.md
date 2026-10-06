# DEVELOPMENT WORKFLOW
<!-- This document details the step-by-step process for project initialization (Bootstrap) -->

## Project Initialization (Bootstrap)

### Phase 0 — STOP CODING
Strictly no business logic, endpoints, entities, functional components, or large refactorings.

### Phase 1 — DISCOVERY
Read all project artifacts:
- `docs/cahier-des-charges.md`
- `README.md`
- `docs/AGENTS.md`
- `docs/` folder
- Root configurations
- `backend/` and `frontend/` structures
- Dependency manifests
- Existing tests and CI/CD configs

Identify: Tech versions, architecture, dependencies, build/test commands, DB, Auth, API conventions, Angular architecture, design system.

### Phase 2 — REQUIREMENTS
Use **Requirements Engineer**.
Analyze: Personas, Use Cases, Functional/Non-functional requirements, Business rules, Security, Acceptance criteria, Edge cases, Assumptions, Risks.
*Never invent a business requirement.*

### Phase 3 — ARCHITECTURE
Use **Software Architect**.
Update `docs/ARCHITECTURE.md`.
Describe: System architecture, Backend/Frontend specifics, Database, Integrations, Auth/Authz, Deployment.
Critical decisions must be documented in `docs/adr/`.

### Phase 4 — DOCUMENTATION
Generate/Update the core project documents:
- `docs/PRD.md`
- `docs/ARCHITECTURE.md`
- `docs/PROJECT_RULES.md`
- `docs/API_CONTRACT.md`
- `docs/SECURITY.md`
- `docs/DESIGN_SYSTEM.md`
- `docs/DEVELOPMENT_WORKFLOW.md`
- `docs/IMPLEMENTATION_PLAN.md`

### Phase 5 — PROJECT RULES
Refine `docs/PROJECT_RULES.md` with specific conventions: Naming, packages, API, validation, errors, logging, tests, security, DB, Angular, Git.

### Phase 6 — API CONTRACT
Update `docs/API_CONTRACT.md`. OpenAPI is the source of truth. Every API change must be verified on both Spring Boot and Angular sides.

### Phase 7 — SECURITY
Update `docs/SECURITY.md`. Cover: Auth, RBAC/ABAC, Sensitive data, Secret management, OWASP, Uploads, Rate limiting, Audit.

### Phase 8 — DESIGN SYSTEM
Use **UI/UX Designer**. Update `docs/DESIGN_SYSTEM.md`: Info architecture, User journeys, Visual hierarchy, Typography, Spacing, Colors, Components, Accessibility.

### Phase 9 — IMPLEMENTATION PLAN
Generate `docs/IMPLEMENTATION_PLAN.md`. For each feature: Objective, Backend/Frontend/DB changes, API, Security, Tests, Acceptance criteria. Respect dependency order.

### Phase 10 — BOOTSTRAP REVIEW
Verify: Coherence, contradictions, Backend/Frontend contract, Architecture vs Requirements, Security assumptions.
Present: Discovered architecture, documents created, decisions, assumptions, open questions, and the implementation plan.
**STOP. No coding until explicit validation.**
