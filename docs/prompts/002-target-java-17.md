# 002 — Target Java 17

Date: 2026-11-16

## Prompt

A small issue, java 21 only jre is installed but not jdk, java 17 is the latest available jdk

## Outcome

- Set `java.version` in `pom.xml` to 17. Spring Boot 4.1.2 still applies; it runs on Java 17.
- Documented in `README.md` that `JAVA_HOME` must stay on the Java 17 JDK. The Java 21 install is a JRE, so it cannot compile.
