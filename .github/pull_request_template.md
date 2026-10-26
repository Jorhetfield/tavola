## What changes
<!-- One or two sentences: what this PR does and why. Link the docs/PLAN.md item if it applies. -->

## Type of change
- [ ] Feature
- [ ] Improvement / refactor
- [ ] Fix
- [ ] Docs / build / CI

## How it was done
<!-- Relevant decisions, discarded alternatives and modules touched. -->

## Screenshots / video
<!-- Required for UI changes: light, dark and large text. Before / after for design changes. -->

## Checks
- [ ] `./gradlew spotlessCheck`
- [ ] `./gradlew assembleDebug`
- [ ] `./gradlew testDebugUnitTest :domain:test :core:testing:test`
- [ ] `./gradlew lintDebug`
- [ ] Tested manually on an emulator or device (say which)

## Project rules (CLAUDE.md)
- [ ] No implicit `it` in lambdas; no fully qualified names in code bodies (imports at the top)
- [ ] No hardcoded colors, dimensions or strings (theme tokens and `stringResource`, es and en)
- [ ] Animations use `MaterialTheme.motionScheme` and clickable elements give touch feedback
- [ ] No business logic in composables; ViewModels, UseCases and repositories have tests
- [ ] Module dependencies respected (`:domain` stays pure, features never depend on each other)
- [ ] Reference screenshots regenerated and reviewed if the UI changes (`updateDebugScreenshotTestDefaultTestSuite`)
- [ ] `docs/PLAN.md` updated if an item is completed
- [ ] Everything written in the repo (docs, templates, comments, commits, PRs) is in English

## Notes for reviewers
<!-- Risks, doubtful parts, remaining debt, work left for other PRs. -->
