Generic Study Capture Framework
====

For instructions to set up a development environment, see http://phenotypefoundation.github.com/GSCF

To set up a production environment, see [INSTALLATION.md](INSTALLATION.md).

Or for the really impatient, download Java 1.6 or higher, Grails 1.3.9 and use (UNIX syntax):
```
export JAVA_HOME=...
export GRAILS_HOME=...
export PATH=$JAVA_HOME/bin:$GRAILS_HOME/bin
export JAVA_OPTS="-Xms32m -Xmx512m -XX:MaxPermSize=750m -XX:MaxHeapFreeRatio=70 -XX:MaxGCPauseMillis=10 -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled"
grails run-app
```