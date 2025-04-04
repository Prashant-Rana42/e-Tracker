# e-Tracker

## Major Project - MCA (Master of Computer Applications)
Developed a **JavaFX-based inventory tracker and billing system** using NetBeans. Designed a **user-friendly interface** for managing stock, tracking inventory, and generating bills. Implemented features like **adding, updating, and removing items** with efficient data handling.

## Overview
The **Billing System** is a Java-based application designed to streamline billing operations. It features a **user-friendly interface** built using **JavaFX** and **SQLite3**, ensuring efficient transaction management.

---
## **Build & Output Description**
When you build the project, the IDE:
- Copies all required **JAR dependencies** to the `dist/lib` folder.
- Updates the **MANIFEST.MF** file to include necessary JARs in the `Class-Path`.

### **Build Process Notes**
- Only **JAR files** are copied to the `lib` folder; other files are ignored.
- If multiple **JAR files** have the same name, only the first one is copied.
- If a library in the **classpath** has a `Class-Path` entry in its manifest, ensure its dependencies are available in the runtime path.
- To set the **main class**:
  - Navigate to: `Right-click Project → Properties → Run → Main Class`
  - Alternatively, manually update the **Main-Class** entry in `MANIFEST.MF`.

---
## **Distribution**
To distribute the project:
1. **Zip the `dist` folder** (including the `lib` folder).
2. **Share the zipped file** with users.

### **Custom JRE & Executable Notes**
- A **custom JRE** is included to allow the `.exe` file to run **without needing Java installed**.
- The **custom JRE** is provided as a **Self-Extracting Archive (SFXARC)** named `jre.exe`.
- To use it:
  1. **Double-click** `jre.exe` to extract the JRE.
  2. **DO NOT change the extraction path**.
  3. Once extracted, run your `.exe` file.
- For `.jar` users, **Java 17 or higher** must be installed on your system.

### **Tools Used**
- **Launch4j**: Converts `.jar` into `.exe`.
- **jdeps**: Checks dependencies.
- **jlink**: Creates the custom JRE.
- **config.xml**: Included for **Launch4j configuration**.

---
## **Database Information**
- **Database files** are stored in:
  ```
  C:\Users\[Your Username]\AppData\Local\AppUser
  ```

---
## **Technical Details**
- **JavaFX Version**: 21.0.5
- **JDK**: Bellsoft JDK 21
- **IDE**: NetBeans 23
- **Design Tool**: SceneBuilder
- **EXE Conversion**: Launch4j
- **Additional Dependencies**:
  - pdfbox-app-2.0.32.jar
  - sqlite-jdbc-3.47.0.0.jar
- **Helper Tools and Websites**:
  - ChatGPT
  - Stack Overflow
  - Flaticon
  - Paint.net
  - GIMP

---
## **License**
This project is licensed under **Apache 2.0**.

