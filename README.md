# VisualDig - Feature Test Framework
Visual Dig is a feature testing framework for Kotlin/Java that
does not use Selenium. The purpose here is to be as general as possible to
work directly in each browser with minimal external dependencies. It was written 
from scratch in Kotlin, Elm, and JavaScript.


## Mission
Many popular web feature testing frameworks that are available today often waste
development time due to poor error messages, and failures that are inconsistent.

Visual Dig aims to reduce the amount of time that developers spend writing, diagnosing, 
and understanding feature tests. Reducing this time would make TDD teams significantly
more productive.

There are three major goals of the Visual Dig framework:

- Reduce or eliminate false negatives
- Reduce the likelihood of needing to change a test when your app changes
- Better failure messages that help diagnose test issues quicker


## Setup
Visual Dig is currently not super user friendly to set up and import into
your project. This will change at a future date with the introduction of a
package at Maven Central.

For now, look at the feature-tests folder for information on how to use and
set up Visual Dig. **feature-tests** is a fully set up sample project that uses
VisualDig with Chrome.


# Testing in VisualDig

Visual Dig combines and uses APIs that were useful from Capybara, Selenium,
and Fluentlenium. It also introduces its own testing API in the way of spacial
assertions.

### Finding a Text Element
This is a simple test for finding text anywhere on the page.

```Kotlin

var element = dig.findText("foo")

```

If your test was to fail, it would be nice to know what element on the page was the
closest to your search term. Visual Dig determines closeness between 
your search term and what was actually found on the page, and displays this
when your test fails. 

The failure output for a **capitalization defect** would look like this:
```
Could not find the text 'foo' on page.


Expected element text:

    foo

Found:

    Foo

Suggestions:

    Did you possibly mean to search for 'Foo'?



	at io.visualdig.DigController.find(DigController.kt:107)
	at io.visualdig.Dig.findText(Dig.kt:31)
	at io.visualdig.featuretest.DigFeatureTests$1$3.invoke(DigFeatureTests.kt:29)
	at io.visualdig.featuretest.DigFeatureTests$1$3.invoke(DigFeatureTests.kt:13)
	...
```


### Searching Spatially
Sometimes, you want to be able to test and find elements that appear next to
another already found element. This is called a **spatial search**.

Visual Dig supports spatial searching of elements once you have found
an element (using `findtext()` for example).

Visual Dig uses cardinal direction when searching spatially. The API describes
right as east, left as west, above as north, and below as south.

```Kotlin

var element = dig.findText("foo")
searchEastOf(element).forCheckbox()

```

Spacial searches have many potential failures but the most common is an 
**alignment defect**. Put another way, the element you're looking for is in 
the direction you specified, but was not aligned within the specified tolerance.

See this example below of an alignment defect error output:
```
Could not find a checkbox element east of element A.

Expected: 

     ___      ___
    |   |    |   |
    | A |    | B |
    |___|    |___|


Found a checkbox B that wasn't aligned vertically:

              ___
             |   |
             | B |
             |___|
     ___
    |   |
    | A |
    |___|


Suggestions:

    - Is the element you're searching for visible?
    - Was your CSS changed to cause the element to be no longer
      aligned vertically?


Additional Info:

    A = Element with text 'foo'
    B = Checkbox with id: bar-checkbox

    B was vertically off of perfect alignment by 23 pixels, and
    it was expected to be less than 20 pixels off.



	at io.visualdig.DigController.find(DigController.kt:107)
	at io.visualdig.Dig.findText(Dig.kt:31)
	at io.visualdig.featuretest.DigFeatureTests$1$3.invoke(DigFeatureTests.kt:29)
	at io.visualdig.featuretest.DigFeatureTests$1$3.invoke(DigFeatureTests.kt:13)
	...
```

