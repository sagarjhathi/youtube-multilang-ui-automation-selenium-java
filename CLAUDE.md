# CLAUDE.md

Project rules for every AI session in this repository (Java + Selenium + TestNG + Maven). This file is tracked in git, so a fresh clone starts with the rules. Facts were checked on 2026-11-22 against the repo, `git log`, `mvn -q test-compile` (exit 0) and the offline suite (23 run, 0 failed, 0 skipped). Anything not run or read is marked UNVERIFIED.

## 0. Start-of-session habit

1. Read this file.
2. Read `docs/STATUS.md` and `docs/LESSONS.md`. Read `docs/DECISIONS.md` when you are about to change how something works.
3. `docs/` is git-ignored (local notes only). If it is missing on this machine, say so and do not invent history; work from this file, the code and `git log`.
4. Run `git status -sb` and `git log --oneline -5` yourself. Do NOT trust a status snapshot given at the start of a conversation; it can be stale.

## 1. Project purpose

- **What it is:** end-to-end tests that switch the YouTube interface language (and region) again and again on the **live** youtube.com, signed out, in a real browser, and check that the UI shows the right text for each one. Nothing is mocked.
- **Who it is for:** the owner (GitHub `sagarjhathi`), as a portfolio and learning project. It is an educational test project, not a scraper or a load tool. It uses the public site the way a visitor does. UNVERIFIED: the owner has not written a terms-of-use position; the project keeps runs small (see rule 3 and section 7).
- **End goal:** a suite whose failures are real. YouTube changes menus without notice, so the suite tolerates known harmless variations (accepted side-menu texts), retries one bad item without hiding the rest (`LoopGuard`), proves it can fail (canary), and says clearly when a CI run is incomplete (the shard check and the lost-tests check in the `publish` job).
- **Sister project (other stack):** TypeScript + Playwright, at `C:\Sagar\youtube-multi-language-ui-validator-automation-play`, GitHub `sagarjhathi/youtube-multilang-ui-automation-playwright-ts`. **This repo's sessions never edit it.** Read-only is fine. Changes go there through a written prompt (skill `port-to-ts-prompt`).
- **This repo on GitHub:** `sagarjhathi/youtube-multilang-ui-automation-selenium-java`, remote `origin`.
- **Live report hub:** https://sagarjhathi.github.io/youtube-multilang-ui-automation-selenium-java/ (HTTP 200 checked 2026-11-22; the workflow builds this URL at `master.yml` line 431, the README does not state it). Canary runs publish under `canary/`.

## 2. Tech stack

| Item | Value | Source |
|---|---|---|
| Language | Java, `<release>21</release>` | `pom.xml` (maven-compiler-plugin 3.13.0) |
| JDK used on the owner's PC / CI | 24 (`java -version` of the PC: Oracle 24.0.2; CI `JAVA_VERSION: 24`, temurin) | `mvn -v`, `master.yml` |
| Build | Maven 3.9.11 (PC) | `mvn -v` |
| Browser driver | Selenium 4.27.0 | `pom.xml` |
| Test framework | TestNG 7.10.2, Surefire 3.5.2 | `pom.xml` |
| Language detection | Lingua 1.2.2 | `pom.xml` |
| Excel data | Apache POI 5.4.1 | `pom.xml` |
| Reporting | ExtentReports 5.1.2, Allure `allure.version` 2.29.0 (allure-testng 0.4.21 UNVERIFIED) | `pom.xml` |
| Screenshots | Thumbnailator (JPEG, quality `imageCompressionQuality`) | `ImageCompressor`, `masterdata.properties` |
| Logging | Log4j 2.20.0, one log file per test | `pom.xml`, `log4j2.xml` |
| CI | GitHub Actions, workflow `CI Pipeline`: checkout@v7, setup-java@v6, upload-artifact@v7, download-artifact@v8, setup-node@v7 (all on `windows-latest`; the last job on `ubuntu-latest`). Whether each is the current major is UNVERIFIED | `.github/workflows/master.yml` |
| Hosting of reports | GitHub Pages from the `gh-pages` branch (force-pushed as one orphan commit on every publish) | `master.yml` |
| Notifications | Discord webhook, secret `DISCORD_WEBHOOK` | `master.yml` |
| License | see `LICENSE` | repo |

## 3. Folder map

