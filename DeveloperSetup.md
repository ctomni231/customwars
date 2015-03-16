There is a lot written here just to keep you right. It's really easy though.

**Checking out the code Win, Linux, Mac**

**Your Dev environment**
  * Eclipse: (Win, Linux, Mac)
  * - Downloading
  * - Workspace
  * - Plugins
  * - Importing
  * - Running
  * - Building


Custom wars is a Java swing application, kept under SVN version control, hosted by google hosting. In order to check out the latest version of the code you will need the following:

  * [Java Dev Kit 1.6.0 or higher](http://download.java.net/jdk6/)
  * An IDE: [eclipse(plain java edition)](http://www.eclipse.org/downloads/), [netbeans(plain java edition)](http://www.netbeans.org/downloads/), [intelliJ(paid)](http://www.jetbrains.com/idea/download/)
  * An SVN client: [tortoiseSVN](http://tortoisesvn.net/downloads), [versions(paid)](http://www.versionsapp.com/), [rapidSVN](http://www.rapidsvn.org/download/release/)
I'll help walk you through how to get these apart form java as you most likely have it already.

# Checking out the code #

### Windows ###

If you are on windows, download and install [tortoisesvn](http://tortoisesvn.net/downloads) . It will require a restart.

Using an svn client, anyone can get the code anonymously(no login required) via http. Alternatively commiters can checkout via the https address with their gmail account login and their google code password.

http://customwars.googlecode.com/svn/trunk/

Check out the code to somewhere on your local disc. If you are on windows be sure to check out the code to somewhere on C:/ as opposed to "Program Files" since this will have hidden space characters (%20) and just creates bother. I'd choose 'C:\repos\customwars\trunk'


### Linux: Ubuntu ###

If you are on Linux, type:
```
 sudo apt-get install subversion
```
Into the terminal. Once that is down:
```
sudo apt- get install rapidsvn
```
into the terminal.

Now select:
Applications > Programming > Rapid SVN

In rapid SVN:
  * Right click on Bookmarks
  * Checkout new working copy:
> _Anonomously_: http://customwars.googlecode.com/svn/trunk/ or
> _as commiter_: https://customwars.googlecode.com/svn/trunk/
  * Choose a destination Directory of: /home/(username)/Documents/repos/customwars/trunk
  * Make sure there are no spaces in the Directory path on _any_ OS as this will result in compile and/or run errors.

Now click ok on the dialog. If you are checking out as a commiter you will now have to enter your login details. Your login details are your gmail account name and your special generated google code password available here. If you are checking out anonymously then just sit back.


### Mac OSX ###
You're on a mac, as you are used to paying for good stuff. I recommend you use 'Versions'
Download and install versions:
  * Click 'Repository'
You can check it out anonomously by just adding:
  * Name: customwars
  * Location:  http://customwars.googlecode.com/svn/trunk/
  * Choose a destination Directory of: /home/(username)/Documents/repos/customwars/trunk

Or if you have commit rights
  * Username: (your gmail username)
  * Pass: (generated on google code)
  * Location: https://customwars.googlecode.com/svn/trunk/
  * Choose a destination Directory of: /home/(username)/Documents/repos/customwars/trunk

Now select the repo in the left hand bar and right click on the trunk that appears in the main window and choose 'checkout'.


# Your Dev environment #

# Eclipse (Windows, Mac, Linux) #

## 1. Eclipse: Installation ##

[Download the 'Eclipse IDE for Java Developers' from here](http://www.eclipse.org/downloads/)
  * Windows: Put it in 'C:\IDE\eclipse'
  * Linux: Put it in 'Documents\IDEs'
  * Mac: Put it in 'Documents\IDEs'

Double click the starter icon regardless of what platform you are on to start it.

## 1. Eclipse: Workspace ##

When loading into eclipse you will be asked to choose a workspace, create a workspace in a separate location to the location of your checked out code. This 'workspace' contains the metadata about your development set up so should be kept away from your actual projects.

## 2. Eclipse: Plugins ##
```

Help > Software updates
```
Click the avialable software tab and then "Add Site..." add the following URL with the name SVN plugins.
```
http://subclipse.tigris.org/update_1.4.x
```
Now tick the site you just entered form the choice of boxes and hit "Install..."
You will be asked if you want to install a whole range of stuff but all you really need is the plugins that have "core" in the name, they may appear under 1.2.x instead of 1.4.x, this is ok. Installing other things shouldn't hurt.

After these have downloaded you will be prompted to restart.


### 3. Eclipse: Importing the code into your workspace ###

You now need to import the code into your workspace. Please note that it is still separate from where the workspace is, you are just working on it via links. So if you delete things in here, it will not delete the real files without prompting you. Right click on the "Package explorer" on the left and then choose:
```
Import > existing projects into workspace
```

Browse to the root directory of the customwars project probably under trunk/customwars and a project should appear in the pane below saying "customwars". Do not click the copy into workspace box, just click "finish". The workspace is now building. Please wait for this to complete. You will see it's progress in the bottom right hand corner.After it has finished you should be able to see the names of those who last commited changes to the files reflected beside the filenames.


### 4. Eclipse: Running the application tests ###
Every time you check out the code the first thing you shoud do is run the tests. The tests are the only way we know that everything we expect to work is working the way we expect it!
```
Run > Run Configurations
```

Right click "Junit" and click the "new" icon.
In the panel that has now come up on the right choose customwars as the project. Now press browse and select "com.customwars.AllTests" as the Test class. Give this configuration the name of "custom wars AllTests " or something like that. Now click, "Apply" then "Run".
If everything is working then you should see a green bar!


### 5. Eclipse: Making a development build ###
To actually run the application do the following.

```
Run > Run Configurations
```

Right click Java Application and click the "new" icon.
In the panel that has now come up on the right choose customwars as the project. Now press browse and select "com.customwars.Main" as the main class. Give this configuration the name of "custom wars App" or something like that. Now click, "Apply" then "Run".
If you have followed the steps to the letter you should see the application running.


---


## Eclipse FAQ ##

It is also not recommended that you import the code directly from the repository into your workspace from within eclipse.

_I don't get names appearing next to my files_

Make sure you have installed the 'core' subverison 1.0.2 plugins plus the optionals in  1.0.4.

_Where do I get Java 6?_

It's java 1.0.6 and you can get it from: http://www.eclipse.org/downloads/