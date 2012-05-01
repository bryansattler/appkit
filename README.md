
swt-appkit - easing development of swt-applications
==========================================

* API-Documentation: [JavaDoc](http://fab1an.github.com/appkit/javadoc/)
* Email: fabian.zeindl@gmail.com
* Latest build: [swt-appkit.jar](http://fab1an.github.com/appkit/swt-appkit.jar)
* Discussion: [appkit@googlegroups.com] (http://groups.google.com/group/appkit)
* SampleCode: [Sample.java](http://github.com/fab1an/appkit/blob/master/src/org/appkit/sample/Sample.java)
There's a sample application in the source which shows some of the features.

Idea
-------

SWT-Appkit is a collection of utilities aiming to improve and help coding with SWT and
building better, more comprehensive, modern applications.
It's a kit, not a framework, in the sense that it tries not to force you to change your
application-structure. Instead you can gradually adapt your code to use parts and features,
which you might find useful.

The API was designed with the following principles:
* Almost all SWT-related calls have to be done in the Display-Thread, which enables you to use static methods more freely. 
* Heavy use of the Google Guava-toolkit, which enables you to write more modern and fun Java-code.
* Fail-fast and safe-guarding of IllegalArguments etc. by using Guava's Preconditions.
* Convention over Configuration

Dependencies
------------------------

* [gson-2.0](http://google-gson.googlecode.com) fast json parsing library
* [guava-11.0.2] (http://guava-libraries.googlecode.com/) google's core java-libraries
* [jna-3.4] (http://github.com/twall/jna) java native access, only necessary to get correct ApplicationData Folder on Windows (O_o)
* [slf4j-api-1.6.4](http://slf4j.org/) simple logging facade for java, provides pluggable logging (static binding, no need for configuration)
* [swt-3.7.1](http://www.eclipse.org/swt) SWT

License
-------------

Code is released under the LGPL.

Thank you / Donations
------------------------------------

If you want you can [flattr](http://flattr.com/profile/cel1ne) me, or send a Paypal donation to the email above.

Features / Overview
-------------------------------

### Templating ###

Use a Json-File: [orderview.json](http://fab1an.github.com/appkit/orderview.json)
![imgJson][]
to create Interfaces:
![imgUI][]

[imgUI]: http://fab1an.github.com/appkit/Orderview-Sample.png "Sample UI" height=100px
[imgJson]: http://fab1an.github.com/appkit/orderview-json.png "Sample UI" height=100px

```java
Shell = new Shell();
shell.setLayout(new FillLayout());

/* create templating */
Templating templating = Templating.fromResources();

/* create the orderView component */
Component orderView = templating.create("orderview", shell);
this.compOrders = orderview.getComposite();

/* select widgets and work with them */
Table t = orderView.select("orders", Table.class);
Label l = orderView.select("sidebar.stores", Label.class);
```

> ### EventHandling
> * Simple wrappers to write less cluttered event-handling code using Guava's EventBus

> ### Utilities
> * Registries for handling Colors, Fonts and Images
> * Store and load user-preferences
> * Throttle Runnables
> * Display overlays on Composites
> * Do measurements of your code run-time
> …

> ### Various widget utilities
> * Automatically resize table columns
> * save / restore column-order and weights
> * save / restore Shell position, maximised state etc.
> * ScrollListener for Table
> …

> ### Various useful widgets
> * better MessageBox
> * SearchFrom
> * better SaveFileDialog

TODOs / Ideas
------------------------

> ### General
> * Help for adding (Win)Sparkle-Integration
> * Unit tests
> * Help creating browser-based widgets (links and images are a problem)
> * Help integrating swing-widgets

> ### Templating
> * Different formats (YAML ?)
> * MigLayout?
> * LayoutUI that positions widget absolute
> * Editing Help: fast reloading of templates
> * Editing Help: activate all composite borders
> * Editing Help: gridlayout configuration
> * Editing Help: write back json to format it properly
	
> ### Measurement / Statistic 
> * output summary (longest running calls etc.)
> * wrapper that measures the length of EventHandlers

> ### Widgets
> * Table that shows results fast
> * typical "+","-" buttons for the mac
> * DebugViewer
> * LicenseViewer for linked opensource-libraries
> * ProgressBar with soft animation
