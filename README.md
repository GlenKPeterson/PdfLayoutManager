PdfLayoutManager
================

A page-layout manager for PDFBox to do things like line-breaking, page-breaking, and tables.  Uses a box-model for styles.  Requires PDFBox which in turn requires Log4J or apache commons Logging.

Building from Source
====================
Requires Maven 3 and Java JDK 1.5 or greater.  Jar file ends up in the target/ sub-folder.

API documentation can be built with `mvn javadoc:javadoc` and is then found at `target/site/apidocs/index.html`

A jar file (and separate sample PDF) can be built with `mvn clean install` and ends up in the `target/` sub-folder.

Recent Changes
==============
Version 0.1.1: Added run-time check for appropriate data types used to create a Cell to provide fail-fast behavior for client code - if you pass an invalid object type, it throws an exception immediately instead of waiting to purge the cache.

Version 0.1: Initial working and documented version

License
=======
Copyright 2013 PlanBase Inc. & Glen Peterson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
