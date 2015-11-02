/**
 *  GDT, a plugin for Grails Domain Templates
 *  Copyright (C) 2011 Jeroen Wesbeek
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

import grails.util.Holders
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON

import groovy.json.JsonSlurper

class TemplateOntologyTermField extends TemplateFieldTypeNew {
    static contains				= Term
    static String type			= "ONTOLOGYTERM"
    static String casedType		= "Term"
    static String description	= "Term from ontology"
    static String category		= "Other"
    static String example		= "A term that comes from one or more selected ontologies"

    // GDT dynamic options
    // as of 20130118 we have removed the AST transformations
    // see ticket https://github.com/PhenotypeFoundation/GSCF/issues/64
    // therefore the maps is now hardcoded and not injected anymore
    // static gdtAddTemplateFieldHasMany = [ontologies: org.dbnp.gdt.Ontology] // to store the ontologies to choose from when the type is 'ontology term'
    // see TemplateField's hasMany map...
    // end change

    /**
     * Static validator closure
     * @param fields
     * @param obj
     * @param errors
     */
    static def validator = { fields, obj, errors ->
        genericValidator(fields, obj, errors, TemplateFieldType.ONTOLOGYTERM, { value -> (value as Term) })
    }

    /**
     * cast value to the proper type (if required and if possible)
     * @param TemplateField field
     * @param mixed value
     * @return Term
     * @throws IllegalArgumentException
     */
    static Term castValue(org.dbnp.gdt.TemplateField field, value, def currentValue) {
        if (value) {
            if (value instanceof Term) {
                return value
            } else if (value.class == String) {
                // find ontology that has this term
                def ontology = field.ontologies.find{it.giveTermByName(value)}
                if (ontology) {
                    return ontology.giveTermByName(value)
                } else {
                    def ontoTerm = Term.findAllByName(value).find {
                        it.name == value
                    }

                    // Term has been used before and is cached in DB
                    if (ontoTerm) {
                        return ontoTerm
                    }

                    // Use Bioontology recommender (configured in the properties)
                    def config = Holders.config
                    String recommenderUrl = config.bioontology.recommender
                    String apiKey = config.bioontology.apikey

                    if (recommenderUrl.empty) {
                        println "Recommender URL not set"
                        throw new IllegalArgumentException("BioOntology Recommender URL not set")
                    }
                    def searchParams = [:]
                    searchParams['input'] = value.replaceAll("\\s", "+")
                    searchParams['pagesize'] = "1"
                    searchParams['display_links'] = "false"
                    searchParams['display_context'] = "false"
                    searchParams['format'] = "json"

                    String queryArgs = searchParams
                            .collect { k,v -> "$k=$v" }
                            .join('&')
                    String queryUrl =  recommenderUrl + "?" + queryArgs
                    def http = new HTTPBuilder(queryUrl)

                    /*
                     * Parsed as JSON by HTTPBuilder
                     */
                    http.request( GET, JSON ) {
                        // Set Apikey in the header. Using QueryString in a URL object does not suffice
                        headers.'Authorization' = 'apikey token='+ apiKey

                        response.success = { response, resOnt ->
                            def result = resOnt[0]
                            if (result) {
                                if (result.ontologies.length == 0) {
                                    println "Not in the cache search online. "
                                    throw new IllegalArgumentException("Ontology not found in bioontology.org database for: ${value}")
                                }

                                def firstOntology = result.ontologies[0]['@id']
                                def resolvedOntology = Ontology.getOrCreateOntology(firstOntology)
                                if (result.coverageResult.annotations.length == 0) {
                                    println "Annotations not available for ontology."
                                    throw new IllegalArgumentException("Annotations not available for ontology : ${value}")
                                }
                                def ontologyAnnotation = result.coverageResult.annotations[0].annotatedClass['@id']
                                Term ontologyTerm = Term.getOrCreateTerm(value, resolvedOntology, ontologyAnnotation)

                                return ontologyTerm
                            } else {
                                println "Error accessing BioOntologyAPI for ${recommenderUrl}"
                                throw new IllegalArgumentException("Error accessing BioOntologyAPI for ${recommenderUrl}")
                            }

                        }

                        response.failure = { resp ->
                            println ("ERROR: ontology with ontologyUrl ${apiUrl} could not be found! Server terturned: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
                            throw new IllegalArgumentException("Error accessing BioOntologyAPI for ${recommenderUrl}")
                        }
                    }


                }
            } else {
                throw new IllegalArgumentException("Ontology term not recognized (not in the ontology cache): ${value}")
            }
        } else {
            return null
        }
    }
}