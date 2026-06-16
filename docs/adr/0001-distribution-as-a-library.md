# 0001. Distribution as a library

- **Status:** Accepted
- **Date:** 2026-06-16

## Context
pdf-toolkit is a reusable library consumed by other projects, not a runnable application. It needs Spring Boot's curated dependency versions (spring-*, thymeleaf, freemarker, junit-jupiter, assertj, slf4j) without inheriting the app-oriented build that `spring-boot-starter-parent` imposes (repackaging, fat-jar layout, opinionated plugin config). It also has to publish cleanly through JitPack, whose build environment runs an older Maven that rejects newer plugin versions and defaults to Java 8.

This is the shared distribution convention across the calcifux navajas (auth-toolkit, mailable-toolkit, the remote-* libraries), kept identical so each toolkit builds and publishes the same way.

## Decision
- **No `<parent>`.** The parent `pom.xml` (`com.github.calcifux:pdf-toolkit-parent`) imports `org.springframework.boot:spring-boot-dependencies` (3.2.5) as a BOM under `<dependencyManagement>` for managed versions, instead of inheriting `spring-boot-starter-parent`. Versions not in the BOM are pinned explicitly (`pebble` 3.2.2, `openhtmltopdf` 1.0.10, `lombok` 1.18.36).
- **Single `${revision}` version + flatten.** All modules share one `${revision}` (currently `0.1.0`); `flatten-maven-plugin` (1.5.0) with `flattenMode=resolveCiFriendliesOnly` rewrites published POMs at `process-resources` so they contain no `${revision}` variable.
- **Multi-module reactor.** Six modules — `pdf-toolkit-core`, `-pebble`, `-thymeleaf`, `-freemarker`, `-openhtmltopdf`, `-spring` — split so the core stays framework- and library-agnostic and consumers pull only the adapters they want.
- **JitPack publishing.** `groupId com.github.calcifux`, JDK 21. `jitpack.yml` forces `openjdk21` and builds `mvn clean install -DskipTests`. Build plugins are pinned to JitPack-friendly versions (`maven-compiler-plugin` 3.11.0, `maven-surefire-plugin` 3.1.2).
- **Deliberately minimal build.** No SonarCloud, no JaCoCo — the toolkit stays "basic and fast" to build; tests run in CI/local, JitPack skips them.
- MIT licensed, © Carlos Guillermo Reyes Ramiro.

## Consequences
- Consumers get Boot-aligned transitive versions without the starter-parent's app build behavior.
- Published POMs are self-contained (no unresolved `${revision}`), so JitPack and downstream Maven resolve them correctly.
- One version bump (`<revision>`) releases the whole reactor consistently.
- Trade-off: pinned-version dependencies (Pebble, OpenHTMLtoPDF) and JitPack-capped plugin versions must be bumped by hand; they are not BOM-managed.
- Trade-off: dropping Sonar/JaCoCo means no automated coverage/quality gate — a conscious choice for build speed and simplicity.
