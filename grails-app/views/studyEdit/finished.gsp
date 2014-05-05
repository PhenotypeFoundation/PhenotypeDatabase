<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <title>Study edit wizard</title>

    <r:require modules="studyEdit,gscf-datatables" />
</head>
<body>
<div class="studyEdit studyAssays">
    <h1>
        <span class="truncated-title">
            Edit study [${study.code?.encodeAsHTML()}]
        </span>
        <g:render template="steps" model="[study: study, active: 'finished']"  />
    </h1>

    <g:if test="${flash.error}">
        <div class="errormessage">
            ${flash.error.toString().encodeAsHTML()}
        </div>
    </g:if>
    <g:if test="${flash.message}">
        <div class="message">
            ${flash.message.toString().encodeAsHTML()}
        </div>
    </g:if>

    <g:if test="${flash.validationErrors}">
        <div class="errormessage">
            <g:each var="error" in="${flash.validationErrors}">
                ${error.value}<br />
            </g:each>
        </div>
    </g:if>

    <span class="info">
        <span class="title">Finished</span>
        You are done creating your study. Below you will find a summary of the study you have just defined.
        Click <g:link controller="study" action="list">here</g:link> to back to the studies overview page.
    </span>

    <div id="accordion">
        <h3><a href="#">General overview</a></h3>
        <div>
            <p>
                You have created a study containing ${(study.subjects) ? study.subjects.size() : 0} subjects,
                ${(study.events) ? study.events.size() : 0} events and ${(study.samplingEvents) ? study.samplingEvents.size(): 0} sampling events grouped into
                ${(study.eventGroups) ? study.eventGroups.size() : 0} event groups. The study results in
                ${(study.samples) ? study.samples.size() : 0} samples analyzed using ${(study.assays) ? study.assays.size() : 0} assays.
            </p>
        </div>
        <h3><a href="#">Study</a></h3>
        <div>
            <p>
            <ul>
                <g:each var="field" in="${study.giveFields()}">
                    <g:if test="${study.getFieldValue(field.name)}"><li>${field.name} - ${study.getFieldValue(field.name)}</li></g:if>
                </g:each>
            </ul>
        </p>
            Not right? Click <g:link controller="studyEdit" action="edit" id="${study.id}">here</g:link> to go back to the study page and make corrections.
        </div>
        <h3><a href="#">Subjects</a></h3>
        <div>
            <g:each var="subject" in="${study.subjects}">
                <p><b>${subject}</b></p>
                <ul>
                    <g:each var="field" in="${subject?.giveFields()}">
                        <g:if test="${subject.getFieldValue(field.name)}"><li>${field.name} - ${subject.getFieldValue(field.name)}</li></g:if>
                    </g:each>
                </ul>
            </g:each>

            Not right? Click <g:link controller="studyEdit" action="subjects" id="${study.id}">here</g:link> to go back to the subjects page and make corrections.
        </div>
        <h3><a href="#">Events</a></h3>
        <div>
            <g:each var="template" in="${study.giveAllEventTemplates()}">
                <p><b>${template}</b></p>
                <ul>
                    <g:each var="event" in="${study.giveEventsForTemplate(template)}">
                        <li>
                            <i><g:if test="${(event.getClass() == 'SamplingEvent')}">Sampling </g:if>Event</i>
                            <ul>
                                <g:each var="field" in="${event?.giveFields()}">
                                    <li>${field} - ${(field.type.toString() == "RELTIME") ? new org.dbnp.gdt.RelTime(event.getFieldValue(field.name)) : event.getFieldValue(field.name)}</li>
                                </g:each>
                            </ul>
                        </li>
                    </g:each>
                </ul>
            </g:each>

            Not right? Click <g:link controller="studyEditDesign" action="index" id="${study.id}">here</g:link> to go back to the study design page and make corrections.
        </div>
        <h3><a href="#">Samples</a></h3>
        <div>
            <g:each var="sample" in="${study.samples}">
                <p><b>${sample}</b></p>
                <ul>
                    <g:each var="field" in="${sample?.giveFields()}">
                        <g:if test="${sample.getFieldValue(field.name)}"><li>${field.name} - ${sample.getFieldValue(field.name)}</li></g:if>
                    </g:each>
                </ul>
            </g:each>

            Not right? Click <g:link controller="studyEdit" action="samples" id="${study.id}">here</g:link> to go back to the samples page and make corrections.
        </div>
        <h3><a href="#">Assays</a></h3>
        <div>
            <g:each var="assay" in="${study.assays}">
                <p><b>${assay}</b></p>
                <ul>
                    <g:each var="field" in="${assay?.giveFields()}">
                        <g:if test="${assay.getFieldValue(field.name)}"><li>${field.name} - ${assay.getFieldValue(field.name)}</li></g:if>
                    </g:each>
                </ul>
            </g:each>

            Not right? Click <g:link controller="studyEdit" action="assays" id="${study.id}">here</g:link> to go back to the assays page and make corrections.
        </div>
    </div>

    <r:script>
        $(function() {
            $("#accordion").accordion({collapsible: true, heightStyle: "content"});
        });
    </r:script>
</div>
</body>
</html>
