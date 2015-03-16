# The Custom Wars source control explained #

Our Subversion repository is located at: http://code.google.com/p/customwars/
Subversion is a [version control](http://en.wikipedia.org/wiki/Version_control) program. Version control enables you to roll back the files contained within the repository to any moment in time within that repository.

This means that we have a complete history of all the code changes and versions of the project in one place.

## Our repository layout ##
Our repo is divided into 'trunk', 'tags' and 'branches'. Everyone will always work off of the 'trunk'. The 'trunk' is the main and only place where to commit your code. The 'tags' directory has a collection of green map versions, check out and build from these versions to reproduce a specific period in time of the green maps history.

## Best Practices ##
Have you passed a single unit test? Commit. Made one unit test pass that didn't? Commit. Changed README file ? Commit. Fixed a typo in some comments? Commit.

Breaking your work into small pieces is a fundamental idea in productivity. Programming has unit testing, project management has 'next actions' and so on - they're all about breaking big and complex things into small and simple ones. Regularly commits contribute to the community project, huge cumbersome commits that you have been working on for days is an annoyance for everyone.

When you commit to the repository **ALWAYS** write a commit message.
Yes, ALWAYS write a commit message. ALWAYS. ALWAYS. ALWAYS.
If you do not write a commit message, no one knows what you have done without scrutinising  and then understanding your code, this is a major productivity blow for those wanting to work on the code base. Please consider others and write a commit message you'd like to receive that details what you were doing. Raise an issue number on google code before you commit and include it in your commit message in the format [Issue #1](https://code.google.com/p/customwars/issues/detail?id=#1).

# Labels #
Every Issue that is posted should belong to either CW1 or CW2, By adding a label to each Issue we can quickly see an overview of all the issues for one version. Ie an overview of all CW2 Issues: http://code.google.com/p/customwars/issues/list?q=label:CW2

We use following labels:
CW1: Label for cw1
CW2: Label for cw2

if you are not familiar with these terms read the [glossary](http://www.customwars.com/wiki/index.php?title=Glossary)

# The trunk code works correctly #
The code in the trunk is guaranteed to always work. If you break it, you fix it, otherwise do not commit. Fixing other peoples changes is very time consuming so please never commit something you know not to work.