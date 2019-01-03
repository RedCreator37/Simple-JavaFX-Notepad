# Notepad ![][licenseBadge] ![][versionBadge]

#### This is almost the same as dev branch, except that HTMLEditor is used instead of TextArea

This is a simple JavaFX HTML notepad/editor app (JavaFX HTMLEditor wrapped in a nice window).

*This is the first program I put on GitHub.*
*I hope that you understand that some things aren't done perfectly.*
*PRs are welcome ;)*

## Features

HTML edition of Notepad:

- Edit HTML files
- Print HTML files
- WYSIWYG formatting
- Make the window semi-transparent...

## What's not working

These are the things that don't work as expected (yet):

- Printing on macOS (not sure about this one, it fails to open the print dialog for some reason)

## Requirements

Notepad is built on Java 11 and JavaFX.

You'll have to manually install JavaFX since it's no longer bundled with JDK.
Also, if you'd like to make an executable you'll have to either do a "fat jar" or make a modular project.

[licenseBadge]: https://img.shields.io/badge/license-MIT-blue.svg
[versionBadge]: https://img.shields.io/badge/version-0.2-brightgreen.svg