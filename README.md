<div align="center">
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

<img src="https://github.com/quarkiverse/.github/blob/main/assets/images/quarkus.svg" width="67" height="70" ><img src="https://github.com/quarkiverse/quarkus-shedlock/blob/main/docs/modules/ROOT/assets/images/plus-sign.png" height="70" ><img src="https://github.com/quarkiverse/quarkus-shedlock/blob/main/docs/modules/ROOT/assets/images/shedlock.png" height="70" >

# Quarkus Shedlock
</div>
<br>

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.shedlock/quarkus-shedlock?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkiverse.shedlock/quarkus-shedlock-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellow.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
[![Build](https://github.com/quarkiverse/quarkus-shedlock/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-shedlock/actions/workflows/build.yml)


# Overview
ShedLock makes sure that your scheduled tasks are executed at most once at the same time. If a task is being executed on one node, it acquires a lock which prevents execution of the same task from another node (or thread). Please note, that if one task is already being executed on one node, execution on other nodes does not wait, it is simply skipped.


## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/dcdh"><img src="https://avatars.githubusercontent.com/u/5189615?v=4?s=100" width="100px;" alt="Damien Clément d'Huart"/><br /><sub><b>Damien Clément d'Huart</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-shedlock/commits?author=dcdh" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!