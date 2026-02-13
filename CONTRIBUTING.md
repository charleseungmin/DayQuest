# DayQuest Development Workflow

## Branch Rule
- Base: master
- Feature: feature/<TaskID>-<short-slug>

Examples:
- feature/A-01-project-setup
- feature/C-01-generate-today-items

## Process
1. Pull latest master
2. Create feature branch
3. Implement one task (or a clear sub-task)
4. Open PR with template
5. Review + merge by maintainer
6. Delete branch

## Commit Convention
- feat(<TaskID>): ...
- fix(<TaskID>): ...
- refactor(<TaskID>): ...
- test(<TaskID>): ...
- chore(<TaskID>): ...
