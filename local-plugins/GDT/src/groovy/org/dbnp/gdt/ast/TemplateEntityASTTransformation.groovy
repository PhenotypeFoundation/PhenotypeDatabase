/**
 *  GDT, a plugin for Grails Domain Templates
 *  Copyright (C) 2011 Jeroen Wesbeek, Kees van Bochove
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Author	: Jeroen Wesbeek <work@osx.eu>
 *  Since	: 20110211
 *
 *  Revision Information:
 *  $Author$
 *  $Rev$
 *  $Date$
 */

package org.dbnp.gdt.ast

import org.codehaus.groovy.transform.*
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.ast.stmt.*
import java.lang.reflect.Modifier
import org.codehaus.groovy.grails.compiler.injection.GrailsASTUtils

/**
 * Hook in the Groovy compiler and dynamically extend TemplateEntity with
 * all TemplateEntity fields (= classes) in the application (either in the
 * GDT plugin, other plugins or the application itself)
 *
 * As this is done before compilitation to byteCode, test this with:
 * clear;grails clean;grails compile
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
class TemplateEntityASTTransformation implements ASTTransformation {
	static debug = true					// debugging on/off
	static templateEntity = null		// templateEntity ClassNode
	static templateField = null			// templateField ClassNode
	static templateFieldType = null		// templateFieldType ClassNode
	static templateFields = []			// temporary cache of fields we need to reprocess (templateEntity)
	static templateFields2 = []			// temporary cache of fields we need to reprocess (templateField::hasMany)
	static cacheTemplateFields = []		// cache of all template fields
	static cacheClasses = []			// cache of all other classes
	static myPackage = "gdt"

	/**
	 * class constructor
	 * @return
	 */
	public TemplateEntityASTTransformation() {
		super
	}

	/**
	 * callback method which is called by the compiler when
	 * a Abstact Syntax Tree Node is visited
	 * @param nodes
	 * @param sourceUnit
	 */
	public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
		// iterate through the nodes
		nodes.each { node ->
			// find all TemplateFields or TemplateEntity nodes
			node.getClasses().findAll {it.name =~ /\.Template([A-Za-z]{1,})Field$/ || it.name =~ /\.TemplateEntity$/ || it.name =~ /\.TemplateFieldType$/ || it.name =~ /\.TemplateField$/}.each { ClassNode owner ->
				// is this TemplateEntity or a TemplateField?
				if (owner.name =~ /\.TemplateEntity$/) {
					// remember templateEntity so we can inject
					// templatefields into it
					templateEntity = owner

					// got some cached template fields to handle?
					if (templateFields.size()) {
						templateFields.each {
							// inject the cached TemplateField into TemplateEntity
							injectTemplateField(templateEntity, it, templateFieldType, templateField)
						}
						templateFields = []
					}

					return
				} else if (owner.name =~ /\.TemplateField$/) {
					// remember templateField so we can inject
					// a hasMany relationship into TemplateField's
					// (if required)
					templateField = owner

					// got some cache to handle?
					if (templateFields2.size()) {
						// yes, extend TemplateField to have new hasMany relationships
						templateFields2.each {
							extendTemplateFieldHasMany(templateField, it)
						}
						templateFields2 = []
					}

					return
				} else if (owner.name =~ /\.TemplateFieldType$/) {
					// remember templateFieldType so we can inject
					// templateFields into it
					templateFieldType = owner
					return
				} else if (templateEntity) {
					// add this templateField to the cache
					cacheTemplateFields[ cacheTemplateFields.size() ] = owner

					// inject this template field into TemplateEntity
					injectTemplateField(templateEntity, owner, templateFieldType, templateField)
				} else {
					// add this templateField to the cache
					cacheTemplateFields[ cacheTemplateFields.size() ] = owner

					// remember to revisit this template field when the compiler
					// has visited TemplateEntity
					templateFields[ templateFields.size() ] = owner

					return
				}
			}

			// cache the other classes we visit as we may need
			// them later when dynamically extending hasMany
			// relationships
			// @see TemplateEntityASTTransformation.extendHasMany
			node.getClasses().findAll {!(it.name =~ /\.Template([A-Za-z]{1,})Field$/ || it.name =~ /\.TemplateEntity$/)}.each { ClassNode owner ->
					cacheClasses[cacheClasses.size()] = owner
			}

			// check if we are visiting the application bootstrap to determine
			// if the compile phase has ended for all GDT related files
			if (node.getClasses().findAll {it.name == "BootStrap"}) {
				// we are in the ASTTransformation of the application itself,
				// we should not have any TemplateFields hanging around which
				// were not compiled. If so, they are probably in the application
				// while they should be in a plugin
				if (templateFields.size() || templateFields2.size()) {
					def count = 1
					println ""
					println ""
					println "----------------------===={ Grails Domain Templates (GDT) WARNING }====-------------------------"
					println ""
					println "Compilation of GDT related files has been finished, and the template model has been dynamically"
					println "extended, but the following template field"+(((templateFields.size() + templateFields2.size())>1)?'s were':' was')+" visited by the compiler AFTER the GDT compilation"
					println "was completed. This means the following field"+(((templateFields.size() + templateFields2.size())>1)?'s are':' is')+" NOT available in the application."
					println ""
					println "This problem is most likely caused by "+(((templateFields.size() + templateFields2.size())>1)?'these files':'this file')+" being in the Application source tree itself,"
					println "rather in a seperate plugin. To solve this issue, move the class"+(((templateFields.size() + templateFields2.size())>1)?'es below into their own':' below into it\'s own')
					println "dedicated plugin."
					println ""
					templateFields.each {
						if (count == 1) {
							println "GDT templateField"+((templateFields.size()>1)?'s:':':')+"\t${count++}.\t${it.name}"
						} else {
							println "\t\t\t${count++}.\t${it.name}"
						}
					}
					if (templateFields2) {
						println ""
						println "TemplateField hasMany relationships that could not be injected:"
						templateFields2.each {
							println it.dump()
						}
					}
					println ""
					println "------------------------------------------------------------------------------------------------"
					println ""
					println ""
				}
			}
		}
	}

	/**
	 * add a template field to templateEntity
	 * @param templateEntityClassNode
	 * @param templateFieldClassNode
	 * @param classLoader
	 */
	private void injectTemplateField(ClassNode templateEntityClassNode, ClassNode templateFieldClassNode, ClassNode templateFieldTypeClassNode, ClassNode templateField) {
		def contains = templateFieldClassNode.fields.find { it.properties.name == "contains" }
		def owner = contains.properties.owner
		def typeName = contains.getInitialExpression().variable
		def splitTemplateFieldName = owner.name.split("\\.")
		def templateFieldName = splitTemplateFieldName[(splitTemplateFieldName.size() - 1)]

		// debug
		if (debug) println "injecting ${templateFieldClassNode} into ${templateEntityClassNode}"

		// todo: refactor TemplateOntologyTermField into TemplateTermField so it is consistent with the rest
		//		 for now sticking to the replaceAll to keep this stupid exception to the rule working...
		def templateFieldMapName = (templateFieldName[0].toLowerCase() + templateFieldName.substring(1) + "s").replaceAll(/Ontology/, '')

		// a GORM Map for this templateField to TemplateEntity, e.g.:
		//
		// Map templateLongFields = [:]
		addTemplateFieldMap(templateEntityClassNode, templateFieldMapName)

		// extend the hasMany relationship in TemplateEntity, e.g:
		//
		// static hasMany = [
		// 		...
		//		templateLongFields: long,
		//		...
		// ]
		extendHasMany(templateEntityClassNode, templateFieldName, templateFieldMapName, typeName)

		// extend the TemplateEntity constraints and inject a dynamic
		// validator, e.g.:
		//
		// static constraints = {
		//		...
		//		templateLongFields(validator: TemplateLongField.validator)
		//		...
		// }
		extendConstraints(templateEntityClassNode, templateFieldClassNode, templateFieldMapName, templateFieldName)

		// extend the TemplateFieldType enum with this templateField
		// unfortunately it does not seem possible to modify an enum with the
		// following format:
		// 		STRING('String','Short text', 'Text', 'max 255 chars'),
		// as we cannot see (or set) the variables within an enum entry. Only
		// enums that contain only constants seem to be Transformable:
		//		STRING, INT, etc
		//extendTemplateFieldType(templateFieldTypeClassNode, templateFieldClassNode)

		// extend TemplateField's hasMany relationship, if required
		if (templateFieldClassNode.properties.find{it.name == "gdtAddTemplateFieldHasMany"}) {
			if (templateField) {
				extendTemplateFieldHasMany(templateField, templateFieldClassNode.properties.find{it.name == "gdtAddTemplateFieldHasMany"}.getInitialExpression().properties.mapEntryExpressions)
			} else {
				templateFields2[ templateFields2.size() ] = templateFieldClassNode.properties.find{it.name == "gdtAddTemplateFieldHasMany"}.getInitialExpression().properties.mapEntryExpressions
			}
		}
	}

	/**
	 * dynamically extend the hasMany relationship for TemplateField, based on
	 * the presence of the gdtAddTemplateFieldHasMany variable in the TemplateXXXField
	 * class, e.g.:
	 *
	 * static gdtAddTemplateFieldHasMany = [ontologies: org.dbnp.gdt.Ontology]
	 *
	 * @param templateFieldClassNode
	 * @param expressions
	 * @return
	 */
	public extendTemplateFieldHasMany(ClassNode templateFieldClassNode, ArrayList expressions) {
		// does templateEntity contain a hasMany map?
		if (GrailsASTUtils.hasProperty(templateFieldClassNode, "hasMany")) {
			PropertyNode hasMany = templateFieldClassNode.getProperty("hasMany")
			MapExpression initialExpression = hasMany.getInitialExpression()
			expressions.each{ mapEntryExpression ->
				if (debug) println "injecting TemplateField hasMany relationship for '${mapEntryExpression.keyExpression.value}'"
				initialExpression.addMapEntryExpression(mapEntryExpression)
			}
		} else {
			MapExpression initialExpression = new MapExpression()
			expressions.each{ mapEntryExpression ->
				if (debug) println "injecting TemplateField hasMany relationship for '${mapEntryExpression.keyExpression.value}'"
				initialExpression.addMapEntryExpression(mapEntryExpression)
			}
			templateFieldClassNode.addProperty("hasMany", Modifier.PUBLIC | Modifier.STATIC, new ClassNode(java.util.Map.class), initialExpression, null, null)
		}

		//if (debug) hasMany.getInitialExpression().properties.mapEntryExpressions.each { println it }
	}

	/**
	 * Extend the TemplateFieldType enum dynamically with the given templateField
	 * @param templateFieldTypeClassNode
	 * @param templateFieldClassNode
	 * @return
	 */
	public extendTemplateFieldType(ClassNode templateFieldTypeClassNode, ClassNode templateFieldClassNode) {
		def type = templateFieldClassNode.getProperty("type").getInitialExpression().value
		def casedType = templateFieldClassNode.getProperty("casedType").getInitialExpression().value
		def descriptoin = templateFieldClassNode.getProperty("description").getInitialExpression().value
		def category = templateFieldClassNode.getProperty("category").getInitialExpression().value
		def example = templateFieldClassNode.getProperty("example").getInitialExpression().value

		if (templateFieldTypeClassNode.getFields().find {it.name == type}) {
			if (debug) println " - enum entry ${type} for ${templateFieldClassNode} already exists, skipping"
		} else {
			if (debug) println " - extending ${templateFieldTypeClassNode} with ${type}"
			//templateFieldTypeClassNode.addFieldFirst("BAR", ACC_ENUM + ACC_FINAL + ACC_PUBLIC + ACC_STATIC, declaringClass, null)
			templateFieldTypeClassNode.addFieldFirst(type, 16409, templateFieldTypeClassNode, null)

		}

		if (type == "ONTOLOGYTERM") {

			// STRING		('String'		,'Short text'			, 'Text'		, 'max 255 chars'),			// string
			// <org.codehaus.groovy.ast.FieldNode@55d2162c name=STRING modifiers=16409 type=org.dbnp.gdt.TemplateFieldType owner=org.dbnp.gdt.TemplateFieldType initialValueExpression=null dynamicTyped=false holder=false closureShare=false annotations=[] synthetic=false declaringClass=org.dbnp.gdt.TemplateFieldType hasNoRealSourcePositionFlag=false lineNumber=-1 columnNumber=-1 lastLineNumber=-1 lastColumnNumber=-1>
			println templateFieldTypeClassNode.dump()
			templateFieldTypeClassNode.getFields().each {
				println it.dump()
			}
			def myTest = templateFieldTypeClassNode.getFields().find { it.name == type }
			myTest.each {
				println it.dump()
				println it.getInitialExpression()
			}

			/*
			MethodNode method = getOrAddStaticConstructorNode();
			Statement statement = method.getCode();
			if (statement instanceof BlockStatement) {
				BlockStatement block = (BlockStatement) statement;
				// add given statements for explicitly declared static fields just after enum-special fields
				// are found - the $VALUES binary expression marks the end of such fields.
				List<Statement> blockStatements = block.getStatements();
			}
			FieldNode constraints = templateEntityClassNode.getDeclaredField("constraints")
			if (hasFieldInClosure(constraints, templateFieldMapName)) {
				//println " - constraint closure already exists, skip this"
			} else {
				// debug
				if (debug) println " - extending constraints closure"

				// get the constraints closure
				ClosureExpression initialExpression = (ClosureExpression) constraints.getInitialExpression();
				BlockStatement blockStatement		= (BlockStatement) initialExpression.getCode();
			*/
			//def
		}
	}

	/**
	 * Extend the TemplateEntity constraints and dynamically inject a validator
	 * for the given TemplateField. In groovy code this would be:
	 *
	 * static constraints = {*     ...
	 *     templateLongFields(validator: TemplateLongField.validator)
	 *     ...
	 *}*
	 * @param templateEntityClassNode
	 * @param templateFieldClassNode
	 * @param templateFieldMapName
	 * @param templateFieldName
	 * @return
	 */
	public extendConstraints(ClassNode templateEntityClassNode, ClassNode templateFieldClassNode, String templateFieldMapName, String templateFieldName) {
		// check if it is a built in class or not
		def packageWords, validator
		try {
			// See if this is a built in class
			new ClassNode(Eval.me("${templateFieldName}.class"))
		} catch (Exception e) {
			// no, use class cache which contains
			// all currently visited classes, and
			// contains ClassNodes for each class
			if (!cacheTemplateFields.find {
				packageWords = it.name.toString().split("\\.")
				return (packageWords[(packageWords.size() - 1)] == templateFieldName)
			}) {
				println "ERROR: gdt error 2, the template field ${templateFieldName} was not cached propely; this should not happen!"
			}
		}

		// check if the templateEntity classNode has constraints
		if (GrailsASTUtils.hasProperty(templateEntityClassNode, "constraints")) {
			FieldNode constraints = templateEntityClassNode.getDeclaredField("constraints")

			if (hasFieldInClosure(constraints, templateFieldMapName)) {
				if (debug) println " - constraint closure already exists, skip this"
			} else {
				// debug
				if (debug) println " - extending constraints closure"

				// get the constraints closure
				ClosureExpression initialExpression = (ClosureExpression) constraints.getInitialExpression();
				BlockStatement blockStatement = (BlockStatement) initialExpression.getCode();

				// check if this is
				//  - a built in class, or
				//  - a class which is in the same package
				if (packageWords == null || packageWords[(packageWords.size() - 2)] == myPackage) {
					// create the validator property
					//PropertyExpression
					validator = new PropertyExpression(
						new VariableExpression(templateFieldName),
						"validator"
					)
				} else {
					// some external class which resides in the application
					// or a child plugin
					validator = null

					// create a map based on the packageWords and the validator mathod
					def fullMethodPath = []
					packageWords.each { fullMethodPath[ fullMethodPath.size() ] = it }
					fullMethodPath[ fullMethodPath.size() ] = 'validator'

					// build nested property
					fullMethodPath.each {
						if (!validator) {
							validator = new VariableExpression(it)
						} else {
							validator = new PropertyExpression(validator, it)
						}
					}
				}

				// create expression arguments
				NamedArgumentListExpression arguments = new NamedArgumentListExpression();
				arguments.addMapEntryExpression(
					new ConstantExpression("validator"),			// validator key (name)
					validator										// validator expression
				)

				// create expression using the expression arguments
				MethodCallExpression constantExpression = new MethodCallExpression(
					VariableExpression.THIS_EXPRESSION,				// object
					new ConstantExpression(templateFieldMapName),	// method
					arguments										// arguments
				)

				// add the newly created expression to the contraints' initialExpression
				blockStatement.addStatement(new ExpressionStatement(constantExpression))
			}

			// show all constraints
			//if (debug) templateEntityClassNode.getDeclaredField("constraints").each() { println it.dump() }
		} else {
			// debug
			if (debug) println "ERROR: gdt error 1, constraints closure not present so we cannot exnted it. This should not happen!"
		}
	}

	/**
	 * add template field map to TemplateEntity
	 * @param templateEntityClassNode
	 * @param templateFieldMapName
	 * @return
	 */
	public addTemplateFieldMap(ClassNode templateEntityClassNode, String templateFieldMapName) {
		if (!GrailsASTUtils.hasProperty(templateEntityClassNode, templateFieldMapName)) {
			// debug
			if (debug) println " - adding ${templateFieldMapName} map..."

			templateEntityClassNode.addProperty(templateFieldMapName, Modifier.PUBLIC, new ClassNode(java.util.Map.class), new MapExpression(), null, null)
		} else {
			// debug
			if (debug) println " - ${templateFieldMapName} Map is already present, skip this"
		}
	}

	/**
	 * add hasMany relation ship
	 * @param templateEntityClassNode
	 * @param templateFieldName
	 * @param templateFieldMapName
	 * @param typeName
	 * @return
	 */
	public extendHasMany(ClassNode templateEntityClassNode, String templateFieldName, String templateFieldMapName, String typeName) {
		// check if it is a built in class or not
		def myClass, packageWords
		try {
			// try to use a build in class
			myClass = new ClassNode(Eval.me("${typeName}.class"))
		} catch (Exception e) {
			// no, use class cache which contains
			// all currently visited classes, and
			// contains ClassNodes for each class
			myClass = cacheClasses.find {
				packageWords = it.name.split("\\.")
				return (packageWords[(packageWords.size() - 1)] == typeName)
			}
		}

		// does templateEntity contain a hasMany map?
		if (GrailsASTUtils.hasProperty(templateEntityClassNode, "hasMany")) {
			PropertyNode hasMany = templateEntityClassNode.getProperty("hasMany")
			MapExpression initialExpression = hasMany.getInitialExpression()

			if (!initialExpression.getMapEntryExpressions().find {it.getKeyExpression().toString() =~ templateFieldMapName}) {
				// debug
				if (debug) println " - extending hasMany map"

				// check if this is
				//  - a built in class, or
				//  - a class which is in the same package
				if (packageWords == null || packageWords[(packageWords.size() - 2)] == myPackage) {
					// builtin or package class
					initialExpression.addMapEntryExpression(
						new ConstantExpression(templateFieldMapName),
						new VariableExpression(typeName)
					)
				} else {
					// some external class which resides in the application
					// or a child plugin
					def myProperty = null
					packageWords.each {
						if (myProperty == null) {
							myProperty = new VariableExpression(it)
						} else {
							myProperty = new PropertyExpression(myProperty, it)
						}
					}

					initialExpression.addMapEntryExpression(
						new ConstantExpression(templateFieldMapName),
						myProperty
					)
				}
			} else {
				// debug
				if (debug) println " - hasMany map entry for ${templateFieldName} already present"
			}

			// show all hasMany entries
			//if (debug) hasMany.getInitialExpression().properties.mapEntryExpressions.each { println it }
		} else {
			// debug
			if (debug) println " - adding hasMany map"

			MapExpression initialExpression = new MapExpression()
			initialExpression.addMapEntryExpression(new ConstantExpression(templateFieldMapName), new ClassExpression(myClass))
			templateEntityClassNode.addProperty("hasMany", Modifier.PUBLIC | Modifier.STATIC, new ClassNode(java.util.Map.class), initialExpression, null, null)
		}
	}

	/**
	 * check if a field exists in a closure
	 * @param closure
	 * @param fieldName
	 * @return
	 */
	public boolean hasFieldInClosure(FieldNode closure, String fieldName) {
		if (closure != null) {
			ClosureExpression initialExpression = (ClosureExpression) closure.getInitialExpression()
			BlockStatement blockStatement = (BlockStatement) initialExpression.getCode()
			List<Statement> statements = blockStatement.getStatements()

			// iterate through block statements
			for (Statement expressionStatement: statements) {
				// does the expression statement contain a method?
				if (expressionStatement instanceof ExpressionStatement && ((ExpressionStatement) expressionStatement).getExpression() instanceof MethodCallExpression) {
					// yes, get the expression
					MethodCallExpression methodCallExpression = (MethodCallExpression) ((ExpressionStatement) expressionStatement).getExpression()

					// get the method
					ConstantExpression constantExpression = (ConstantExpression) methodCallExpression.getMethod()

					// is it the same as the field name?
					if (constantExpression.getValue().equals(fieldName)) {
						return true
					}
				}
			}
		}
		return false
	}
}