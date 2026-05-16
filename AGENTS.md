<!-- gitnexus:start -->

# GitNexus — Code Intelligence

This project is indexed by GitNexus as **nrolube** (24054 symbols, 77112 relationships, 300 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Resources

| Resource                                 | Use for                                  |
| ---------------------------------------- | ---------------------------------------- |
| `gitnexus://repo/nrolube/context`        | Codebase overview, check index freshness |
| `gitnexus://repo/nrolube/clusters`       | All functional areas                     |
| `gitnexus://repo/nrolube/processes`      | All execution flows                      |
| `gitnexus://repo/nrolube/process/{name}` | Step-by-step execution trace             |

## CLI

| Task                                         | Read this skill file                                        |
| -------------------------------------------- | ----------------------------------------------------------- |
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus/gitnexus-exploring/SKILL.md`       |
| Blast radius / "What breaks if I change X?"  | `.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?"             | `.claude/skills/gitnexus/gitnexus-debugging/SKILL.md`       |
| Rename / extract / split / refactor          | `.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md`     |
| Tools, resources, schema reference           | `.claude/skills/gitnexus/gitnexus-guide/SKILL.md`           |
| Index, status, clean, wiki CLI commands      | `.claude/skills/gitnexus/gitnexus-cli/SKILL.md`             |

<!-- gitnexus:end -->

# NROLUBE Cursor Rules

## Goals

- Keep changes small, local, and reversible.
- Improve maintainability without large rewrites.
- Respect existing gameplay behavior and network protocol.

## Project Structure (Current Reality)

- This is a Java monolith game server organized mainly by domain packages:
  - `src/player`, `src/npc`, `src/boss`, `src/map`, `src/models`, `src/services`
  - data access in `src/jdbc` and `src/jdbc/daos`
  - transport/session in `src/network` and `src/server`
- Follow existing package boundaries unless explicitly asked to redesign architecture.

## Change Scope Rules

- Only modify files directly required for the task.
- Do not refactor unrelated legacy code in the same file.
- Preserve public behavior unless the task explicitly asks to change behavior.
- Prefer incremental cleanup in touched code paths over global cleanup.

## Data Access Rules

- New SQL must live in DAO/fetcher classes under `src/jdbc/daos` (or existing `jdbc` data-access layer).
- Service/controller/input classes must call DAO methods, not write inline SQL.
- If a query is needed for a feature/fix, add a focused DAO method and call it.
- Reuse existing DAO/fetcher methods before adding new ones.

## Service and Controller Rules

- `server`/`Controller` handles routing and protocol dispatch only.
- `services` and `services/func` hold business logic.
- Keep methods focused: parse/validate input -> call service/DAO -> send response.
- Avoid hidden side effects and duplicated business rules.

## Validation and Error Handling

- Validate external input before parse/convert (`int`, `long`, ids, amounts).
- Use utility validators from `Util` when available; add small reusable helpers there when needed.
- Never swallow exceptions silently in new code.
- When catching exceptions, return clear user-safe messages and log enough context for debugging.

## Clean Code Rules

- Use intention-revealing names (`accountId`, `cashToAdd`, `playerName`).
- Keep functions short and single-purpose.
- Avoid duplicated logic; extract small local/private methods first.

## Commenting Rules

- Write comments in Vietnamese (team's working language); identifiers (class/method/variable names) stay in English.
- Only add a comment when the logic is genuinely hard or a trade-off/constraint must be remembered. Self-explanatory code wins by default.
- Comments must explain **WHY** (rationale, consequence of not doing it this way), not **WHAT** (the code already shows the what).
- Do not narrate what a method/variable name already conveys — rename for clarity instead of adding a descriptive comment.
- Do not duplicate: if an outer block already explains the intent, do not repeat it on inner lines or nested catch blocks.
- Avoid JavaDoc on short/self-naming private methods; reserve JavaDoc for public/protected APIs or methods whose name cannot fully convey intent.
- Lean on identifier names to carry meaning: `catch (Exception ignored) {}` is enough on its own — no "ignore this" comment needed.
- Acceptable comments: legal/license headers, intent of complex regex/SQL, explicit trade-off warnings, TODOs with owner and reason.
- Do not delete existing WHY comments in legacy code just because they look unpolished — only remove if actually wrong or stale.

## Compatibility and Safety

- Do not change packet/message IDs, protocol formats, or command text flows unless requested.
- Keep DB schema assumptions unchanged unless task includes migration work.
- Avoid destructive operations and risky mass edits.

## Testing and Verification

- After edits, run targeted checks/tests relevant to changed files.
- Re-open changed code path mentally end-to-end: input -> business logic -> DAO -> response.
- Report limitations if full runtime validation is not possible in current session.

## Priority When Rules Conflict

1. Correctness and data safety
2. Backward compatibility
3. Maintainability and cleanliness
4. Micro-optimizations
