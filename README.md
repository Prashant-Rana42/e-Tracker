# e-Tracker
Developed a JavaFX-based inventory tracker and billing system using NetBeans. Designed a user-friendly interface for managing stock, tracking inventory, and generating bills. Implemented features like adding, updating, and removing items with efficient data handling.
Overview
The Billing System is a Java-based application designed to streamline billing operations. It features a user-friendly interface built using JavaFX and ensures efficient transaction management.

Build & Output Description
When you build the project, the IDE:

Copies all required JAR dependencies to the dist/lib folder.

Updates the MANIFEST.MF file to include necessary JARs in the Class-Path.

Distribution
To distribute the project, zip the dist folder (including the lib folder) and share it.

Annex
Build Process Notes
Only JAR files are copied to the lib folder; other files are ignored.

If multiple JAR files have the same name, only the first one is copied.

If a library in the classpath has a Class-Path entry in its manifest, ensure its dependencies are available in the runtime path.

To set the main class, navigate to:
Right-click Project → Properties → Run → Main Class
Alternatively, manually update the Main-Class entry in MANIFEST.MF.

License
This project is licensed under Apache 2.0.

