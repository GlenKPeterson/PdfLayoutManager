PdfLayoutManager
================

A page-layout manager for PDFBox to do things like line-breaking, page-breaking, and tables.  Uses a box-model for styles.  Someday, there may be a download site, but right now, you have to build the latest sources and javadocs with Maven 3.

To build (Jar file ends up in target/ sub-folder):

mvn clean install

Documentation is currently all in the javadocs.  Build them:

mvn javadoc:javadoc

View the generated docs in a web browser from this generated sub-folder:

target/site/apidocs/index.html

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