```
CLAUDE.md                          this file (tracked)
readme.md                          public description, data files, canary, CI explanation
.github/workflows/master.yml       CI: offline-checks -> test (4 shards) -> publish (+ notify-offline-failure)
.github/pages/index.html           landing page (template) of the report hub
docs/                              GIT-IGNORED local notes: STATUS, DECISIONS, LESSONS and 6 older notes
.claude/                           GIT-IGNORED local: settings.json, skills, agents
yt_multi_language_ui_validator/    the Maven project (run every mvn command from here)
  pom.xml
  src/main/java/yt_multi_lang_ui_validator/      package main.java.yt_multi_lang_ui_validator.*
    base/ BasePage, BaseTest
    config/ ConfigManager           key lookup: -D system property, then environment, then masterdata.properties
    driverManager/ DriverManager    builds Chrome / Firefox / Edge (and Grid) with the args from masterdata.properties
    pages/ YtLandingPage            the page object
    safeActions/ SafeActions        click/find with waits, retries (element.retry.attempts), fallback
    utilities/ LoopGuard, Canary, GenericUtility, WaitUtility, ScreenshotUtilUpdated, ImageCompressor
    lingua/ LinguaHelper            language detection
    fileReader/ FileReader, DataSheets, SheetLookup   Excel reading; rows found by NAME
    reporting/ ReportManager, TestListenerUpdated, Attachers
    logger/, pathManager/, seleniumGrid/
  src/main/resources/data/          7 Excel sheets: LanguagesList, countryCodes, expectedSideMenuExpanded,
                                    expectedSideMenuCollapsed, expectedSearchFilter,
                                    ApplicableLanguageExpectedLanguage, ApplicableLanguageExpectedAttibute
  src/main/test/java/tests/         YtMasterTests (9 LIVE tests), AcceptedSideMenuTextsTest, CanaryTest, DataIntegrityTest (23 OFFLINE tests)
  src/main/test/java/retry/         RetryFailedTest, RetryListener
  src/main/test/java/runners/       testng.xml (live, parallel methods, 3 threads), lingua.xml, settings-options.xml, offline.xml
  src/test/resources/masterdata.properties          run settings
```

Note the odd layout: tests live under `src/main/test/java`, not `src/test/java`; only `masterdata.properties` is under `src/test/resources`.

**Test counts** (from `@Test` annotations and the offline run, 2026-11-22): 9 live tests in `YtMasterTests` (the country-code test is two methods, one per half of the countries); 23 offline tests = `AcceptedSideMenuTextsTest` 13 + `CanaryTest` 4 + `DataIntegrityTest` 6. The offline run reported 23 run, 0 failed, 0 skipped.

**Git-ignored** (`.gitignore`): `docs/`, `.claude/`, `CLAUDE.local.md`, `.env`, `*.pem`, `*.key`, `target/`, `run_*/`, `test-output/`, `allure-results/`, `allure-report/`, `logs/`, `*.log`, `.metadata/`, `.vscode/`, `bin/`. **Tracked:** everything else, including `CLAUDE.md`. Only `CLAUDE.md` of the agent setup files is tracked; the rest are local and ignored.

## 4. Commands

