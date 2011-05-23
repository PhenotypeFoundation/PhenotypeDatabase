<%@ page import="dbnp.studycapturing.SamplingEvent" %>
<%
/**
 * Events page
 *
 * @author  Jeroen Wesbeek
 * @since   20100212
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib::previousNext
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<af:page>
	<span class="info">
		<span class="title">Define all events that occur in your study</span>
		An event is any change ‘forced’ upon a subject, such as treatment, challenge, sampling. Choose an event type an define the different parameters of the event.		
	</span>

	<af:radioElement name="eventType" description="Choose the type of event" elements="[event:'treatment, challenge, etc.',sample:'sampling event']" class="eventradio" elementclass="label_radio" value="${values?.eventType}">
		The type of event can be either a sampling event (e.g. taking a sample) or anything that acts upon a subject (e.g. a treatment or a challenge)
	</af:radioElement>
	<af:templateElement required="true" name="eventTemplate" elementId="eventTemplate" description="Event Template" value="${event?.template}" entity="${dbnp.studycapturing.Event}" addDummy="true" ajaxOnChange="switchTemplate" afterSuccess="onPage()" >
		The template to use for this event
	</af:templateElement>
	<af:templateElement  required="true" name="sampleTemplate" elementId="sampleTemplate" description="Sampling Event Template" value="${event?.template}" entity="${dbnp.studycapturing.SamplingEvent}" addDummy="true" ajaxOnChange="switchTemplate" afterSuccess="onPage()" >
		The template to use for this sampling event
	</af:templateElement>
	<g:if test="${event?.template}">
	<div id="${values?.eventType}TemplateFields">
	<g:if test="${event?.template}"><af:templateElements entity="${event}" /></g:if>
	<g:if test="${event?.template}"><af:buttonElement name="add" value="Add" afterSuccess="onPage()"/></g:if>
	</div>
	</g:if>
	
	<script type="text/javascript">
	function swapTemplate(value,refresh) {
		$("div[id$='Template'],div[id$='TemplateFields']").each(function() {
			var e = $(this);
			if (e.attr('id').match("^"+value) != null) {
				e.show();
			} else {
				e.hide();
			}
		});

		if(refresh) {
		}
	}

	// handle template selectors
	$(document).ready(function() {
		// bind event handlers
		$("input[name=eventType]").click(function() {
			swapTemplate($(this).val(),true);
		});

		// handle selects
		swapTemplate($('input:radio[name=eventType]:checked').val(),false);

		// ignore enter key in event group name text fields
		$('input[name^="eventGroup"]').bind('keydown',function(event) {
		  return (event.keyCode != 13);
		});

		$('body').addClass('has-js');
		$('.label_check, .label_radio').click(function(){
			setupLabel();
		});
		setupLabel();
	});

    function setupLabel() {
        if ($('.label_check input').length) {
            $('.label_check').each(function(){
                $(this).removeClass('c_on');
            });
            $('.label_check input:checked').each(function(){
                $(this).parent('label').addClass('c_on');
            });
        }
        if ($('.label_radio input').length) {
            $('.label_radio').each(function(){
                $(this).removeClass('r_on');
            });
            $('.label_radio input:checked').each(function(){
                $(this).parent('label').addClass('r_on');
            });
        }
    }
	</script>

	<g:if test="${study.events || study.samplingEvents}">
		<g:each var="template" in="${study.giveAllEventTemplates()}">
			<g:set var="showHeader" value="${true}" />
			<h1>${template} (<g:if test="${template.entity == SamplingEvent}">sampling event</g:if><g:else>treatment, challenge, etc.</g:else>)</h1>
			<div class="tableEditor">
			<g:each var="event" in="${study.giveEventsForTemplate(template)}">
				<g:if test="${showHeader}">
				<g:set var="showHeader" value="${false}" />
				<div class="header">
					<div class="firstColumn"></div>
					<g:if test="${study.eventGroups}"><g:each var="eventGroup" in="${study.eventGroups}">
					<div class="column">
						<g:textField name="eventGroup_${eventGroup.getIdentifier()}_${template.getIdentifier()}" value="${eventGroup.name}" />
						<af:ajaxButton name="deleteEventGroup" src="${resource(dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam')}" alt="delete this eventgroup" class="famfamfam" value="-" before="\$(\'input[name=do]\').val(${eventGroup.getIdentifier()});" afterSuccess="onPage()" />
					</div>
					</g:each></g:if>
					<div class="firstColumn">
						<af:ajaxButton name="addEventGroup" src="${resource(dir: 'images/icons', file: 'add.png', plugin: 'famfamfam')}" alt="add a new eventgroup" class="famfamfam" value="+" afterSuccess="onPage()" />
					</div>
				  <af:templateColumnHeaders class="column" entity="${event}" />
				</div>
				</g:if>

				<div class="row" identifier="${event.getIdentifier()}">
					<div class="firstColumn">
						<input type="button" value="" action="deleteEvent" class="delete" identifier="${event.getIdentifier()}" />
					</div>
					<g:if test="${study.eventGroups}"><g:each var="eventGroup" in="${study.eventGroups}">
					<div class="column">
						<g:if test="${eventGroup.events.find{ it == event } || eventGroup.samplingEvents.find{ it == event }}">
							<input type="checkbox" name="event_${event.getIdentifier()}_group_${eventGroup.getIdentifier()}" checked="checked" />
						</g:if><g:else>
							<input type="checkbox" name="event_${event.getIdentifier()}_group_${eventGroup.getIdentifier()}"/>
						</g:else>
					</div>
					</g:each></g:if>
					<div class="firstColumn">
						<input type="button" value="" action="duplicate" class="clone" identifier="${event.getIdentifier()}" />
					</div>
					<af:templateColumns class="column" entity="${event}" name="event_${event.getIdentifier()}" />
				</div>

			</g:each>
			</div>
		</g:each>
	</g:if>

	<script type="text/javascript">
	$(document).ready(function() {
		if (tableEditor) {
			tableEditor.registerActionCallback('deleteEvent', function() {
				if (confirm('are you sure you want to delete ' + ((this.length>1) ? 'these '+this.length+' events?' : 'this event?'))) {
					$('input[name="do"]').val(this);
					<af:ajaxSubmitJs name="deleteEvent" afterSuccess="onPage()" />
				}
			});
			tableEditor.registerActionCallback('duplicate', function() {
				$('input[name="do"]').val(this);
				<af:ajaxSubmitJs name="duplicate" afterSuccess="onPage()" />
			});
		}
	});
	</script>
</af:page>