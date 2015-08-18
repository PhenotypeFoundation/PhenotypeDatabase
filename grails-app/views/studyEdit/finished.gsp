<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <title>Study edit wizard</title>

    <r:require modules="studyEdit,gscf-datatables" />
</head>
<body>
<div class="basicTabLayout studyEdit studyAssays">
    <h1>
        <span class="truncated-title">
            Edit study [${study.code?.encodeAsHTML()}]
        </span>
        <g:render template="steps" model="[study: study, active: 'finished']"  />
    </h1>

	<g:render template="/common/flashmessages" />
	<g:render template="/common/flash_validation_messages" />
	
    <span class="message info">
        <span class="title">Overview</span>
        Below you will find a summary of the current study.
        Click <g:link controller="study" action="list">here</g:link> to go back to the "All studies" overview page.
    </span>

    <div>
        <h3>General overview</h3>
        <p>
            You have created a study containing ${study.subjectCount} subjects grouped into ${(study.subjectGroups) ? study.subjectGroups.size() : 0} groups.<br />
            
            The design consists of ${(study.events) ? study.events.size() : 0} treatment type(s) and ${(study.samplingEvents) ? study.samplingEvents.size(): 0} sample type(s). 
            They are grouped into ${(study.eventGroups) ? study.eventGroups.size() : 0} groups.
            
            <g:set var="groupsNotUsed" value="${study.eventGroups?.findAll { !it.subjectEventGroups }?.size() ?: 0}" />
            <g:if test="${groupsNotUsed}">
            	Please note: ${groupsNotUsed} sample &amp; treatement group(s) have not been used in the study design.
            </g:if>
            <br />
            
            The study results in ${study.sampleCount} samples analyzed using ${study.assayCount} assays.
        </p>
    </div>

</div>
</body>
</html>
