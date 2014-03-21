Spring Security ACL in Google Cloud DataStore Demo
=

Requires [Apache Maven](http://maven.apache.org) 3.1 or greater, and JDK 7+ in order to run.

To build, run

    mvn package

Building will run the tests, but to explicitly run tests you can use the test target

    mvn test

To start the app, use the [App Engine Maven Plugin](http://code.google.com/p/appengine-maven-plugin/) that is already included and run

    mvn appengine:devserver

To see all the available goals for the App Engine plugin, run

    mvn help:describe -Dplugin=appengine
