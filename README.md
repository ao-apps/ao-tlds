# [<img src="ao-logo.png" alt="AO Logo" width="35" height="40">](https://github.com/aoindustries) [AO TLDs](https://github.com/aoindustries/ao-tlds)
<p>
	<a href="https://aoindustries.com/life-cycle#project-current-stable">
		<img src="https://aoindustries.com/ao-badges/project-current-stable.svg" alt="project: current stable" />
	</a>
	<a href="https://aoindustries.com/life-cycle#management-production">
		<img src="https://aoindustries.com/ao-badges/management-production.svg" alt="management: production" />
	</a>
	<a href="https://aoindustries.com/life-cycle#packaging-active">
		<img src="https://aoindustries.com/ao-badges/packaging-active.svg" alt="packaging: active" />
	</a>
	<br />
	<a href="https://docs.oracle.com/javase/6/docs/api/">
		<img src="https://aoindustries.com/ao-badges/java-6.svg" alt="java: &gt;= 6" />
	</a>
	<a href="http://semver.org/spec/v2.0.0.html">
		<img src="https://aoindustries.com/ao-badges/semver-2.0.0.svg" alt="semantic versioning: 2.0.0" />
	</a>
	<a href="https://www.gnu.org/licenses/lgpl-3.0">
		<img src="https://aoindustries.com/ao-badges/license-lgpl-3.0.svg" alt="license: LGPL v3" />
	</a>
</p>

Self-updating Java API to get top-level domains.

## Project Links
* [Project Home](https://aoindustries.com/ao-tlds/)
* [Changelog](https://aoindustries.com/ao-tlds/changelog)
* [API Docs](https://aoindustries.com/ao-tlds/apidocs/)
* [Maven Central Repository](https://search.maven.org/#search%7Cgav%7C1%7Cg:%22com.aoindustries%22%20AND%20a:%22ao-tlds%22)
* [GitHub](https://github.com/aoindustries/ao-tlds)

## Features
* Background self-updating from [official iana.org source](https://data.iana.org/TLD/tlds-alpha-by-domain.txt).
* Stores updates in [Java Preferences API](http://docs.oracle.com/javase/6/docs/technotes/guides/preferences/) to reduce queries of data.iana.org.
* Small footprint, self-contained, no transitive dependencies - not part of a big monolithic package.

## Motivation
[Top level domains](https://en.wikipedia.org/wiki/Top-level_domain) have become a moving target.  Any API that includes a hard-coded list, array, enumeration, or even an external flatfile of top level domains will require some routine maintenance to update the list.  We support many well-established and largely set-it-and-forget-it apps that routinely go long time periods without updates.  We prefer to write low maintenance code and existing solutions do not fulfill this requirement.

## Evaluated Alternatives
* [Apache Commons Validator](https://commons.apache.org/proper/commons-validator/apidocs/org/apache/commons/validator/routines/DomainValidator.html) - Includes a hard-coded list of TLDs.  Use of this API requires occasional updates of JARs and redeploys.
* [Google Guava InternetDomainName](http://google.github.io/guava/releases/5.0/api/docs/com/google/common/net/InternetDomainName.html) - Also includes a hard-coded list of TLDs in [com.google.common.net.TldPatterns](http://grepcode.com/file/repo1.maven.org/maven2/com.google.guava/guava/r06/com/google/common/net/TldPatterns.java).  Same issue with maintenance applies.

## Contact Us
For questions or support, please [contact us](https://aoindustries.com/contact):

Email: [support@aoindustries.com](mailto:support@aoindustries.com)  
Phone: [1-800-519-9541](tel:1-800-519-9541)  
Phone: [+1-251-607-9556](tel:+1-251-607-9556)  
Web: https://aoindustries.com/contact
