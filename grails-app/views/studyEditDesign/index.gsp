<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study edit wizard</title>
	
	<r:require modules="studyEdit,gscf-datatables" />
</head>
<body>
	<div class="basicTabLayout studyEdit studyProperties">
	
		<h1>
			<span class="truncated-title">
				Edit study [${study.code?.encodeAsHTML()}]
			</span>
			<g:render template="/studyEdit/steps" model="[study: study, active: 'design']"  />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<span class="message info"> 
			<span class="title">Define or import your study design</span> 
			The study design consists of treatement types and sample types, grouped together in sample & treatment groups. Sample & treatment groups can be assigned to groups of subjects.<br /><br />
			<strong>N.B.</strong>You can edit your subject groups by double clicking on its name.
		</span>
		
		<g:form action="design" name="design">
			<g:hiddenField name="_action" />
			<g:hiddenField name="id" value="${study.id}" />
			
			<div id="studydesign">
				<div id="timeline-eventgroups"></div>
				<div class="overlay">The first step is to add one or more subject groups.<br />After that, you can create sample &amp; treatment groups and assign them to the subject groups.</div>
			</div>
			<div id="design-meta">
				<div id="subjectGroups" class="subjectgroups addToTimeline">
					<ul>
						<li class="add" onClick="StudyEdit.design.subjectGroups.add();">Add subjectgroup</li>
					</ul>
				</div>
				<div id="eventgroups" class="eventgroups addToTimeline">
					<h3>Available sample & treatment groups</h3>
					<ul>
						<g:each in="${study.eventGroups.sort() { it.name } }" var="eventgroup">
							<li id="eventgroup-${eventgroup.id}" data-duration="${eventgroup.duration.value}" data-origin-id="${eventgroup.id}" data-url="${g.createLink( controller: 'studyEditDesign', action: 'eventGroupDetails', id: eventgroup.id)}">
								<span class="name">${eventgroup.name}</span>
								<span class="events">
									${eventgroup.contents}
								</span>
								<span class="designobject-buttons">
									<a href="#" class="edit">edit</a>
									<a href="#" class="delete">del</a>
								</span>
							</li>
						</g:each>
						<li class="add" onClick="StudyEdit.design.eventGroups.add();">Add new</li>
					</ul>
				</div>
			</div>
			
			<p class="options">
				<g:link controller="studyEdit" action="subjects" id="${study.id}" class="previous">Previous</g:link>
				<g:link controller="studyEdit" action="samples" id="${study.id}" class="next">Next</g:link>
			</p>				
			
			<br clear="all" />
		</g:form>
		
		<div id="eventGroupDialog">
			<span class="message info"> 
				<span class="title">Edit the details of the sample & treatment group</span> 
				Drag treatement types and sample types into the group. Changes will be saved immediately. However, changes in the name require a click on the 'save name' button.<br />
				<strong>Please note</strong>: changes to this sample &amp; treatment group will affect all instances of the group.<br />
				<strong>Please note</strong>: the timing of treatment and sample types also depends on the timing of the group within the study.
			</span>
					
			<label>Name: </label><input type="text" name="eventgroup-name" id="eventgroup-name" /><br />
			
			<div id="timeline-events"></div>
			
			<div id="design-meta">
				<div id="events" class="events addToTimeline" data-url="${g.createLink( action: 'eventList', id: study.id )}">
					<g:render template="eventList" params="[ study: study ]" />
				</div>
				<div id="samplingEvents" class="samplingEvents addToTimeline" data-url="${g.createLink( action: 'samplingEventList', id: study.id )}">
					<g:render template="samplingEventList" params="[ study: study ]" />
				</div>
			</div>			
		</div>
		<div id="eventGroupContentsDialog">
		</div>
		<div id="subjectGroupDialog">
			<span class="message info"> 
				<span class="title">Edit the details of the subjectgroup</span> 
				Check the subjects that are in this group
			</span>
			
			<label>Name: </label><input type="text" name="subjectgroup-name" id="subjectgroup-name" /><br />
			
			<div id="design-subjects">
				<table id="selectSubjectsTable" data-fieldPrefix="subject" class="subjectsTable selectMulti" rel="${g.createLink(action:"dataTableSubjectSelection", id: study.id)}">
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
							<div class="messagebar selectAll">
								You selected all items on this page. Would you <a href="#">select all items on other pages</a> as well? 
							</div>						
						</td></tr>
					</tfoot>
				</table>	
			</div>				
		</div>
		
		<%-- These forms are meant to use the URL in javascript in a generic way --%>		
		<g:form action="event" name="event"></g:form>
		<g:form action="samplingEvent" name="samplingEvent"></g:form>
		<g:form action="eventGroup" name="eventGroup"></g:form>
		<g:form action="subjectGroup" name="subjectGroup"></g:form>
		<g:form action="eventInEventGroup" name="eventInEventGroup"></g:form>
		<g:form action="samplingEventInEventGroup" name="samplingEventInEventGroup"></g:form>
		<g:form action="subjectEventGroup" name="subjectEventGroup"></g:form>

		<r:script>	
			$(function() {
				var data = [];
				<g:each in="${study.subjectEventGroups}" var="group">

					<g:set var="hasSamples" value="${group.sampleCount != 0}"/>

				     data.push({
				       'start': new Date(${group.startDate.time}),
				       'end': new Date(${group.endDate.time}),  // end is optional
				       'type': "${group.eventGroup?.duration?.value == 0 ? 'box' : 'range' }",
				       'content': '${group.eventGroup?.name.encodeAsJavaScript()}',
				       'group': '${group.subjectGroup?.name.encodeAsJavaScript()}',
				       'className': 'eventgroup eventgroup-id-${group.id} <g:if test="${hasSamples}">hasSamples</g:if>',
				       'data': { 
				       		id: ${group.id},
				       		hasSamples: <g:if test="${hasSamples}">true</g:if><g:else>false</g:else>,
				       		group: '${group.subjectGroup?.name.encodeAsJavaScript()}',
				       		subjectGroupId: ${group.subjectGroup?.id},
				       		eventGroupId: ${group.eventGroup?.id}
				       }
				     });
     			</g:each>
				
				StudyEdit.design.initialize( data, new Date(${study.startDate?.time}), StudyEdit.design.subjectGroups.groups.data );
				
				<g:if test="${study.subjectGroups}">
					// Make sure all groups exist
					<g:each in="${study.subjectGroups}" var="group">
                        <!-- please verify whether subjext_idx are set correct if an error about group.id appears! -->
						StudyEdit.design.subjectGroups.groups.data.push( { 'id': ${group.id}, 'name':  '${group.name .encodeAsJavaScript()}' } );
						StudyEdit.design.subjectGroups.updateTimeline();
	  				</g:each>
	  			</g:if>
			});
		</r:script>
	</div>
</body>
</html>
