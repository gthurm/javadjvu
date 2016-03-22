JavaDjVu - Release 0.8.09

This package contains the source code and documentation for JavaDjVu.  JavaDjVu
consists of a reference library and GUI code that may be used to view and
navigate DjVu documents from JVM 1.1 compliant virtual machines.

BUILD REQUIREMENTS:

	1. JDK 1.5.0 or later.
	2. GNU bash, version 2.05b or later. (for command line builds)
	3. NetBeans 4.1 or later.  (for GUI builds)


RUNTIME REQUIREMENTS:

	1. One of J2SE Runtime Environment 1.1.4, J2ME 2.0, or newer releases.
           JIT highly recommended.
	2. At least 64MB of memory.
	3. A java enabled browser such as Internet Explorer,
	   Mozilla Firefox 1.0, or Netfront 3.1.

NON-SUPPORTED RUNTIME ENVIRONMENTS:

	If you are not lucky enough to be using the Sun JVM, you can try
	a Java 1.1 compatible virtual machines.  Visit:
		http://www.gnu.org/software/classpath/stories.html#jvm
	for a list of virtual machines to try.

INVOKING THE VIEWER:

        Primarily, the viewer is intended to be used as an applet.  The instructions in
        Deployment.html will guide you deploying the applet on your website.  There is 
        limited support for running the viewer as a standalone application.  To do so, you 
        must have Java 1.3 or later installed and in your search path.  Assuming both
        javadjvu.jar and foo.djvu are in the current directory, the following command 
        line would be used to view foo.djvu:

    	        java -jar javadjvu.jar foo.djvu

        Actually any valid URL may be used.  For example, when debugging the following 
        commandline is invoked:

	        java -jar javadjvu.jar http://javadjvu.sourceforge.net/examples/

        Note that the web browser built into Java does not support current standards.  
        Consiquently, few websites can actually be browsed successfully with the viewer.

BUILD INSTRUCTIONS:

	If you have NetBeans, you may load javadjvu as a NetBeans project
	and build, debug, and test from there.  Otherwise you may install and configure
	GNU bash with your JDK path and execute the following command line:

		./build.sh

	The resulting files will be populated into the build directory.  You
	may test your build with the command:

		cd example
		java -jar ../build/javadjvu.jar

	This should open a standalone application browsing the document in the
        examples directory.  You are now ready to copy the DjVuApplet.class
	and javadjvu.jar file to your website.


DEPLOYMENT INSTRUCTIONS:

	Basic instructions for deploying a document are listed in the
	Deployment.html file.

ADVANCED BUILD INSTRUCTIONS:

	JavaDjVu contains the optional components:

		applet, menu, toolbar, text, anno, outline, and frame

	Each of these components adds to the functionality of the build, at
	the cost of a larger file size and a longer download time.

	The default is to build all optional components but you may 
	do a custom build with just the features you wish to use by
	listing only the features you want on the build.sh command line.
	For example, if you wanted to build an applet with only menu
	navigation, and no support for hidden text or annotations you 
	would use the command:

		./build.sh applet menu

	If you are using NetBeans to build the applet, then comment out
	the properties in the build.properties file for the optional components
	you do not want to include.  For example to disable the menu
	option you would change the line:

        addon.menu=com/lizardtech/*/menu/*.java

	to:

        # addon.menu=com/lizardtech/*/menu/*.java

	The optional components are further described below:

	applet: (adds approximately 137 bytes to the build)

		Used to build the DjVuApplet.class.  This is required for
		using the <APPLET> tag from HTML documents.

	menu: (adds approximately 5036 bytes to the build)

		Used to add a pop-up menu to the applet.  When the user clicks
		on the right mouse button, or any mouse button with a CTRL key
		the pop-up menu will appear.  This will allow users to navigate
		multi-page documents, zoom, and access the AboutURL and HelpURL.
		This option does not work for most J2ME devices.

        keys: (adds approximately 1528 bytes to the build)

		Used to add keyboard shortcuts to the applet.  The user will be
                able to navigate the document using the keyboard.  This option does 
                not work for most J2ME devices.

	toolbar: (adds approximately 34012 bytes to the build)

		Used to enable a toolbar at the top of each page.  This toolbar
		may be used for navigating multi-page documents, zoom, and
		perform text search functions if "text" has also been
		selected.

	text: (adds approximately 9988 bytes to the build)

		Used to enable underlying support for the hidden text layer.
		This enables decoding of TXTa and TXTz chunks in the document.
		If combined with the toolbar option, users will be able to
		search and view hidden text.

	anno: (adds approximately 23358 bytes to the build)

		Used to enable hyperlink annotations.  This will enable
		decoding of ANTa and ANTz chunks.  Users will be able to
		click on hyperlinks and follow links inside the DjVu document.

	outline: (adds approximately 8709 bytes to the build)

		Used to enable outline navigation.  This option enables the
		decoding of the "NAVM" chunks, and using the ouline to 
		navigate multi-page DjVu documents.



