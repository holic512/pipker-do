# Backend Structure

<!-- ai-index: backend runtime config files use application example yml templates -->

## Runtime Config

- Shared config stays in `backend/src/main/resources/application.yml`.
- Local development should copy `application-dev.example.yml` to `application-dev.yml`.
- Production deployment should copy `application-prod.example.yml` to `application-prod.yml`.
- Real runtime config files are ignored by Git. The repository only tracks the corresponding `*.example.yml` templates.

- `common`: framework-level infrastructure only, such as unified response, config, exception handling, and request filters.
- `shared`: reusable cross-business capabilities.
- `shared/account`: user, VIP, profile, and account-facing controllers/services/mappers/entities.
- `shared/admin`: admin auth, admin profile, role, and project workspace capabilities.
- `shared/auth`: WeChat miniapp login and logout flow.
- `shared/security`: shared login context and Sa-Token kit wrappers.
- `shared/storage`: file upload and local file storage infrastructure.
- `biz`: business domains.
- `biz/kyzz`: current exam-prep business domain.

Rule:
- Anything reusable across `kyzz`, `kysx`, or later businesses should enter `shared`.
- Anything only serving one business should stay inside its own `biz/<domain>` tree.
