  <g:if test="${studyList*.subjects?.flatten()?.size()==0}">
	No subjects in the selected studies
  </g:if>
  <g:else>
	<table>
	  <thead>
		<tr>
		  <g:if test="${multipleStudies}">
			<th></th>
		  </g:if>
		  <g:each in="${new dbnp.studycapturing.Subject().giveDomainFields()}" var="field">
			<th>${field}</th>
		  </g:each>

		  <%
			// Determine a union of the fields for all different
			// subjects in all studies. In order to show a proper list.
			// We want every field to appear just once, so the list is filtered
			// for unique names. This ensures that different template fields with
			// the same name, show up in the same column. This might happen when
			// both humans and mice are shown, where humans have a weight in kgs
			// and mice have a weight in grams.
			subjectTemplates = studyList*.giveSubjectTemplates()?.flatten().unique()
			if( !subjectTemplates ) {
			  subjectTemplates = [];
			  subjectFields = [];
			} else {
			  subjectFields = subjectTemplates*.fields?.flatten().name.unique()
			  if( !subjectFields ) {
				subjectFields = [];
			  }
			}

			/*
			 * These lines are rewritten because
			 * performance sucked
			 *
			 *   // These took about 9 seconds (for 31 subjects and
			 *   allSubjects = studyList*.subjects?.flatten()
			 *
			 *   subjectFields = subjectFields.findAll { subjectField ->
			 *     ( true in allSubjects.collect { subject -> subject.fieldExists( subjectField.name ) && subject.getFieldValue( subjectField.name ) != null }.flatten() )
			 *   }
			 */

			// Filter out all fields that are left blank for all subjects
			allSubjects = studyList*.subjects?.flatten()

			showSubjectFields = []
			subjectFields.each { subjectField ->
			  for( subject in allSubjects )
			  {
				// If the field is filled for this subject, we have to
				// show the field and should not check any other
				// subjects (hence the break)
				if( subject.fieldExists( subjectField ) && subject.getFieldValue( subjectField ) ) {
				  showSubjectFields << subjectField;
				  break;
				}
			  }
			}
		  %>

		  <g:each in="${showSubjectFields}" var="field">
			<th>${field}</th>
		  </g:each>

		</tr>
	  </thead>

	  <g:set var="i" value="${1}" />

	  <g:each in="${studyList}" var="studyInstance">
		<g:each in="${studyInstance.subjects}" var="subject" status="j">
		  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			<g:if test="${multipleStudies && j==0}">
			  <td class="studytitle" rowspan="${studyInstance.subjects?.size()}">
				${studyInstance.title}
			  </td>
			</g:if>
			<g:each in="${subject.giveDomainFields()}" var="field">
			  <td><wizard:showTemplateField field="${field}" entity="${subject}" /></td>
			</g:each>

			<g:each in="${showSubjectFields}" var="fieldname">
			  <td>
				<g:if test="${subject.fieldExists(fieldname)}">
					<g:set var="field" value="${subject.getField(fieldname)}" />
					<wizard:showTemplateField field="${field}" entity="${subject}" />
				</g:if>
				<g:else>
				  N/A
				</g:else>
			  </td>
			</g:each>

		  </tr>
		  <g:set var="i" value="${i + 1}" />
		</g:each>
	  </g:each>
	</table>
  </g:else>