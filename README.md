\# CorePLM — Product Lifecycle Management Platform



A lightweight, from-scratch PLM (Product Lifecycle Management) backend built with Spring Boot, inspired by enterprise systems like Siemens Teamcenter — implementing real engineering change control concepts: versioned item revisions, Bill of Materials with cycle detection, and a two-stage change approval workflow (ECR/ECO).



\*\*Frontend repo:\*\* \[coreplm-frontend](https://github.com/Malar99/coreplm-frontend)



\## Why this project exists



Most portfolio projects are CRUD apps with extra steps. This one implements the actual structural concepts that make PLM systems different from a generic database-backed API — the separation between an Item's permanent identity and its versioned content, cycle-safe recursive product structures, and formal change control gating what can and can't be released.



\## Tech Stack



\- Java 21, Spring Boot 4.1.0

\- Spring Security + JWT (stateless authentication)

\- Spring Data JPA / Hibernate

\- MySQL 8

\- Maven

\- Docker \& Docker Compose



\## Architecture Highlights



\- \*\*Item / Item Revision split\*\* — an Item is permanent identity (auto-generated part number); an ItemRevision is versioned, eventually-immutable content. BOM lines and change requests reference specific revisions, preserving historical accuracy even as a part evolves.

\- \*\*Recursive Bill of Materials\*\* — multi-level product structures with depth-first cycle detection, preventing a component from directly or transitively containing itself at any nesting depth.

\- \*\*Two-stage Change Control (ECR/ECO)\*\* — Engineering Change Requests require review and approval by a different user than the submitter (separation of duties, enforced server-side), and only an approved, open Engineering Change Order authorizes a revision's release.

\- \*\*Role-based access control\*\* — many-to-many User↔Role model, method-level (`@PreAuthorize`) and path-level authorization, with registration defaulting to a minimal role to prevent privilege escalation.

\- \*\*State machines\*\* — Revision and Change Request lifecycles enforced via explicit transition tables, rejecting invalid status jumps.



\## Running the Project



\### Option 1: Docker Compose (recommended)



Requires Docker Desktop with virtualization enabled (WSL2 on Windows).



&#x20;   cd coreplm

&#x20;   docker-compose up --build



This starts MySQL, the backend (localhost:8080), and the frontend (localhost:3000) together.



First-time setup — the database starts empty; roles must be seeded once. Connect to the containerized MySQL at 127.0.0.1:3307 (not the default 3306, to avoid conflicting with any local MySQL installation) and run:



&#x20;   INSERT INTO roles (name, description) VALUES

&#x20;   ('ADMIN', 'Full system access'),

&#x20;   ('ENGINEER', 'Can create and modify items'),

&#x20;   ('VIEWER', 'Read-only access');



Then register a user via POST /api/v1/users, and promote them to ADMIN via direct SQL:



&#x20;   INSERT INTO user\_roles (user\_id, role\_id)

&#x20;   SELECT u.id, r.id FROM users u, roles r

&#x20;   WHERE u.username = 'YOUR\_USERNAME' AND r.name = 'ADMIN'

&#x20;   ON DUPLICATE KEY UPDATE user\_id = user\_id;



\### Option 2: Run locally without Docker



Requires Java 21, Maven, and a local MySQL 8 instance.



&#x20;   cd coreplm

&#x20;   mvn spring-boot:run



Configure src/main/resources/application-dev.yml with your local MySQL credentials.



\## API Overview



| Area | Endpoints |

|---|---|

| Auth | POST /api/v1/auth/login |

| Users | POST/GET/PUT/DELETE /api/v1/users |

| Items | POST/GET/PUT/DELETE /api/v1/items |

| Revisions | POST/GET/PATCH /api/v1/items/{itemId}/revisions, /api/v1/revisions/{id} |

| BOM | POST/GET/DELETE /api/v1/revisions/{revisionId}/bom |

| Change Requests | POST/GET/PATCH /api/v1/change-requests |



\## Known Limitations



\- No automated role-seeding on first startup — currently a manual one-time SQL step (see Setup above).

\- Access tokens only, no refresh token flow (by design, for now).

\- No document/file attachment management.

\- Fine-grained per-object access control (beyond role-based) not implemented.



\## Project Structure



Layered architecture: controller → service → repository → entity, with DTOs at every API boundary, centralized exception handling, and JPA auditing on every entity.

