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
 *  $Author$
 *  $Rev$
 *  $Date$
 */
package org.dbnp.gdt

import org.codehaus.groovy.grails.commons.ApplicationHolder

class GdtService implements Serializable {
    // Must be false, since the webflow can't use a transactional service. See
    // http://www.grails.org/WebFlow for more information
    static transactional = false


	// cached template entities
	static cachedEntities

	/**
	 * return the templateField class based on casedName
	 * @param casedName
	 * @return
	 */
	def public getTemplateFieldTypeByCasedName(String casedName) {
		def grailsApplication = ApplicationHolder.application
		return grailsApplication.getAllClasses().find{it.name =~ "${casedName}Field" && it.name =~ /Template([A-Za-z]{1,})Field$/}
	}

	/**
	 * get all domain classes that use the domain templates
	 * @return map
	 */
	def getTemplateEntities() {
		// return cached entities if present
		if (cachedEntities) return cachedEntities

		// fetch entities and cache them
		def grailsApplication = ApplicationHolder.application
		def entities = []

		// iterate through domain classes
		grailsApplication.getArtefacts("Domain").each {
			def myInstance = it.clazz
			if (myInstance.properties.superclass.toString() =~ 'TemplateEntity') {
				def matches	= myInstance.toString() =~ /\.([^\.]+)$/

                entities[entities.size()] = [
                        name		: matches[0][1],
                        description	: matches[0][1].replaceAll(/([A-Z])/, ' $1').replaceFirst(/^ /,''),
                        entity		: prepareEntity(myInstance.toString()),
                        instance	: myInstance,
                        encoded		: encodeEntity(prepareEntity(myInstance.toString()))
                ]
			}
		}

		// cache entities
		cachedEntities = entities

		return cachedEntities
	}

	/**
	 * encrypt the name of an entity
	 * @param String entityName
	 * @return String
	 */
	def String encodeEntity(String entityName) {
		// encode the class name, looks unprofessional to have Java class names in URL
        java.net.URLEncoder.encode(entityName.replaceAll(/^class /, '').bytes.encodeBase64().toString(),"UTF-8")
	}

    def String prepareEntity(String entityName) {
        return entityName.replaceAll(/^class /, '');
    }

	/**
	 * decrypt an entity
	 * @param String entity
	 * @return String
	 */
	def String decodeEntity(String entity) {
        // URL decode and base64 decode
        new String(java.net.URLDecoder.decode(entity,"UTF-8").decodeBase64())

	}

	/**
	 * instantiate by encrypted entity
	 * @param String entity
	 * @return Object
	 */
	def getInstanceByEntity(String entity) {
		return getInstanceByEntityName(decodeEntity(entity))
	}

	/**
	 * instantiate by entity name
	 * @param String entityName
	 * @return Object
	 */
	def getInstanceByEntityName(String entityName) {
		def grailsApplication = ApplicationHolder.application
		def entity

        // Check whether the entityName is actually a domain class
        def entities = getTemplateEntities()
        if (!entities.entity*.equals(entityName).count { it }) {
            throw new InvalidClassException("Unregistered class name passed: ${entityName}")
        }

		// dynamically instantiate the entity (if possible)
		try {
			entity = Class.forName(entityName, true, grailsApplication.getClassLoader())

			// succes, is entity an instance of TemplateEntity?
			if (entity && entity.superclass =~ /TemplateEntity$/ || entity.superclass.superclass =~ /TemplateEntity$/) {
				return entity
			}
		} catch (Exception e) {}

		return false
	}

	/**
	 * check if an entity is valid
	 * @param entity
	 * @return
	 */
	def Boolean checkEntity(String entity) {
		if (getInstanceByEntity(entity)) {
			return true
		} else {
			return false
		}
	}
}