
<%@ page import="dbnp.studycapturing.Study" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
  <title><g:message code="default.show.label" args="[entityName]" /></title>
  <script type="text/javascript">
    $(function() {
            $("#tabs").tabs();
    });
  </script>

</head>
<body>

  <div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
  </div>

  <div class="body">
    <h1><g:message code="default.show.label" args="[entityName]" /></h1>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">

      <div id="tabs">
        <ul>
          <li><a href="#study">Study Information</a></li>
          <li><a href="#subjects">Subjects</a></li>
          <li><a href="#events">Events</a></li>
          <li><a href="#event-group">Event Groups</a></li>
          <li><a href="#assays">Assays</a></li>
          <li><a href="#persons">Persons</a></li>
        </ul>

        <div id="study">

          <b> Id </b>: ${fieldValue(bean: studyInstance, field: "id")} <br>
          <b>Template </b>:<g:link controller="template" action="show" id="${studyInstance?.template?.id}">${studyInstance?.template?.encodeAsHTML()}</g:link><br>
          <b> Start </b>:<g:formatDate date="${studyInstance?.startDate}" /> <br>
          <b> Events </b>:
            <g:if test="${studyInstance.giveEventTemplates().size()==0}">
              -
            </g:if>
            <g:else>
             ${studyInstance.giveEventTemplates().name}
            </g:else>
          <br>
          <b>Sampling Events </b>:
            <g:if test="${studyInstance.giveSamplingEventTemplates().size()==0}">
             -
            </g:if>
            <g:else>
              ${studyInstance.giveSamplingEventTemplates().name}
            </g:else>
          <br>
          <b>Last Updated </b>:<g:formatDate date="${studyInstance?.lastUpdated}" /><br>
          <b>Readers </b>:

          <g:if test="${studyInstance.readers.size()==0}">
            -
          </g:if>
          <g:else>
            <ul>
              <g:each in="${studyInstance.readers}" var="r">
                <li><g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
              </g:each>
            </ul>
          </g:else>
          <br>

          <!-- All template fields -->
          <g:each in="${studyInstance.giveFields()}" var="field">
            <b>${field.name}</b>: ${studyInstance.getFieldValue(field.name)}<br />
          </g:each>

          <b>Editors </b>:
          <g:if test="${studyInstance.editors.size()==0}">
            -
          </g:if>
          <g:else>
            <ul>
              <g:each in="${studyInstance.editors}" var="e">
                <li><g:link controller="user" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
              </g:each>
            </ul>
          </g:else>
          <br>
          <b>Title </b>: ${fieldValue(bean: studyInstance, field: "title")} <br>
          <b>Owner </b>:<g:link controller="user" action="show" id="${studyInstance?.owner?.id}">${studyInstance?.owner?.encodeAsHTML()}</g:link> <br>
          <b>Date Created </b>:<g:formatDate date="${studyInstance?.dateCreated}" /> <br>
        </div>

        <div id="subjects">
          <g:each in="${studyInstance.giveSubjectTemplates()}" var="template">
            <table>
              <tr>
                <td><b>Id </b></td>
                <td><b>Species</b></td>
                <td><b>Name</b></td>
              <g:each in="${template.fields}" var="g">
                <td><b>
                    <g:link controller="templateField" action="show" id="${g.id}">
