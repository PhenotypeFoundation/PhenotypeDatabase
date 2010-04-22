<%@ page import="dbnp.studycapturing.Study" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
  <title><g:message code="default.list.label" args="[entityName]" /></title>

  <script type="text/javascript">
    $(function() {
            $("#tabs").tabs();
    });
  </script>

</head>
<body>

  <% studyList = dbnp.studycapturing.Study.list() %>
  <% def att_list = ['template','startDate','events','samplingEvents','lastUpdated','readers','code','editors','ecCode','researchQuestion','title','description','owner','dateCreated'] %>
  <% def selectedStudies = [] %>
  <% def tmpList = [] %>


  <div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
  </div>

  <div class="body">
    <h1>Studies Comparison</h1>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>

    <% params.each{key,values-> %>
    <% if (values=="on"){ %>
      <% tmpList.add(key) %>
    <%  } }%>

    <% for (i in studyList) {%>
    <% if (tmpList.contains(i.getTitle())) { %>
      <% selectedStudies.add(i) %>
     <% }} %>

      <%
        /* Determine all template fields that are available in one or more templates used */
        def allTemplateFields = []
        selectedStudies.each { println it.giveFields(); allTemplateFields += it.giveFields() }
        allTemplateFields = allTemplateFields.unique();
      %>
     <g:if test="${selectedStudies.size()>0}">

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
      <br>
      <table>
      <tr>
        <td></td>
        <g:each in="${selectedStudies}" status="j" var="studyIns">
          <td width="50%"><b>${studyIns.title}</b></td>
        </g:each>
      </tr>
      <tr>
        <td><b>Id</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td><g:link action="show" id="${studyIns.id}">
${fieldValue(bean: studyIns, field: "id")}</g:link></td>
        </g:each>
      </tr>
      <tr>
        <td><b>Template</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td><g:link controller="template" action="show" id="${studyIns.template?.id}">
