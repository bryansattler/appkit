
swt-appkit - easing development of swt-applications
==========================================

* API-Documentation: [JavaDoc](http://fab1an.github.com/appkit/javadoc/)
* Email: fabian.zeindl@gmail.com
* Latest build: [swt-appkit.jar](http://fab1an.github.com/appkit/swt-appkit.jar)
* Discussion: [appkit@googlegroups.com] (http://groups.google.com/group/appkit)
* SampleCode: [Sample.java](http://github.com/fab1an/appkit/blob/master/src/org/appkit/sample/Sample.java)
* License: Code is released under the LGPL2.

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

Thank you / Donations
------------------------------------

If you want you can [flattr](http://flattr.com/profile/cel1ne) me, or send a Paypal donation to the email above.

Features
-------------------------------

### Templating ###

Use a Json-File: [orderview.json](http://fab1an.github.com/appkit/orderview.json)     
<img src="http://fab1an.github.com/appkit/orderview-json.png" height="150px"/>    
to create Interfaces:    
<img src="http://fab1an.github.com/appkit/Orderview-Sample.png" height="150px"/>    

```java
Shell = new Shell();
shell.setLayout(new FillLayout());

/* create templating */
Templating templating = Templating.fromResources();

/* create the orderView component */
Component orderView = templating.create("orderview", shell);

/* select widgets and work with them */
Table t = orderView.select("orders", Table.class);
Label l = orderView.select("sidebar.stores", Label.class);
```

### EventHandling
Write less cluttered event-handling code using Guava's EventBus.

```java
public Sample() {
	/* …*/

	/* for catching all local events (see the methods tagged with @Subscribe) */
	LocalEventContext eventContext = new LocalEventContext(this);

	/* create the orderview component with the given eventContext */
	Component orderView = templating.create("orderview", eventContext, shell);
	this.compOrders = orderView.getComposite();

	/* … */
	shell.open();
}

@Subscribe
public void daterangeChange(final DateRange daterange) {
	L.debug("we got a date-range of our date picker: " + daterange);
}

```

### Registries for Colors, Fonts and Images ###
Automatic assigning, caching, disposing etc. etc.

```java
Button btw = new Button(parent, SWT.PUSH);
Fonts.set(btn, Fonts.BOLD);
Colors.setForeground(btn, 140, 120, 100); // RGB
Images.set(btn, ImageTypes.LOGO); // ImageTypes is an enum providing filenames
```

### User-preferences ###

```java
PrefStore prefStore = PrefStore.createJavaPrefStore("org/appkit/sample");
int option = prefStore.get("option_name", 2); // 2 is the default
```


### Overlays ###

TODO

### Measurements ### 
Do measurements of your code run-time.
```java
Measurement.Listener statistic = new SimpleStatistic();
Measurement.setListener(statistic);
/*…*/
Measurement.start(DEBUG_ENABLED, "expensive_op", data); // data can be added options
/* crunch crunch */
Measurement.stop();
/* … */
L.debug("Stats: {}", statistic.getResults());
```

### Utilties for SWT-Widgets ###
```java
/* six table-coumns equally amount available size */
TableUtils.fillTableWidth(table);
/* restore and save column-weights and order */
TableUtils.rememberColumnWeights(prefStore, executor, table, "MyTable");
TableUtils.rememberColumnOrder(prefStore, executor, table, "MyTable");
/* resize columns proportionally if table is resized */
TableUtils.autosizeColumns(t);
/* save and restore shell size, position and maximised state */
ShellUtils.rememberSizeAndPosition(prefStore, executor, shell, "My Shell", defWidth, defHeight, defX, defY);
```

### More

* better MessageBox, SearchForm, SaveFileDialog
* throttle expensive Runnables
* ScrollListener for Table
* ...

TODOs / Ideas
------------------------

> ### General
> * Help for adding (Win)Sparkle-Integration
> * Guava Caches for registries and Naming
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
