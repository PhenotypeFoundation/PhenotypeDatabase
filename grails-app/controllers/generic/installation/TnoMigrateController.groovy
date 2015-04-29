package generic.installation

import dbnp.studycapturing.*
import grails.converters.JSON
import org.dbnp.gdt.Template

/**
 * User: Seth
 * Date: 28-11-13
 * Time: 12:10
 */
class TnoMigrateController {
    def dataSource

    def migrateStudy() {
        def study = Study.read( params.id )

        def sql = new groovy.sql.Sql(dataSource)

        def allEventGroups = []

        def oldEventGroupIdList = []
        def oldEventIdList = []
        def oldSamplingEventIdList = []

        def newSubjectGroupIdList  = []
        def newEventIdList = []
        def newSamplingEventIdList = []

        //Creation of subjectGroups
        def oldEventGroups = sql.rows("SELECT id, name FROM event_group WHERE parent_id = ${study.id}")

        oldEventGroups.each() { oldEventGroup ->
            oldEventGroupIdList << oldEventGroup.id

            def subjectGroup = new SubjectGroup( name: oldEventGroup.name, parent: study )
            subjectGroup.parent = study

            //Get subjects for event_group
            sql.rows("SELECT subject_id FROM event_group_subject WHERE event_group_subjects_id = ${oldEventGroup.id}").collect() { it.subject_id }.each() { subjectId ->
                def subject = Subject.read( subjectId)
                subjectGroup.addToSubjects( subject )
            }

            study.addToSubjectGroups( subjectGroup )
            study.save( flush: true, failOnError: true )
            subjectGroup.save( flush: true, failOnError: true )

            newSubjectGroupIdList  << subjectGroup.id

            def oldEvents = sql.rows("SELECT id, start_time, end_time, template_id FROM event WHERE id IN (SELECT event_id FROM event_group_event WHERE event_group_events_id = ${oldEventGroup.id})")
            oldEvents.each() { oldEvent ->
                oldEventIdList << oldEvent.id

                def eventName = sql.rows("SELECT template_string_fields_elt FROM event_template_string_fields WHERE event_id = ${oldEvent.id} AND template_string_fields_idx ='Event name (STRING)'").template_string_fields_elt[0]
                def migration = sql.rows("SELECT template_string_fields_elt FROM event_template_string_fields WHERE event_id = ${oldEvent.id} AND template_string_fields_idx ='Migration'").template_string_fields_elt[0]

                EventGroup eventGroup = allEventGroups.find() { it.name.equalsIgnoreCase(eventName) }
                if (!eventGroup) {
                    eventGroup = new EventGroup( name: eventName, parent: study )
                    study.addToEventGroups( eventGroup )
                    eventGroup.save( flush: true, failOnError: true )

                    allEventGroups << eventGroup
                }

                SubjectEventGroup subjectEventGroup = new SubjectEventGroup( study: study, SubjectGroup: subjectGroup, eventGroup: eventGroup, startTime: oldEvent.start_time )
                study.addToSubjectEventGroups(subjectEventGroup)
                subjectGroup.addToSubjectEventGroups(subjectEventGroup)
                eventGroup.addToSubjectEventGroups(subjectEventGroup)
                subjectEventGroup.save( flush: true, failOnError: true )

                def event = Event.findByParentAndName(study, eventName)
                if (!event) {
                    event = new Event( name: eventName, parent: study, template: Template.read( oldEvent.template_id ) )
                    study.addToEvents( event )
                    event.save( flush: true, failOnError: true )

                    newEventIdList << event.id
                }

                def eventRelativeStartTime = (oldEvent.start_time - subjectEventGroup.startTime)
                def eventDuration = (oldEvent.end_time - oldEvent.start_time)

                def eventInEventGroup = EventInEventGroup.findByEventGroupAndEventAndStartTimeAndDuration(eventGroup, event, eventRelativeStartTime, eventDuration)
                if (!eventInEventGroup) {
                    eventInEventGroup = new EventInEventGroup( startTime: eventRelativeStartTime, duration: eventDuration, event: event, eventGroup: eventGroup ).save( flush: true, failOnError: true )
                    eventGroup.addToEventInstances(eventInEventGroup)
                }
            }

            def oldSamplingEvents = sql.rows("SELECT id, start_time, duration, template_id, sample_template_id FROM sampling_event WHERE id IN (SELECT sampling_event_id FROM event_group_sampling_event WHERE event_group_sampling_events_id = ${oldEventGroup.id})")
            oldSamplingEvents.each() { oldSamplingEvent ->
                oldSamplingEventIdList << oldSamplingEvent.id

                def oldSamplingEventDetails = sql.rows("SELECT setsf.template_string_fields_elt, setrtf.template_rel_time_fields_elt FROM sampling_event_template_string_fields setsf, sampling_event_template_rel_time_fields setrtf WHERE setsf.sampling_event_id = ${oldSamplingEvent.id} AND setrtf.sampling_event_id = ${oldSamplingEvent.id} AND setsf.template_string_fields_idx = 'Migration' AND setrtf.template_rel_time_fields_idx = 'Relative time in related event'" )
                def migration = oldSamplingEventDetails.template_string_fields_elt[0]
                def relativeTimeField = oldSamplingEventDetails.template_rel_time_fields_elt[0]

                migration.split(';').each() { eventName ->
                    EventGroup eventGroup = allEventGroups.find() { it.name.equalsIgnoreCase(eventName) }

                    if (eventGroup) {
                        def correspondingSubjectEventGroup = eventGroup.subjectEventGroups.find() { it.startTime <= (oldSamplingEvent.start_time - relativeTimeField) && it.endTime >= ((oldSamplingEvent.start_time - relativeTimeField) + oldSamplingEvent.duration) }
                        if (correspondingSubjectEventGroup) {
                            def template = Template.read( oldSamplingEvent.template_id )

                            //Set template name as samplingEvent name
                            def samplingEventName = template.name

                            def samplingEvent = SamplingEvent.findByParentAndName(study, samplingEventName)
                            if (!samplingEvent) {
                                def sampleTemplate = Template.read( oldSamplingEvent.sample_template_id )
                                samplingEvent = new SamplingEvent( name: samplingEventName, parent: study, template: template, sampleTemplate: sampleTemplate )
                                study.addToSamplingEvents( samplingEvent )
                                samplingEvent.getRequiredFields().each() {
                                    samplingEvent.setFieldValue(it.name, SamplingEvent.read(oldSamplingEvent.id).getFieldValue(it.name))
                                }
                                samplingEvent.save( flush: true )

                                newSamplingEventIdList << samplingEvent.id
                            }

                            def relativeStartTime = ( (long) oldSamplingEvent.start_time - (long) correspondingSubjectEventGroup.startTime )

                            def samplingEventInEventGroup = SamplingEventInEventGroup.findByEventGroupAndEventAndStartTimeAndDuration( eventGroup, samplingEvent, relativeStartTime, oldSamplingEvent.duration )
                            if (!samplingEventInEventGroup) {
                                samplingEventInEventGroup = new SamplingEventInEventGroup( startTime: relativeStartTime, duration: oldSamplingEvent.duration, event: samplingEvent, eventGroup: eventGroup ).save( flush: true, failOnError: true )
                                eventGroup.addToSamplingEventInstances(samplingEventInEventGroup)
                            }
                        }
                    }
                    else {
                        println "No eventGroup found for migration column value ${eventName}"
                    }
                }
            }
        }

        redirect controller: 'studyEditDesign', params: [ id: study.id, migrateDesign: [ oldEventGroupIdList: oldEventGroupIdList, oldEventIdList: oldEventIdList, oldSamplingEventIdList: oldSamplingEventIdList, newEventGroupIdList: allEventGroups.id, newSubjectGroupIdList: newSubjectGroupIdList, newEventIdList: newEventIdList, newSamplingEventIdList: newSamplingEventIdList ] ]
    }

