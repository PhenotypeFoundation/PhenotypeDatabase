// see https://github.com/jkuehn/gorm-mongodb
// see https://github.com/jkuehn/gorm-mongodb/blob/master/scripts/_Events.groovy
// see https://github.com/jkuehn/gorm-mongodb/blob/master/src/groovy/grails/plugins/mongodb/ast/MongoDomainASTTransformation.groovy
// see http://code.google.com/p/burningimage/source/browse/trunk/src/java/pl/burningice/plugins/image/ast/DBImageContainerTransformation.java?r=77
// see http://svn.codehaus.org/grails-plugins/grails-burning-image/trunk/src/java/pl/burningice/plugins/image/ast/AbstractImageContainerTransformation.java
// see http://www.breskeby.com/2010/06/write-a-custom-caching-ast-transformation-with-groovy/

// as of 20130118 we have removed the AST transformations
// see ticket https://github.com/PhenotypeFoundation/GSCF/issues/64

//import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
//
//includeTargets << grailsScript("_GrailsClean")
//
////def mongoAstPath = "${gormMongodbPluginDir}/src/groovy/grails/plugins/mongodb/ast"
////def mongoAstDest = "${projectWorkDir}/ast/gorm-mongodb"
//def gdtAstPath = "${gdtPluginDir}/src/groovy/org/dbnp/gdt/ast"
//def gdtAstDest = "${projectWorkDir}/ast/gdt"
//
//eventCleanStart = {
//        ant.delete(dir:gdtAstDest)
//}
//
//eventCompileStart = {
//        ant.mkdir(dir:"${gdtAstDest}/META-INF")
//        ant.groovyc(destdir: gdtAstDest,
//                                encoding: "UTF-8") {
//                        src(path: gdtAstPath)
////                        src(path: "${mongoAstPath}/java")
////                        javac() // to compile java classes using the javac compiler
//        }
//
//        ant.copy(todir:"${gdtAstDest}/META-INF") {
//                        fileset dir:"${gdtAstPath}/META-INF"
//        }
//
//        grailsSettings.compileDependencies << new File(gdtAstDest)
//        classpathSet=false
//        classpath()
//}