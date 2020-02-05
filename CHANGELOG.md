# Changelog

## [Unreleased]

## [2.2]

### Removed
 - [Remove hardcoded value for hibernate.implicit_naming_strategy](https://github.com/premium-minds/pm-persistence-utils/pull/27)
 
### Changed 
 - [Bump easymock from 4.1 to 4.2](https://github.com/premium-minds/pm-persistence-utils/pull/26)
 - Bump hibernate.version from 5.3.5.Final to 5.4.10.Final
 - [Bump maven-source-plugin from 3.2.0 to 3.2.1](https://github.com/premium-minds/pm-persistence-utils/pull/23)
 - [Bump maven-source-plugin from 2.2.1 to 3.2.0](https://github.com/premium-minds/pm-persistence-utils/pull/11)
 - [Bump junit from 4.12 to 4.13](https://github.com/premium-minds/pm-persistence-utils/pull/24)
 - [Bump slf4j-api from 1.7.29 to 1.7.30](https://github.com/premium-minds/pm-persistence-utils/pull/22)
 - [Bump slf4j-api from 1.7.25 to 1.7.29](https://github.com/premium-minds/pm-persistence-utils/pull/20)
 - [Bump maven-javadoc-plugin from 2.9.1 to 3.1.1](https://github.com/premium-minds/pm-persistence-utils/pull/21)
 - Bump H2 to 1.4.200
 - [Bump nexus-staging-maven-plugin from 1.6.2 to 1.6.8](https://github.com/premium-minds/pm-persistence-utils/pull/15)
 - [Bump maven-scm-provider-gitexe from 1.8.1 to 1.11.2](https://github.com/premium-minds/pm-persistence-utils/pull/17)
 - [Bump maven-release-plugin from 2.4.2 to 2.5.3](https://github.com/premium-minds/pm-persistence-utils/pull/16)
 - [Bump guice-persist from 4.2.0 to 4.2.2](https://github.com/premium-minds/pm-persistence-utils/pull/19)
 - [Bump maven-scm-publish-plugin from 1.0-beta-2 to 3.0.0](https://github.com/premium-minds/pm-persistence-utils/pull/10)
 - [Bump maven-compiler-plugin from 2.3.2 to 3.8.1](https://github.com/premium-minds/pm-persistence-utils/pull/12)
 - [Bump maven-gpg-plugin from 1.5 to 1.6](https://github.com/premium-minds/pm-persistence-utils/pull/13)
 - [Bump easymock from 3.6 to 4.1](https://github.com/premium-minds/pm-persistence-utils/pull/14)

### Added 

 - Added maven-scm-api
 
## [2.1]

### Fixed
 - [start() returns "transaction already started" everytime after an exception](https://github.com/premium-minds/pm-persistence-utils/pull/9)
 
## [2.0]

### Changed

 - [Migration to Hibernate 5.3.5](https://github.com/premium-minds/pm-persistence-utils/pull/7)
 
## [1.2]

### Changed

 - [increase (provided) guice version from 3.0 to 4.1.0](https://github.com/premium-minds/pm-persistence-utils/pull/6) 
 
### Fixed

 - [MultiplePersistenceTransaction receiving any exceptions in first inner transaction prevent any further transactions being opened/closed](https://github.com/premium-minds/pm-persistence-utils/pull/5)
 - [Postgres service shutdown at Transaction End](https://github.com/premium-minds/pm-persistence-utils/pull/4)
 - [O hibernateEnversDDL usa agora as configuracoes passadas como parametros no SchemaUpdate](https://github.com/premium-minds/pm-persistence-utils/pull/3)
 
## [1.1]

### Added 

 - [Utility Class](https://github.com/premium-minds/pm-persistence-utils/pull/1)

### Changed

 - [upgarde Hibernate version from 4.1.7.Final to 4.3.10.Final](https://github.com/premium-minds/pm-persistence-utils/pull/2)

[unreleased]: https://github.com/premium-minds/pm-persistence-utils/compare/v2.1...HEAD
[2.1]: https://github.com/premium-minds/pm-persistence-utils/compare/v2.1...v2.2
[2.1]: https://github.com/premium-minds/pm-persistence-utils/compare/v2.0...v2.1
[2.0]: https://github.com/premium-minds/pm-persistence-utils/compare/v1.2...v2.0
[1.2]: https://github.com/premium-minds/pm-persistence-utils/compare/v1.1...v1.2
[1.1]: https://github.com/premium-minds/pm-persistence-utils/compare/v1.0...v1.1