    def deleteOldDesignAndLinkSamples() {
        def migrateDesign = JSON.parse(params.migrateDesign)

        migrateDesign['oldEventGroupIdList'].each() { oldEventGroupId ->
        }

        migrateDesign['oldEventIdList'].each() { oldEventId ->
        }

        migrateDesign['oldSamplingEventIdList'].each() { oldSamplingEventId ->
        }

        redirect controller: 'studyEditDesign', id: params.id
    }

    def deleteNewDesign() {
        def migrateDesign = JSON.parse(params.migrateDesign)

        def study = Study.read( params.id )

        migrateDesign['newEventIdList'].each() { newEventId ->
            deleteNewDesignEvent( Event.read( newEventId ) )
        }
        migrateDesign.remove('newEventIdList')
        study.save( flush: true )

        migrateDesign['newSamplingEventIdList'].each() { newSamplingEventId ->
            deleteNewDesignSamplingEvent( SamplingEvent.read( newSamplingEventId ) )
        }
        migrateDesign.remove('newSamplingEventIdList')
        study.save( flush: true )

        if( migrateDesign['newSubjectGroupIdList'].size() > 0 ) {
            study.deleteSubjectGroup( SubjectGroup.read( migrateDesign['newSubjectGroupIdList'][0]) )
            migrateDesign['newSubjectGroupIdList'].remove( 0 )
            redirect( action: 'deleteNewDesign', params: [ id: params.id, migrateDesign: migrateDesign ] )
            return
        }

        if( migrateDesign['newEventGroupIdList'].size() > 0 ) {
            study.deleteEventGroup( EventGroup.read( migrateDesign['newEventGroupIdList'][0]) )
            migrateDesign['newEventGroupIdList'].remove( 0 )
            redirect( action: 'deleteNewDesign', params: [ id: params.id, migrateDesign: migrateDesign ] )
            return
        }

        redirect controller: 'studyEditDesign', id: params.id
    }

    private deleteNewDesignEvent( Event event ) {
        // remove event from eventGroups
        ( [] + event.eventGroupInstances ).each { eventGroupInstance ->
            eventGroupInstance.eventGroup.removeFromEventInstances( eventGroupInstance )
            eventGroupInstance.event.removeFromEventGroupInstances( eventGroupInstance )

            eventGroupInstance.delete()
        }

        // remove event from the study
        event.parent.removeFromEvents(event)

        // and perform a hard delete
        event.delete( flush: true )
    }

    private deleteNewDesignSamplingEvent( SamplingEvent samplingEvent ) {
        // remove event from eventGroups
        ( [] + samplingEvent.eventGroupInstances ).each { eventGroupInstance ->
            eventGroupInstance.eventGroup.removeFromSamplingEventInstances( eventGroupInstance )
            eventGroupInstance.event.removeFromEventGroupInstances( eventGroupInstance )

            eventGroupInstance.delete()
        }

        // Remove event from the study
        // This should remove the event group itself too, because of the cascading belongsTo relation
        samplingEvent.parent.removeFromSamplingEvents(samplingEvent)

        // But apparently it needs an explicit delete() too
        // (Which can be verified by outcommenting this line, then SampleTests.testDeleteViaParentSamplingEvent fails
        samplingEvent.delete( flush: true )
    }
}