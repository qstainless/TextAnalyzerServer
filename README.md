# TextAnalyzer Server
Go to [TextAnalyzer Client](https://github.com/qstainless/TextAnalyzerClient).

## Overview

## What does this program do?

## Code design

## Test plans and standards

## System requirements
The program is a JavaFX application using version 8 of Amazon's distribution of the Open Java Development Kit (OpenJDK) [Corretto 8](https://aws.amazon.com/corretto/), which includes JavaFX 8. Unit tests were created using [Junit 5](https://github.com/junit-team/junit5/).

## Database connection defaults
The program assumes that an existing database user with all database privileges in the local MySQL database with username/password: textanalyzer/textanalyzer. It also assumes that it will connect to localhost using default port 3306. However, the user may change these initial configuration options by editing lines 32 to 35 in the `Database` class: 

```
/src/gce/textanalyzer/controller/Database.java

32    String databaseHost = "localhost";
33    String databasePort = "3306";
34    String databaseUser = "textanalyzer";
35    String databasePass = "textanalyzer";
```

## How to use this program.

## Installation.

## Known Issues
(See [Known Issues](https://github.com/qstainless/TextAnalyzerClient#known-issues) in TextAnalyzerClient).

Not all HTML files are created equal. The program's code to convert HTML files to plain text is still rudimentary, as it is unable to identify HTML tag properties in lines without opening or closing tags.

For example, in the following code:

```html
<div class="Popover anim-scale-in js-tagsearch-popover"
     hidden
     data-tagsearch-url="/qstainless/TextAnalyzer/find-symbols"
     data-tagsearch-ref="master"
     data-tagsearch-path="README.md"
     data-tagsearch-lang="Markdown">
</div>
```

The following will be considered as 'words' by the program because the lines do not begin with "<" nor end with ">."

```html
     hidden
     data-tagsearch-url="/qstainless/TextAnalyzer/find-symbols"
     data-tagsearch-ref="master"
     data-tagsearch-path="README.md"
```

That is because the program parses the target URL line by line. Lines that begin with "<" or end with ">" are ignored for purposes of counting words in them. 

## Todo
Refactor the `countWordFrequencies` method to detect lines that begin with "<" and ignore all following lines up to and including the next line with a closing ">." Because the parser will ignore the lines, there will be no need to call the `htmlToText` method.

## Version history
The version numbering of this project does not follow most version numbering guidelines. Instead, it is limited to a two-token concept:

```(major).(course module)``` 

```
Version 1.11 (current) - Converted program into server/client
Version 1.10 - Added database support
Version 1.9 - Added JavaDocs
Version 1.7 - Added unit tests using JUnit
Version 1.6 - Added GUI functionality
Version 1.2 - First version
```

## Screenshots

## Unit Tests
