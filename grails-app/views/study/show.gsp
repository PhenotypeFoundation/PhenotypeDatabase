
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
          <li><a href="#publications">Publications</a></li>
        </ul>

        <div id="study">

          <table>
            <!-- Show all template and domain fields, if filled -->
            <g:each in="${studyInstance.giveFields()}" var="field">
              <g:if test="${studyInstance.getFieldValue(field.name)}">
                <tr>
                  <td>${field}</td>
                  <td>${studyInstance.getFieldValue(field.name)}</td>
                </tr>
              </g:if>
            </g:each>

            <!-- Add some extra fields -->
            <tr>
              <td>Events</td>
              <td>
                <g:if test="${studyInstance.giveEventTemplates().size()==0}">
                  -
                </g:if>
                <g:else>
                 ${studyInstance.giveEventTemplates().name.join(", ")}
                </g:else>
              </td>
            </tr>
            <tr>
              <td>Sampling events</td>
              <td>
                <g:if test="${studyInstance.giveSamplingEventTemplates().size()==0}">
                  -
                </g:if>
                <g:else>
                 ${studyInstance.giveSamplingEventTemplates().name.join(", ")}
                </g:else>
              </td>
            </tr>
            <tr>
              <td>Readers</td>
              <td>
                <g:if test="${studyInstance.readers.size()==0}">
                  -
                </g:if>
                <g:else>
                  <g:each in="${studyInstance.readers}" var="r" status="i">
                    <g:if test="${i > 0}">, </g:if>
                    <g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link>
                  </g:each>
                </g:else>
              </td>
            </tr>
            <tr>
              <td>Editors</td>
              <td>
                <g:if test="${studyInstance.editors.size()==0}">
                  -
                </g:if>
                <g:else>
                  <g:each in="${studyInstance.editors}" var="r" status="i">
                    <g:if test="${i > 0}">, </g:if>
                    <g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link>
                  </g:each>
                </g:else>
              </td>
            </tr>

          </table>
        </div>

        <div id="subjects">
          <g:each in="${studyInstance.giveSubjectTemplates()}" var="template">
            <table>
              <thead>
                <tr>
                  <g:each in="${new dbnp.studycapturing.Subject().giveDomainFields()}" var="field">
                    <th>${field}</th>
                  </g:each>
                  <g:each in="${template.fields}" var="field">
                    <th>${field}</th>
                  </g:each>
                </tr>
              </thead>
              
              <%
                subjects = studyInstance.subjects.findAll {it.template == template};
                sortedSubjects = subjects.sort( { a, b -> a.name <=> b.name } as Comparator )
              %>
              <g:each in="${sortedSubjects}" var="s" status="i">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                  <g:each in="${s.giveDomainFields()}" var="field">
                    <td>${s.getFieldValue(field.name)}</td>
                  </g:each>
                  <g:each in="${template.fields}" var="field">
                    <td>
                      ${s.getFieldValue(field.name)}
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
                <thead>
                  <tr>
                    <th>Start time</th>
                    <th>Duration</th>
                    <th>Type</th>
                    <th>Sampling event</th>
                    <th>Parameters</th>
                  </tr>
                </thead>

              <%
                // Sort events by starttime and duration
                events = studyInstance.events + studyInstance.samplingEvents;
                sortedEvents = events.sort( { a, b ->
                      a.startTime == b.startTime ?
                        a.getDuration().toMilliseconds() <=> b.getDuration().toMilliseconds() :
                        a.startTime <=> b.startTime
                  } as Comparator )
              %>

                <g:each in="${sortedEvents}" var="event" status="i">
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
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
                      <g:each in="${event.giveTemplateFields()}" var="field">
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
                <td colspan="${studyInstance.giveAllEventTemplates().size()}"><b>Events</b></td>
                <td><b>Subjects</b></td>
              </tr>
              <tr>
                <td></td>
                <g:each in="${studyInstance.giveAllEventTemplates()}" var="eventTemplate">
                  <td><b>${eventTemplate.name}</b></td>
                </g:each>
                <td></td>
              </tr>
              <g:each in="${studyInstance.eventGroups}" var="eventGroup">
                <tr>
                  <td>${eventGroup.name}</td>

                  <g:each in="${studyInstance.giveAllEventTemplates()}" var="currentEventTemplate">
                    <td>
                      <g:each in="${eventGroup.events}" var="event">
                        <g:if test="${event.template.name==currentEventTemplate.name}">

                          <g:set var="fieldCounter" value="${1}" />
                          <g:each in="${event.giveTemplateFields()}" var="field">
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
                  <td>
                    <% sortedGroupSubjects = eventGroup.subjects.sort( { a, b -> a.name <=> b.name } as Comparator )  %>
                    ${sortedGroupSubjects.name.join( ', ' )}
                  </td>
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
              <thead>
                <tr>
                  <th width="100">Assay Name</th>
                  <th width="100">Module</th>
                  <th>Type</th>
                  <th width="150">Platform</th>
                  <th>Url</th>
                  <th>Samples</th>
                </tr>
              </thead>
              <g:each in="${studyInstance.assays}" var="assay" status="i">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                  <td>${assay.name}</td>
                  <td>${assay.module.name}</td>
                  <td>${assay.module.type}</td>
                  <td>${assay.module.platform}</td>
                  <td>${assay.module.url}</td>
                  <td>
                    <% sortedAssaySamples = assay.samples.sort( { a, b -> a.name <=> b.name } as Comparator )  %>
                    ${sortedAssaySamples.name.join( ', ' )}
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
                <thead>
                  <th>Name</th>
                  <th>Affiliations</th>
                  <th>Role</th>
                  <th>Phone</th>
                  <th>Email</th>
                </thead>
              </tr>
              <g:each in="${studyInstance.persons}" var="studyperson" status="i">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                  <td>${studyperson.person.firstName} ${studyperson.person.prefix} ${studyperson.person.lastName}</td>
                  <td>
                    ${studyperson.person.affiliations.join(', ')}
                  </td>
                  <td>${studyperson.role.name}</td>
                  <td>${studyperson.person.phone}</td>
                  <td>${studyperson.person.email}</td>
                </tr>
              </g:each>
            </table>
          </g:else>
        </div>

        <div id="publications">
          <g:if test="${studyInstance.publications.size()==0}">
            No publications attached to this study
          </g:if>
          <g:else>
            <table>
              <tr>
                <thead>
                  <th>Title</th>
                  <th>Authors</th>
                  <th>Comments</th>
                </thead>
              </tr>
              <g:each in="${studyInstance.publications}" var="publication" status="i">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                  <td>${publication.title}</td>
                  <td>
                    ${publication.authorlist}
                  </td>
                  <td>${publication.comment}</td>
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
        <span class="button"><g:link class="backToList" action="list">Back to list</g:link></span>
      </g:form>
    </div>
  </div>
</body>
</html>
