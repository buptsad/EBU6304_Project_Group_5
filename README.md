# AI-Empowered Personal Finance Tracker

## Overview

This is a Java-based personal finance tracker enhanced with AI features. It provides a modern UI, charting, and REST capabilities.

---

## Prerequisites

- **Java 17** or newer (JDK)
- **Gradle 8.8-8.14** (pure Gradle, no wrapper)
- **Git** (optional, for version control)

---

## Setup & Build

1. **Clone the repository** (if you haven't already):

    ```
    git clone https://github.com/buptsad/EBU6304_Project_Group_5
    cd EBU6304_Project_Group_5
    ```

2. **Build the project** (downloads dependencies and compiles code):

    ```
    gradle build
    ```

---

## Running the Application

You can run the application using Gradle:

```
gradle run
```

Or run the generated JAR file (after building):

```
java -jar Mini_project_test-1.0-SNAPSHOT.jar
```

For usage manual, see `user manual.docx`

---

## Javadoc Documentation

The Javadoc has already been generated and is included in the repository.

To view the documentation, open:  
`doc/index.html`

If you wish to regenerate the documentation:

```
gradle javadoc
```

---

## Testing
All the test codes lies in `src/test`, each in the same folder as the code to be tested in the main source code.

To run all tests:

```
gradle test
```

---

## Configuration

- The main class is: `com.example.app.Main`
- Dependencies are managed in build.gradle
- No additional configuration is required for basic usage.

---

## Troubleshooting

- Ensure your `JAVA_HOME` points to a compatible JDK (Java 17+).
- If you encounter issues with UI on headless systems, uncomment the `java.awt.headless` property in build.gradle.

---

## License

This project is part of academic coursework and is protected by the following license:

### Restricted Academic License

**Until June 30, 2025:**
All rights reserved. This software and associated documentation files may not be copied, modified, distributed, or used in any manner without explicit written permission from the authors.

**After June 30, 2025:**
This software will be released under the MIT License:

> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.