<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study edit wizard</title>
	
	<r:require modules="studyEdit" />
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
			The study design consists of treatement types and sample types, grouped together in sample & treatment groups. Sample & treatment groups can be assigned to groups of subjects.
		</span>
		
		<g:form action="design" name="design">
			<g:hiddenField name="_action" />
			<g:hiddenField name="id" value="${study.id}" />
			
			<div id="studydesign">
				<div id="timeline-eventgroups"></div>
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
						<g:each in="${study.eventGroups}" var="eventgroup">
							<li id="eventgroup-${eventgroup.id}" data-duration="${eventgroup.duration.value}" data-origin-id="${eventgroup.id}" data-url="${g.createLink( controller: 'studyEditDesign', action: 'eventGroupDetails', id: eventgroup.id)}">
								<span class="name">${eventgroup.name}</span>
								<span class="events">
									${eventgroup.contents}
								</span>
								<span class="eventgroup-buttons">
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
				<strong>Please note</strong>: changes to this sample & treatment group will affect all instances of the group.
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
				<span class="subjects">
					<g:set var="subjectCount" value="${study.subjects?.size()}" />
					<g:set var="numRows" value="${Math.max( (int)subjectCount / 3, 1 )}" />
					<g:each in="${study.subjects}" var="subject" status="i">
						<span class="subject">
							<input type="checkbox" name="subjectgroup_subjects" id="subjectgroup_subjects_${subject.id}" value="${subject.id}" /> ${subject.name}
						</span>
						
						<g:if test="${( i + 1 ) % numRows == 0}">
							</span>
							<span class="subjects">
						</g:if>
					</g:each>
				</span>
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

		<g:set var="timelineMax" value="${study.subjectEventGroups.endTime.max()}"/>
		
		<r:script>	
			$(function() {
				var data = [];
				<g:each in="${study.subjectEventGroups}" var="group">
				     data.push({
				       'start': new Date(${group.startDate.time}),
				       'end': new Date(${group.endDate.time}),  // end is optional
				       'type': "${group.eventGroup?.duration?.value < (timelineMax/50) ? 'box' : 'range' }",	// ${group.eventGroup?.duration}
				       'content': '${group.eventGroup?.name.encodeAsJavaScript()}',
				       'group': '${group.subjectGroup?.name.encodeAsJavaScript()}',
				       'className': 'eventgroup eventgroup-id-${group.id} <g:if test="${group.samples}">hasSamples</g:if>',
				       'data': { 
				       		id: ${group.id},
				       		hasSamples: <g:if test="${group.samples}">true</g:if><g:else>false</g:else>,
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

	<sec:ifAnyGranted roles="ROLE_ADMIN">
		<div>
			<p>
				<h2>Migrate this study!</h2>
				<g:if test="${migrateDesign.step == 1}">
					<p>
						This seems to be a legacy study, click the 'migrate' button below to migrate this study to the new format<br>
						<b>Note:</b> please do not make manual changes to this study before migration.
					<g:form controller="tnoMigrate" action="migrateStudy">
						<g:hiddenField name="id" value="${study.id}"/>
						<g:submitButton name="Migrate" class="button-4 margin10 pie"/>
					</g:form>
					</p>
				</g:if>
				<g:elseif test="${migrateDesign.step == 2}">
					<p>
						<b>Please validate the complete study design.</b><br>
						If something didn't work out the way it supposed, please click the 'reset' button below.
						<g:form controller="tnoMigrate" action="deleteNewDesign">
							<g:hiddenField name="id" value="${study.id}"/>
							<g:hiddenField name="migrateDesign" value="${migrateDesign}"/>
							<g:submitButton name="Reset" class="button-4 margin10 pie"/>
						</g:form>
					</p>
					<br><br>
					<p>
						If it all worked out fine please click the 'proceed' button below to delete the old design and link the samples to the new design.
						<g:form controller="tnoMigrate" action="deleteOldDesignAndLinkSamples">
							<g:hiddenField name="id" value="${study.id}"/>
							<g:hiddenField name="migrateDesign" value="${migrateDesign}"/>
							<g:submitButton name="Proceed" class="button-4 margin10 pie"/>
						</g:form>
					</p>
				</g:elseif>
			</p>
		</div>
	</sec:ifAnyGranted>
</body>
</html>
