<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study ${study.code} - Design</title>

	<r:require modules="studyView,gscf-datatables" />
</head>
<body>
	<div class="basicTabLayout studyView studyDesign">

		<h1>
			<span class="truncated-title">
				Study [${study.code?.encodeAsHTML()}]
			</span>
			<g:render template="steps" model="[study: study, active: 'design']"  />
		</h1>

		<g:render template="/common/flashmessages" />

		<span class="message info">
			<span class="title">This page shows your study design</span>
			The study design consists of treatement types and sample types, grouped together in sample & treatment groups.<br>
            Double click on a subjectgroup or sample & treatment group to see details.
		</span>

		<g:form action="design" name="design">
			<g:hiddenField name="_action" />
			<g:hiddenField name="id" value="${study.id}" />

			<div id="studydesign">
				<div id="timeline-eventgroups"></div>
			</div>

			<br clear="all" />
		</g:form>

		<p class="options">
			<g:if test="${study.canWrite(loggedInUser)}">
				<g:link class="edit" controller="studyEditDesign" action="index" id="${study?.id}">edit</g:link>
			</g:if>
			<g:link class="back" controller="study" action="list" >back to list</g:link>
		</p>

        <div id="subjectGroupDialog">
			<span class="message info">
				<span class="title">Checked subjects are in this group</span>
			</span>

			<div id="design-subjects">
				<table id="subjectsTable" data-fieldPrefix="subject" class="subjectsTable selectMulti" rel="${g.createLink(controller:"studyEditDesign",action:"dataTableSubjectSelection", id: study.id)}">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Template</th>
                        <th>Species</th>
                    </tr>
                    </thead>
                    <tfoot>
                    <tr><td>
                        <div class="messagebar loadingSelection">
                            Loading selection of subjects. Please be patient.
                        </div>
                    </td></tr>
                    </tfoot>
                </table>
            </div>
        </div>

		<div id="eventGroupDialog">
			<div id="timeline-events"></div>
		</div>

		<%-- These forms are meant to use the URL in javascript in a generic way --%>
		<g:form controller="studyEditDesign" action="subjectGroup" name="subjectGroup"></g:form>

		<r:script>
			$(function() {

				var data = [];
				<g:each in="${study.subjectEventGroups}" var="group">
				     data.push({
				       'start': new Date(${group.startDate.time}),
				       'end': new Date(${group.endDate.time}),
				       'type': "${group.eventGroup?.duration?.value == 0 ? 'box' : 'range' }",
				       'content': '${group.eventGroup?.name.encodeAsJavaScript()}',
				       'group': '${group.subjectGroup?.name.encodeAsJavaScript()}',
				       'className': 'eventgroup eventgroup-id-${group.id} <g:if test="${group.samples}">hasSamples</g:if>',
				       'data': {
				       		id: ${group.id},
				       		hasSamples: <g:if test="${group.samples}">true</g:if><g:else>false</g:else>,
				       		group: '${group.subjectGroup?.name.encodeAsJavaScript()}',
				       		subjectGroupId: ${group.subjectGroup?.id},
				       		eventGroupId: ${group.eventGroup?.id},
				       		dataUrl: '${g.createLink( controller: 'studyEditDesign', action: 'eventGroupDetails', id: group.eventGroup?.id).encodeAsJavaScript()}'
				       }
				     });
     			</g:each>

				<g:if test="${study.subjectGroups}">
					// Make sure all groups exist
					<g:each in="${study.subjectGroups}" var="group">
						<!-- please verify whether subjext_idx are set correct if an error about group.id appears! -->
                    	StudyView.design.subjectGroups.groups.data.push( { 'id': ${group.id}, 'name':  '${group.name .encodeAsJavaScript()}' } );
					</g:each>
				</g:if>

				StudyView.design.initialize( data, new Date(${study.startDate?.time}), StudyView.design.subjectGroups.groups.data );
			});
		</r:script>
	</div>
</body>
</html>
