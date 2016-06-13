[![Release](https://jitpack.io/v/badoualy/stepper-indicator.svg)](https://jitpack.io/#badoualy/stepper-indicator)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-stepper--indicator-green.svg?style=true)](https://android-arsenal.com/details/1/3711)

# ![](https://github.com/badoualy/stepper-indicator/blob/master/sample/src/main/res/mipmap-mdpi/ic_launcher.png) Stepper indicator

<img src="https://github.com/badoualy/stepper-indicator/blob/master/ART/screen.gif" width="300">


Sample
----------------

You can checkout the [Sample Application](https://play.google.com/store/apps/details?id=com.badoualy.stepperindicator.sample) on the Play Store

Setup
----------------

First, add jitpack in your build.gradle at the end of repositories:
 ```gradle
repositories {
    // ...       
    maven { url "https://jitpack.io" }
}
```

Then, add the library dependency:
```gradle
compile 'com.github.badoualy:stepper-indicator:1.0.0'
```

Now go do some awesome stuff!

Usage
----------------
```xml
<com.badoualy.stepperindicator.StepperIndicator
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:stpi_stepCount="5"/>
```

Attributes:

| Name                   | Description                                         | Default value   |
|------------------------|-----------------------------------------------------|-----------------|
| stpi_stepCount         | number of pages/steps                               |                 |
| stpi_circleColor       | color of the stroke circle                          | #b3bdc2 (grey)  |
| stpi_circleRadius      | radius of the circle                                | 10dp            |
| stpi_circleStrokeWidth | width of circle's radius                            | 4dp             |
| stpi_indicatorColor    | color for the current page indicator                | #00b47c (green) |
| stpi_indicatorRadius   | radius for the circle of the current page indicator | 4dp             |
| stpi_lineColor         | color of the line between indicators                | #b3bdc2 (grey)  |
| stpi_lineDoneColor     | color of a line when step is done                   | #00b47c (green) |
| stpi_lineStrokeWidth   | width of the line stroke                            | 2dp             |
| stpi_lineMargin        | margin at each side of the line                     | 5dp             |

```java
indicator.setViewPager(pager);
// or keep last page as "end page"
indicator.setViewPager(pager, pager.getAdapter().getCount() - 1); //
// or manual change
indicator.setStepCount(3);
indicator.setCurrentStep(2);
```
