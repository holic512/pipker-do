# Uni-App Structure

- `shared`: reusable frontend infrastructure.
- `shared/config`: environment and base endpoint configuration.
- `shared/network`: unified `uni.request` and upload wrapper.
- `shared/session`: token, current user cache, and bootstrap auth flow.
- `shared/api`: reusable account/auth/profile APIs.
- `shared/platform`: platform adapters such as WeChat nickname/avatar helpers.
- `pages/common`: cross-business pages such as Mine and Profile Edit.
- `pages/kyzz`: current business pages for study, question bank, practice, and exam.
- `components`: shared UI components.
- `components/kyzz`: business-specific components for the `kyzz` domain.

Rule:
- Shared capabilities go into `shared` or `pages/common`.
- Business-specific pages, APIs, and components should be grouped under their own domain directory.
