<div align="center">

<img src="https://github.com/quarkiverse/.github/blob/main/assets/images/quarkus.svg" width="67" height="70" ><img src="https://github.com/quarkiverse/.github/blob/main/assets/images/plus-sign.svg" height="70" ><img src="https://github.com/quarkiverse/quarkus-shedlock/blob/main/docs/modules/ROOT/assets/images/shedlock.svg" height="70" >

# Quarkus Shedlock
</div>
<br>

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.shedlock/quarkus-shedlock?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkiverse.shedlock/quarkus-shedlock-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellow.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
[![Build](https://github.com/quarkiverse/quarkus-shedlock/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-shedlock/actions/workflows/build.yml)


# Overview
ShedLock makes sure that your scheduled tasks are executed at most once at the same time. If a task is being executed on one node, it acquires a lock which prevents execution of the same task from another node (or thread). Please note, that if one task is already being executed on one node, execution on other nodes does not wait, it is simply skipped.

