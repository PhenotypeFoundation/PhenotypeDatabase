<?xml version="1.0"?>
<templates xmlns="gscf" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="gscf http://www.nmcdsp.org/xml/template.xsd" count="${templates.size()}">
  <g:each in="${templates}" var="template">
	<template>
		<name>${template.name?.encodeAsSimpleXML()}</name>
		<description>${template.description?.encodeAsSimpleXML()}</description>
		<entity>${template.entity?.getName().encodeAsSimpleXML()}</entity>

		<templateFields>
		  <g:each in="${template.fields}" var="field">
			<templateField>
				<name>${field.name?.encodeAsSimpleXML()}</name>
				<type>${field.type?.toString().encodeAsSimpleXML()}</type>
				<unit>${field.unit?.encodeAsSimpleXML()}</unit>
				<comment>${field.comment?.encodeAsSimpleXML()}</comment>
				<required>${field.required ? 'true' : 'false'}</required>
				<preferredIdentifier>${field.preferredIdentifier ? 'true' : 'false'}</preferredIdentifier>

				<g:if test="${field.type.toString() == 'ONTOLOGYTERM'}">
				  <ontologies>
					<g:each in="${field.ontologies}" var="ontology">
					  <ontology>
						  <ncboId>${ontology.ncboId}</ncboId>
						  <ncboVersionedId>${ontology.ncboVersionedId}</ncboVersionedId>
						  <name>${ontology.name?.encodeAsSimpleXML()}</name>
						  <description>${ontology.description?.encodeAsSimpleXML()}</description>
						  <url>${ontology.url?.encodeAsSimpleXML()}</url>
						  <versionNumber>${ontology.versionNumber?.encodeAsSimpleXML()}</versionNumber>
					  </ontology>
					</g:each>
				  </ontologies>
				</g:if>
				<g:if test="${field.type.toString() == 'STRINGLIST'}">
				  <listItems>
					<g:each in="${field.listEntries}" var="listEntry">
					  <listItem>
						  <name>${listEntry.name?.encodeAsSimpleXML()}</name>
					  </listItem>
					</g:each>
				  </listItems>
				</g:if>
			</templateField>
		  </g:each>
		</templateFields>
	</template>
  </g:each>
</templates>