${studyInse?.template?.encodeAsHTML()}</g:link></td>
        </g:each>
      </tr>

      <tr>
        <td><b>Title</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td>${fieldValue(bean: studyIns, field: "title")}</td>
        </g:each>
      </tr>
      <tr>
        <td><b>Start</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td><g:formatDate date="${studyIns?.startDate}" /></td>
        </g:each>
      </tr>
      
      <tr>
        <td><b>Date Created</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td><g:formatDate date="${studyIns?.dateCreated}" /></td>
        </g:each>
      </tr>
      <tr>
        <td><b>Last updated</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td><g:formatDate date="${studyIns?.lastUpdated}" /></td>
        </g:each>
      </tr>

      <tr>
        <td><b>Events</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td>
            <g:if test="${studyIns.giveEventTemplates().size()==0}">
              -
            </g:if>
            <g:else>
             ${studyIns.giveEventTemplates().name}
            </g:else>
          </td>
        </g:each>
      </tr>
      <tr>
        <td><b>Sampling Events</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td>
            <g:if test="${studyIns.giveSamplingEventTemplates().size()==0}">
              -
            </g:if>
            <g:else>
             ${studyIns.giveSamplingEventTemplates().name}
            </g:else>
          </td>
        </g:each>
      </tr>
      <tr>
        <td><b>Owner</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td>
            <g:link controller="user" action="show" id="${studyIns?.owner?.id}">${studyIns?.owner?.encodeAsHTML()}</g:link>
          </td>
        </g:each>
      </tr>

      <tr>
        <td><b>Readers</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td>
            <g:if test="${studyIns.readers.size()==0}">
              -
            </g:if>
            <g:else>
              <g:each in="${studyIns.readers}" var="r">
                <li><g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
              </g:each>
            </g:else>
          </td>
        </g:each>
      </tr>
      <tr>
        <td><b>Editors</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td>
            <g:if test="${studyIns.editors.size()==0}">
              -
            </g:if>
            <g:else>
              <g:each in="${studyIns.editors}" var="r">
                <li><g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
              </g:each>
            </g:else>
          </td>
        </g:each>
      </tr>

      <!-- All template fields -->
      <g:each in="${allTemplateFields}" var="field">
        <tr>
          <td><b>${field.name}</b></td>
          <g:each in="${selectedStudies}" status="k" var="studyIns">
            <td>
              <g:if test="${studyIns.fieldExists(field.name)}">
                ${studyIns.getFieldValue(field.name)}
              </g:if>
              <g:else>
                -
              </g:else>
            </td>
          </g:each>
        </tr>
      </g:each>

      </table>
    </div>

          <div id="subjects">
       
       <table border="2">
         <tr>
         <g:each in="${selectedStudies}" var="study">
           <td><center><b>${study.title}</b></center></td>
         </g:each>
       </tr>

         <tr>
         <g:each in="${selectedStudies}" var="stud">
             <td>


  <g:each in="${stud.giveSubjectTemplates()}" var="template">
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

          <g:each in="${stud.subjects.findAll { it.template == template}}" var="s">
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



             </td>
           </g:each>
         </tr>
       </table>
       
      </div>

          <div id="events">
          <table border="2">
            <tr>
              <g:each in="${selectedStudies}" var="study">
                <td><center><b>${study.title}</b></center></td>
              </g:each>
            </tr>

            <tr>
              <g:each in="${selectedStudies}" var="study">
                <td>

                  <g:if test="${study.events.size()+study.samplingEvents.size()==0}">
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

                        <g:each in="${study.events + study.samplingEvents}" var="event">
                          <tr>
                            <td><g:link controller="event" action="show" id="${event.id}">${event.id}</g:link></td>
                            <td>${event.getPrettyDuration(study.startDate,event.startTime)}</td>
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

                </td>
              </g:each>
            </tr>
          </table>

      </div>
  
        <div id="event-group">
          <table border="2">
            <tr>
              <g:each in="${selectedStudies}" var="study">
                <td><center><b>${study.title}</b></center></td>
              </g:each>
            </tr>

            <tr>
              <g:each in="${selectedStudies}" var="study">

                <g:if test="${study.eventGroups.size()==0}">
                  No event groups in this study
                </g:if>
                <g:else>
                  <table>
                    <tr>
                      <td><b>Name</b></td>
                      <td colspan="${study.giveEventTemplates().size()}"><b>Events</b></td>
                      <td><b>Subjects</b></td>
                    </tr>
                    <tr>
                      <td></td>
                      <g:each in="${study.giveEventTemplates()}" var="eventTemplate">
                        <td><b>${eventTemplate.name}</b></td>
                      </g:each>
                      <td></td>
                    </tr>
                    <g:each in="${study.eventGroups}" var="eventGroup">
                      <tr>
                        <td>${eventGroup.name}</td>

                        <g:each in="${study.giveEventTemplates()}" var="currentEventTemplate">
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

              </g:each>
            </tr>
          </table>
        </div>

          <div id="assays">
   <table border="2">
  <tr>
         <g:each in="${selectedStudies}" var="study">
          <td><center><b>${study.title}</b></center></td>
         </g:each>
  </tr>
   <tr>
         <g:each in="${selectedStudies}" var="stud">
           <td>
           <table>
             <tr><td>
  <g:if test="${stud.assays.size()==0}">
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
          <g:each in="${stud.assays}" var="assay">
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


  </table>
        </td>
           </g:each>
         </tr>
       </table>

      </div>


      <div id="persons">
        <table border="2">
          <tr>
            <g:each in="${selectedStudies}" var="study">
              <td><center><b>${study.title}</b></center></td>
            </g:each>
          </tr>
          <tr>
            <g:each in="${selectedStudies}" var="study">
              <td>
                <g:if test="${study.persons.size()==0}">
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
                    <g:each in="${study.persons}" var="studyperson">
                      <tr>
                        <td>${studyperson.person.firstName} ${studyperson.person.midInitials} ${studyperson.person.lastName}</td>
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
              </td>
           </g:each>
         </tr>
       </table>

      </div>

    </div>

    </g:if>
    
    <g:if test="${selectedStudies.size()==0}">
    Please select studies to compare.
    </g:if>

    </div>


</body>
</html>
