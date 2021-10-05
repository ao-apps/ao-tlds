# [<img src="ao-logo.png" alt="AO Logo" width="35" height="40">](https://github.com/ao-apps) [AO OSS](https://github.com/ao-apps/ao-oss) / [TLDs](https://github.com/ao-apps/ao-tlds)

[![project: current stable](https://oss.aoapps.com/ao-badges/project-current-stable.svg)](https://aoindustries.com/life-cycle#project-current-stable)
[![management: production](https://oss.aoapps.com/ao-badges/management-production.svg)](https://aoindustries.com/life-cycle#management-production)
[![packaging: active](https://oss.aoapps.com/ao-badges/packaging-active.svg)](https://aoindustries.com/life-cycle#packaging-active)  
[![java: &gt;= 8](https://oss.aoapps.com/ao-badges/java-8.svg)](https://docs.oracle.com/javase/8/docs/api/)
[![semantic versioning: 2.0.0](https://oss.aoapps.com/ao-badges/semver-2.0.0.svg)](http://semver.org/spec/v2.0.0.html)
[![license: LGPL v3](https://oss.aoapps.com/ao-badges/license-lgpl-3.0.svg)](https://www.gnu.org/licenses/lgpl-3.0)

[![Build](https://github.com/ao-apps/ao-tlds/workflows/Build/badge.svg?branch=master)](https://github.com/ao-apps/ao-tlds/actions?query=workflow%3ABuild)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.aoapps/ao-tlds/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aoapps/ao-tlds)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=master&project=com.aoapps%3Aao-tlds&metric=alert_status)](https://sonarcloud.io/dashboard?branch=master&id=com.aoapps%3Aao-tlds)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?branch=master&project=com.aoapps%3Aao-tlds&metric=ncloc)](https://sonarcloud.io/component_measures?branch=master&id=com.aoapps%3Aao-tlds&metric=ncloc)  
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?branch=master&project=com.aoapps%3Aao-tlds&metric=reliability_rating)](https://sonarcloud.io/component_measures?branch=master&id=com.aoapps%3Aao-tlds&metric=Reliability)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?branch=master&project=com.aoapps%3Aao-tlds&metric=security_rating)](https://sonarcloud.io/component_measures?branch=master&id=com.aoapps%3Aao-tlds&metric=Security)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?branch=master&project=com.aoapps%3Aao-tlds&metric=sqale_rating)](https://sonarcloud.io/component_measures?branch=master&id=com.aoapps%3Aao-tlds&metric=Maintainability)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?branch=master&project=com.aoapps%3Aao-tlds&metric=coverage)](https://sonarcloud.io/component_measures?branch=master&id=com.aoapps%3Aao-tlds&metric=Coverage)

Self-updating Java API to get top-level domains.

## Project Links
* [Project Home](https://oss.aoapps.com/tlds/)
* [Changelog](https://oss.aoapps.com/tlds/changelog)
* [API Docs](https://oss.aoapps.com/tlds/apidocs/)
* [Maven Central Repository](https://search.maven.org/artifact/com.aoapps/ao-tlds)
* [GitHub](https://github.com/ao-apps/ao-tlds)

## Features
* Background self-updating from [official iana.org source](https://data.iana.org/TLD/tlds-alpha-by-domain.txt).
* Stores updates in [Java Preferences API](https://docs.oracle.com/javase/7/docs/technotes/guides/preferences/) to reduce queries of data.iana.org.

## Motivation
[Top level domains](https://wikipedia.org/wiki/Top-level_domain) have become a moving target.  Any API that includes a hard-coded list, array, enumeration, or even an external flatfile of top level domains will require some routine maintenance to update the list.  We support many well-established and largely set-it-and-forget-it apps that routinely go long time periods without updates.  We prefer to write low maintenance code and existing solutions do not fulfill this requirement.

## Evaluated Alternatives
* [Apache Commons Validator](https://commons.apache.org/proper/commons-validator/apidocs/org/apache/commons/validator/routines/DomainValidator.html) - Includes a hard-coded list of TLDs.  Use of this API requires occasional updates of JARs and redeploys.
* [Google Guava InternetDomainName](http://google.github.io/guava/releases/5.0/api/docs/com/google/common/net/InternetDomainName.html) - Also includes a hard-coded list of TLDs in [com.google.common.net.TldPatterns](http://grepcode.com/file/repo1.maven.org/maven2/com.google.guava/guava/r06/com/google/common/net/TldPatterns.java).  Same issue with maintenance applies.

## Contact Us
For questions or support, please [contact us](https://aoindustries.com/contact):

Email: [support@aoindustries.com](mailto:support@aoindustries.com)  
Phone: [1-800-519-9541](tel:1-800-519-9541)  
Phone: [+1-251-607-9556](tel:+1-251-607-9556)  
Web: https://aoindustries.com/contact
