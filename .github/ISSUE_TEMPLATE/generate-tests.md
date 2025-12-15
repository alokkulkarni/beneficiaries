---
name: Generate tests with Copilot Agent
about: Trigger Copilot Agent to analyze branch and add missing tests
title: "Generate tests for branch <branch-name> (beneficiaries)"
labels: [tests, copilot-agent]
assignees: []
---

## Branch
<branch-name>

## Changed Java sources (list paths)
```
src/main/java/.../Foo.java
src/main/java/.../Bar.java
```

## Frameworks detected (optional)
JUnit 5, Spring Boot Test, Mockito, Cucumber JVM, Testcontainers

## Requirements
- Prefer existing frameworks found in the repo (JUnit 5, Spring Boot Test, Mockito, Cucumber JVM).
- If absent, use JUnit 5 + Mockito + Spring Boot Test; BDD with Cucumber; integration via Testcontainers.
- Generate unit tests, Spring slice tests (@WebMvcTest, @DataJpaTest as applicable), BDD scenarios, and Testcontainers-backed integration tests for repositories/messaging.
- Keep changes minimal and focused on tests.

#github-pull-request_copilot-coding-agent
