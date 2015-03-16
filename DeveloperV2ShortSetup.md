# A short guide to Checkout cw2 from google code #

#1 Join us on google code, you'll need a Google Account

#2 checkout the V2 directory using your svn client the svn url should start with
> httpS NOTE THE S AT THE END full url 'https://customwars.googlecode.com/svn/trunk/v2'

#3 provide google username, your personal password can be found here http://code.google.com/hosting/settings

#4 There will be many compile errors at this time fix that by adding each jar in the lib dir to the classpath.

#5 now the code should compile

#6 Make sure the **src** and **resources** packages are included in the classpath.(Blue color in intelliJ)

#7 An attempt to run the MainGame main method will result in following error message:
```
Exception in thread "main" java.lang.UnsatisfiedLinkError: no lwjgl in java.library.path
	at java.lang.ClassLoader.loadLibrary(ClassLoader.java:1682)
```
That's a good thing, everything compiles, only 1 more step.

#8 We are using native libraries and the virtual machine needs to know where to find them.
<br />Add the relative path
-Djava.library.path=lib/native/(your os) to the **vm parameters**. Replace your os with the directory of you os.

Examples:
  * -Djava.library.path=lib/native/**linux**
  * -Djava.library.path=lib/native/**windows**
  * -Djava.library.path=lib/native/**macosx**
  * -Djava.library.path=lib/native/**solaris**

You should now be greeted with a screen.

---

<p>If a resource could not be found Make sure that the compiler copies all resources to the output dir:<p />
For IntelliJ select File > Settings > Compiler. Add the following Resource patterns:<br />
<code>?*.properties;?*.xml;?*.gif;?*.png;?*.jpeg;?*.jpg;?*.html;?*.dtd;?*.tld;?*.ftl;?*.txt;?*.wav;?*.tga;?*.fnt;?*.ttf;?*.ogg;</code>