Run from `yt_multi_language_ui_validator\` (where `pom.xml` is). Status: **verified** = run on 2026-11-22; otherwise UNVERIFIED today.

| Purpose | Command | Status |
|---|---|---|
| Compile check | `mvn -q test-compile` | **verified**, exit 0 |
| Offline suite (the fast check, no browser) | `mvn test "-Dsurefire.suiteXmlFiles=src/main/test/java/runners/offline.xml"` | **verified**: 23 run, 0 failed, 0 skipped, BUILD SUCCESS (prints log4j `FileAppender` ERROR lines because the log path is empty offline; the tests still pass) |
| Lint / format | none; the repo has no lint tool | verified by reading `pom.xml` |
| One live group (4 shards in CI) | `mvn test -Dgroups=side-menu-expanded` (or `side-menu-collapsed`, `settings-region`, `global-filter`) | UNVERIFIED today; **LIVE, ask first** |
| By detector type | `mvn test -Dgroups=lingua` / `-Dgroups=non-lingua` | README, UNVERIFIED today; **LIVE, ask first** |
| Small live run | add `-DrunForAllLanguages=false -DlanguageCount=5 -DlocationCount=5` (also `-DrunForAllLocations=false`) | README; **LIVE, ask first** |
| Whole live suite | `mvn clean test` | **LIVE**; never run it to "check" |
| Canary (every test must FAIL) | PowerShell `$env:CANARY = "true"; mvn test -DrunForAllLanguages=false -DlanguageCount=5 -DlocationCount=5`; or tick `canary` in Actions > CI Pipeline > Run workflow. Keep to 5 languages (README "Canary Mode") | README, UNVERIFIED today; **LIVE, ask first** |
| Reports | Extent: `run_<timestamp>/reports/ExtentReport.html`; Allure raw results: `target/allure-results/` (Allure CLI is on the owner's PC; command UNVERIFIED) | README |
| Install | none beyond Maven downloading dependencies | |

Machine notes: Windows; no `python`, `jq` or `gh` on the owner's PC; use `curl` and Maven. The public GitHub API is rate-limited (about 60 calls an hour without a token); call it once and save the result. Job logs need a login. Bash tool = Git Bash (`VAR=x cmd`); PowerShell tool needs `$env:VAR='x'; cmd`.

**Settings, variables and defaults** (read from `masterdata.properties`, `ConfigManager`, `GenericUtility`):

| Name | Default | Effect |
|---|---|---|
| Lookup order | `-Dkey=value`, then environment variable (key upper-cased, `.` becomes `_`), then `masterdata.properties` | `ConfigManager.get` |
| `CANARY` (env or key) | false | one expected value per test is spoiled; every test must fail; the test retry is off. The environment variable wins over the key |
| `IS_CRON` (env) | unset | `true` only for the nightly `schedule` run (set in `master.yml` line 119); selects the `...CI` keys |
| `runForAllLanguages` / `languageCount` | false / 10 | a local, push, PR and manual run checks the first 10 languages. The count is used only when the all-flag is false |
| `runForAllLocations` / `locationCount` | false / 10 | same for locations |
| `runForAllLanguagesCI` / `runForAllLocationsCI` | true / true | nightly: everything. `languageCountCI` and `locationCountCI` (both 10) are therefore unused while their all-flags are true |
| `skipFlakyLanguageDetection` | true | skip the Lingua assertion for the flaky languages; the `<html lang>` check still runs |
| `browser` | chrome | `chrome.headless=true`; Firefox and Edge keys exist; Grid via `*.runOnGrid` + `gridHubUrl` |
| `explicit.wait` / `element.retry.attempts` | 10 s / 3 | waits and `SafeActions` attempts |
| `page.load.timeout` / `script.timeout` | 120 s / 60 s | hung page load or script |
| `compressImage` / `imageCompressionQuality` | true / 0.60 | screenshots saved as `.jpg` when true, `.png` otherwise |
| Test retry | one retry, never after an `AssertionError`, off in canary | fixed in `RetryFailedTest`, not read from the properties file |

## 5. Conventions

- **Layers:** tests -> page object (`pages/YtLandingPage`) -> `SafeActions` -> utilities (`utilities/`, `WaitUtility`) -> reporting (`TestListenerUpdated`, `Attachers`, Log4j). Each layer calls only the one below. Tests do not call raw Selenium actions that `SafeActions` covers.
- **Naming:** live tests `verifying...` (for example `verifyingSearchFilterText`); offline tests are in `AcceptedSideMenuTextsTest`, `CanaryTest`, `DataIntegrityTest`. Test method names are read by other code (the `publish` job's lost-tests check): search the whole repo before renaming one. UNVERIFIED which other files depend on them.
- **Style:** tab-indented Java, no formatter or linter is configured; do not add one unasked. Comments explain WHY. The owner wants changes kept simple, with easy to understand syntax.
- **Test patterns:**
  - One test goes through all languages or locations. Use `SoftAssert` inside the loop (a wrong value is reported at the end and the loop continues) and a hard assert only outside loops (`softAssert.assertAll()` after the loop).
  - Each item runs inside `LoopGuard` (`passed()`, `retryAfterFailure()`, `shouldStop()`): one retry per item; stop after 3 environment failures or 5 other failures in a row.
  - Loops start at row 0 of the language list. Rows are found by NAME (`DataSheets.…get(langText)` through `SheetLookup`); a duplicate name stops at load, and a missing name or column throws an error that names the file, sheet and column.
  - Missing data or a missing page option throws; it never silently passes. Known exception: `YtMasterTests` `checkCountryCodes` (line ~574) returns early, with a log line, when its half has no locations (only with a very small `locationCount`). It is logged, not silent, but it checks nothing: see docs/STATUS.md known issues.
  - Texts are compared after normalization (`GenericUtility.normalizeText`). The expanded side menu accepts several versions of its text (see glossary: accepted side-menu texts).
- **Git:** branches on `origin`: `master` (used), `canary`, `improve`, `cleanup-and-timeouts`, `cleanup/premium-config-surefire`, `ci/gate-on-offline-checks`, `ci/node24-actions`; `gh-pages` holds the published reports (never touch by hand). **The owner makes every commit and push.** No commit-message format or branching convention is defined here. This clone does not fetch `gh-pages` (refspec `^refs/heads/gh-pages`, set 2026-11-22) because it is about 135 MB per fetch.
- **CI:** workflow `CI Pipeline`. Jobs: `offline-checks` (no browser; if it fails nothing else runs) -> `test` (matrix of 4 groups, `fail-fast: false`, 60 min limit) -> `publish` (merges reports, adds problems to the hub page and run summary, deploys to `gh-pages`, stays green even when shards failed) plus `notify-offline-failure`. Each shard ends with a step that fails the shard when the Maven step did not succeed or no tests ran (`master.yml` around lines 205-226). Triggers: push and pull_request on every branch, manual run with switches `canary`, `check_failed_tests`, `check_lost_shard`, and nightly cron `43 3 * * *` UTC (09:13 AM IST). `publish` has its own lock `gh-pages-publish`; pull requests build reports but never publish. Discord messages (secret `DISCORD_WEBHOOK`): each shard start, the publish result, offline-check failure; all `continue-on-error: true`.

## 6. Rules for agents

1. Read docs/STATUS.md and docs/LESSONS.md at the start of every session (section 0).
2. Never guess. If unsure, search the code, run it, or ask. A command, path, number, version or date must come from something you read or ran.
3. Run code and tests before claiming something works, and report the real output. Offline checks are cheap; a live run loads the real site: ask the owner first, then run one case only.
4. Verify version-sensitive facts against official docs and cite them. If you did not look it up in this session, mark it UNVERIFIED.
5. Make focused changes. Do not touch unrelated files, do not reformat, do not refactor on the side.
6. Ask before deleting files, changing configs, changing workflows, or anything hard to reverse or outward-facing (publishing, force-pushing, secrets, notifications). Approval for one thing does not extend to the next.
7. Never run git commit or git push (settings deny them). Make the edit, show the diff or git status, let the owner commit. Read-only git is fine.
8. Report what a CI run really did. A green run with skipped tests checked nothing. Always give passed / failed / skipped / broken counts and the skip reasons (skill assess-ci-run).
9. Do not name a cause you did not see. Say "UNVERIFIED" or "likely", and say what you could not read (CI job logs need a login; the gh CLI is not installed).
10. Deferred work goes in docs/STATUS.md, Backlog section, with the date.
11. Porting: the other project is a separate repo and stack. Do not edit it from here. Write a prompt for its own session in ITS terms (skill port-to-ts-prompt); never call files by this stack's names, never assume a file exists there.
12. At the end of a session: update docs/STATUS.md (done, next step, known issues), add decisions to docs/DECISIONS.md and new lessons to docs/LESSONS.md, then tell the owner the changes are ready to review and commit.

## 7. Do-not list

- **Never evade the live site's bot detection.** No stealth settings, user-agent or fingerprint changes, proxies, challenge solving or human-like input tricks. Handling is: click the site's own "continue" button, reload once, otherwise skip with the reason. The browser argument `--disable-blink-features=AutomationControlled` (it hid the automation flag) was removed from `DriverManager` and `masterdata.properties` on the owner's decision (2026-11-22); do not add it back or anything like it. UNVERIFIED: no live run was done after the removal, so whether YouTube behaves differently is not known yet.
- No loops, big parallel batches or data collection with live runs. One case, after asking.
- A skip is not a pass, and a green workflow is not proof of testing. (The Maven test step has `continue-on-error: true` on purpose so the upload steps run; the later step "Fail the shard if tests failed or none ran" makes the shard red. Do not remove that step.)
- Never leave a test that could not check something by a silent early exit or a bare `return`: throw an error that names the problem, or skip with a reason.
- Never write `${{ github.* }}` (or `matrix`, `needs`, `job` values) inside a workflow `run:` script; pass it through `env:` (a branch name can contain `$(...)`). The three Discord steps already follow this (values in `env:`, checked 2026-11-22); a new use in a `run:` script is a finding.
- No secrets or webhook URLs in any file or log. Never print the `DISCORD_WEBHOOK` value.
- No "TEMPORARY" triggers or temp files left on master.
- Do not use `secrets.*` in a step `if:`; check for an empty value inside the script. Keep `continue-on-error: true` on Discord steps. Use the POSIX zone `TZ='IST-5:30'`, not `Asia/Kolkata`, in scripts that may run in Git Bash on Windows (the three Discord steps use `IST-5:30` since 2026-11-22).
- Do not leave `CANARY=true` in `masterdata.properties`.
- Never run two Maven runs in this folder at the same time (they share `target/` and the `run_*` folder), and after stopping a run check that its processes are really gone: `Get-CimInstance Win32_Process | Where-Object { $_.Name -match 'java|chrome|chromedriver' }`; stop only processes you started (`taskkill /PID <pid> /T /F`). Live runs are limited to a few languages unless the owner asks for more: `-DrunForAllLanguages=false -DlanguageCount=5 -DlocationCount=5`.
- Do not widen a comparison so far that a wrong text can pass; every widening needs a negative test.
- Do not rely on `.gitignore` to untrack a file: a tracked file stays tracked (`git ls-files`, `git check-ignore -v` on the REAL file).
- Do not rename packages, remove dependencies or edit `pom.xml`, the workflow or `masterdata.properties` as a side effect of another task.
- Do not let the README drift: update it in the same session as the code.
- No `git push --force`, `git reset --hard` or branch deletion unasked. (The CI itself force-pushes `gh-pages`; that is the publish design, not something to copy.)
- Do not modify the TypeScript repo from here.

## 8. Glossary

| Term | Meaning |
|---|---|
| Lingua | the language-detection library; some languages are flaky, so detection is skipped for them (`skipFlakyLanguageDetection`) and `<html lang>` is still checked |
| accepted side-menu texts | the set of side-menu wordings the suite accepts per language, kept in `expectedSideMenuExpanded.xlsx` and checked offline by `AcceptedSideMenuTextsTest` |
| `{slot}` / `{premium}` | placeholders in the base text of `expectedSideMenuExpanded.xlsx` (columns 2 and 3 hold their options separated by `||`, `(none)` = entry missing): `{slot}` is the Live / Hype entry, `{premium}` is "YouTube Premium" or a promo like "Try Premium for $0" (read from `AcceptedSideMenuTextsTest` and `GenericUtility.loadAcceptedSideMenuTexts`) |
| SafeActions | click/find helpers with waits, retries and fallback |
| LoopGuard | per-item retry and stop rules for a language/location loop |
| canary | a run where one expected value per test is spoiled on purpose; every test must fail |
| shard | one of 4 parallel CI jobs, one per TestNG group |
| lost-tests check | `publish` step: all 4 shards sent a report and every test in `YtMasterTests` has a result; shown as a red box on the hub page; never blocks the publish |
| report hub | the GitHub Pages landing page (`.github/pages/index.html`) with links to the reports |
| offline checks | the 3 test classes in `offline.xml` and the CI job that runs them first |
| health-check switches | manual inputs `check_failed_tests`, `check_lost_shard` that fail things on purpose to prove the gates |
| CI keys | `masterdata.properties` keys ending in `CI`, used by the nightly run (`IS_CRON=true`) |

## 9. Pointers

- Local notes: `docs/STATUS.md` (where we are), `docs/DECISIONS.md` (why), `docs/LESSONS.md` (mistakes). Older notes in `docs/` (`how-it-works.md`, `run-coverage.md`, `ci-failure-visibility.md`, ...) are history and may be out of date. The code and this file win.
- Skills (`.claude/skills/`, local): `assess-ci-run`, `merge-readiness`, `port-to-ts-prompt`, `update-allowed-wording`.
- Agents (`.claude/agents/`, local): `code-reviewer` (read-only), `test-runner`.
- Settings: `.claude/settings.json` (local): allows the compile and offline commands and read-only git, asks before live runs and installs, denies commit, push and reading `.env`.
- External docs (given as pointers, not looked up this session): Selenium https://www.selenium.dev/documentation/, TestNG https://testng.org/, Maven Surefire https://maven.apache.org/surefire/maven-surefire-plugin/, GitHub Actions https://docs.github.com/actions.

## 10. Ongoing habit

- **Start of each session:** section 0.
- **End of each session:** update `docs/STATUS.md` (done, next step, known issues), add decisions to `docs/DECISIONS.md`, add lessons to `docs/LESSONS.md`, then tell the owner the changes are ready to review and commit. Never commit yourself.
- Personal notes that must not be committed go in `CLAUDE.local.md` (git-ignored).