${g}</b></td>
                </g:link>
              </g:each>
              </tr>

              <g:each in="${studyInstance.subjects.findAll {it.template == template}}" var="s">
                <tr>
                  <td><g:link controller="subject" action="show" id="${s.id}">${s.id}</g:link></td>
                <td>${s.species}</td>
                <td>${s.name}</td>
                <g:each in="${template.fields}" var="g">
                  <td>
                  <% print s.getFieldValue(g.toString())  %>
                  </td>
                </g:each>
                </tr>
              </g:each>

            </table>

          </g:each>
        </div>


        <div id="events">
          <g:if test="${studyInstance.events.size()==0}">
            No events in this study
          </g:if>
          <g:else>

              <table>
                <tr>
                  <td><b>Id </b></td>
                  <td><b>Start time</b></td>
                  <td><b>Duration</b></td>
                  <td><b>Type</b></td>
                  <td><b>Sampling event</b></td>
                  <td><b>Parameters</b></td>
                </tr>

                <g:each in="${studyInstance.events}" var="event">
                  <tr>
                    <td><g:link controller="event" action="show" id="${event.id}">${event.id}</g:link></td>
                    <td>${event.getPrettyDuration(studyInstance.startDate,event.startTime)}</td>
                    <td>${event.getPrettyDuration()}</td>
                    <td>${event.template.name}</td>
                    <td>
                      <g:if test="${event instanceof dbnp.studycapturing.SamplingEvent}">
                        <g:checkBox name="samplingEvent" disabled="${true}" value="${true}"/>
                      </g:if>
                      <g:else>
                        <g:checkBox name="event" disabled="${true}" value="${false}" />
                      </g:else>
                    </td>
                    <td>
                      <g:set var="fieldCounter" value="${1}" />
                      <g:each in="${event.giveFields()}" var="field">
                        <g:if test="${event.getFieldValue(field.name)}">
                          <g:if test="${fieldCounter > 1}">, </g:if>
                            ${field.name} = ${event.getFieldValue( field.name )}
                          <g:set var="fieldCounter" value="${fieldCounter + 1}" />
                        </g:if>
                      </g:each>
                    </td>
                  </tr>
                </g:each>

              </table>

          </g:else>
        </div>

        <div id="event-group">
          <g:if test="${studyInstance.eventGroups.size()==0}">
            No event groups in this study
          </g:if>
          <g:else>
            <table>
              <tr>
                <td><b>Name</b></td>
                <td colspan="${studyInstance.giveEventTemplates().size()}"><b>Events</b></td>
                <td><b>Subjects</b></td>
              </tr>
              <tr>
                <td></td>
                <g:each in="${studyInstance.giveEventTemplates()}" var="eventTemplate">
                  <td><b>${eventTemplate.name}</b></td>
                </g:each>
                <td></td>
              </tr>
              <g:each in="${studyInstance.eventGroups}" var="eventGroup">
                <tr>
                  <td>${eventGroup.name}</td>

                  <g:each in="${studyInstance.giveEventTemplates()}" var="currentEventTemplate">
                    <td>
                      <g:each in="${eventGroup.events}" var="event">
                        <g:if test="${event.template.name==currentEventTemplate.name}">

                          <g:set var="fieldCounter" value="${1}" />
                          <g:each in="${event.giveFields()}" var="field">
                            <g:if test="${event.getFieldValue(field.name)}">
                              <g:if test="${fieldCounter > 1}">, </g:if>
                                ${field.name} = ${event.getFieldValue( field.name )}
                              <g:set var="fieldCounter" value="${fieldCounter + 1}" />
                            </g:if>
                          </g:each>
                        </g:if>
                      </g:each>
                     </td>
                  </g:each>
                  <td>${eventGroup.subjects.name.join( ', ' )}</td>
                </tr>
              </g:each>
            </table>
          </g:else>
        </div>

        <div id="assays">
          <g:if test="${studyInstance.assays.size()==0}">
            No assays in this study
          </g:if>
          <g:else>
            <table>
              <tr>
                <td width="100"><b>Assay Name</b></td>
                <td width="100"><b>Module</b></td>
                <td><b>Type</b></td>
                <td width="150"><b>Platform</b></td>
                <td><b>Url</b></td>
                <td><b>Samples</b></td>
              </tr>
              <g:each in="${studyInstance.assays}" var="assay">
                <tr>
                  <td>${assay.name}</td>
                  <td>${assay.module.name}</td>
                  <td>${assay.module.type}</td>
                  <td>${assay.module.platform}</td>
                  <td>${assay.module.url}</td>
                  <td>
                <g:each in="${assay.samples}" var="assaySample">
${assaySample.name}<br>
                </g:each>
                </td>
                </tr>
              </g:each>

            </table>
          </g:else>
        </div>

        <div id="persons">
          <g:if test="${studyInstance.persons.size()==0}">
            No persons involved in this study
          </g:if>
          <g:else>
            <table>
              <tr>
                  <td><b>Name</b></td>
                  <td><b>Affiliations</b></td>
                  <td><b>Role</b></td>
                  <td><b>Phone</b></td>
                  <td><b>Email</b></td>
              </tr>
              <g:each in="${studyInstance.persons}" var="studyperson">
                <tr>
                  <td>${studyperson.person.firstName} ${studyperson.person.prefix} ${studyperson.person.lastName}</td>
                  <td>
                    ${studyperson.person.affiliations.name.join(', ')}
                  </td>
                  <td>${studyperson.role.name}</td>
                  <td>${studyperson.person.phone}</td>
                  <td>${studyperson.person.email}</td>
                </tr>
              </g:each>
            </table>
          </g:else>
        </div>
      </div>
    </div>
    <br>
    <div class="buttons">
      <g:form>
        <g:hiddenField name="id" value="${studyInstance?.id}" />
        <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
        <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
      </g:form>
    </div>
  </div>
</body>
</html>
