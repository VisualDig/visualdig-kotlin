# Virtual Dig - Feature Test Framework
Virtual Dig is a feature testing framework for Java and Kotlin users that
does not use Selenium as a web driver. It was written fully from scratch
in Kotlin, Elm, and JavaScript.


### Setup
Virtual Dig is currently not super user friendly to set up and import into
your project. This will change at a future date with the introduction of a
package at Maven Central.

For now, look at the feature-tests folder for information on how to use and
set up Virtual Dig. **feature-tests** is a fully set up sample project that uses
VirtualDig with Chrome.


### Design Approach
Many popular feature testing frameworks that are available today often take up
a lot of developers time due to poor error messages, and failures that are inconsistent.

Virtual Dig aims to reduce the amount of time that developers spend writing, diagnosing, 
and understanding feature tests. Reducing this time would make TDD teams significantly more
productive.

There are three major goals of the Virtual Dig framework:

- Reduce or eliminate false negatives
- Reduce the likelihood of needing to change a test when your site changes
- Better failure messages that help diagnose test issues quicker


