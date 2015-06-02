package generic.installation

import dbnp.studycapturing.*
import org.dbnp.gdt.RelTime
import org.dbnp.gdt.Template

class TnoMigrateController {
    def dataSource

    def migrateStudy() {
        session.removeAttribute('migrateDesign')

        def study = Study.read( params.id )

        def sql = new groovy.sql.Sql(dataSource)

        def allEventGroups = []

        def oldEventGroupIdList = []
        def oldEventIdList = []
        def oldSamplingEventIdList = []

        def newSubjectGroupIdList  = []
        def newEventIdList = []
        def newSamplingEventIdList = []

        //oldId:newId
        def sampleMigration = [:]

        def allOldSamplingEvents = sql.rows("SELECT id FROM sampling_event WHERE parent_id = ${study.id}").collectAll() { it.id }
        def allOldSamples = sql.rows("SELECT id FROM sample WHERE parent_id = ${study.id}").collectAll() { it.id }

        def subjectEventGroupConflicts = []
        def eventGroupConflicts = []

        //Creation of subjectGroups
        def oldEventGroups = sql.rows("SELECT id, name FROM event_group WHERE parent_id = ${study.id}")

        oldEventGroups.each() { oldEventGroup ->
            oldEventGroupIdList << oldEventGroup.id

            def subjectGroup = new SubjectGroup( name: oldEventGroup.name, parent: study )
            subjectGroup.parent = study

            //Get subjects for event_group
            sql.rows("SELECT subject_id FROM event_group_subject WHERE event_group_subjects_id = ${oldEventGroup.id}").collect() { it.subject_id }.each() { subjectId ->
                def subject = Subject.findById( subjectId )
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

                if (migration.split(';').size() != 1) {
                    flash.error = "Events should have one item in migration column, not multiple"
                    redirect controller: 'studyEditDesign', action: 'index', params: [ id: study.id ]
                    return
                }

                if (!eventName.equalsIgnoreCase(migration)) {
                    println "EventName field (${eventName}) not matching migration field (${migration}). Using ${migration}"
                    eventName = migration
                }

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

                    def eventGroups = allEventGroups.findAll() { it.name.equalsIgnoreCase(eventName) }

                    if (eventGroups.size() == 1) {
                        def eventGroup = eventGroups[0]

//                        def correspondingSubjectEventGroups = eventGroup.subjectEventGroups.findAll() { it.subjectGroup.id == subjectGroup.id && it.startTime <= (oldSamplingEvent.start_time - relativeTimeField) && it.endTime >= ((oldSamplingEvent.start_time - relativeTimeField) + oldSamplingEvent.duration) }
                        def correspondingSubjectEventGroups = SubjectEventGroup.findAllByParentAndSubjectGroupAndEventGroupAndStartTimeLessThanEquals(study, subjectGroup, eventGroup, (oldSamplingEvent.start_time - relativeTimeField)).findAll() { it.endTime >= ((oldSamplingEvent.start_time - relativeTimeField) + oldSamplingEvent.duration) }
                        if (correspondingSubjectEventGroups.size() == 1) {

                            def correspondingSubjectEventGroup = correspondingSubjectEventGroups[0]
                            def template = Template.read( oldSamplingEvent.template_id )

                            //Set template name as samplingEvent name
                            def samplingEventName = template.name

                            def samplingEvent = SamplingEvent.findByParentAndName(study, samplingEventName)
                            if (!samplingEvent) {
                                def sampleTemplate = Template.read( oldSamplingEvent.sample_template_id )
                                samplingEvent = new SamplingEvent( name: samplingEventName, parent: study, template: template, sampleTemplate: sampleTemplate )
                                study.addToSamplingEvents( samplingEvent )
                                samplingEvent.getRequiredFields().each() {
                                    samplingEvent.setFieldValue(it.name, SamplingEvent.findById(oldSamplingEvent.id).getFieldValue(it.name))
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

                            def samples = sql.rows("SELECT id FROM sample WHERE parent_event_id = ${oldSamplingEvent.id} AND parent_event_group_id = ${oldEventGroup.id}").collectAll() { it.id }

                            samples.each() { sampleId ->
                                sampleMigration[sampleId] = [ correspondingSubjectEventGroup.id, samplingEventInEventGroup.id ]
                            }
                        }
                        else {
                            if (correspondingSubjectEventGroups.size() > 1) {
                                println "Multiple corresponding SubjectEventGroups for eventGroup ${eventName}"
                                subjectEventGroupConflicts << eventName
                            }
                        }
                    }
                    else {
                        if (eventGroups.size() > 1) {
                            println "Multiple eventGroups found for name ${eventName}"
                            eventGroupConflicts << eventName
                        }
                    }
                }
            }
        }

        def lostSamplingEvents = allOldSamplingEvents - oldSamplingEventIdList
        def lostSamples = allOldSamples - sampleMigration.keySet()

        if (subjectEventGroupConflicts.size() != 0 || eventGroupConflicts.size() != 0 || lostSamplingEvents.size() != 0 || lostSamples.size() != 0) {
            flash.error = "Lost SamplingEvents (${lostSamplingEvents.size()}): ${lostSamplingEvents.join(', ')} | Lost Samples (${lostSamples.size()}): ${lostSamples.join(', ')} | eventGroupConflicts: ${eventGroupConflicts.unique().join(', ')} | subjectEventGroupConflicts: ${subjectEventGroupConflicts.unique().join(', ')}"
        }

        session.migrateDesign = [ studyId: study.id, oldEventGroupIdList: oldEventGroupIdList.unique(), oldEventIdList: oldEventIdList.unique(), oldSamplingEventIdList: oldSamplingEventIdList.unique(), newEventGroupIdList: allEventGroups.id, newSubjectGroupIdList: newSubjectGroupIdList, newEventIdList: newEventIdList, newSamplingEventIdList: newSamplingEventIdList, sampleMigration: sampleMigration ]

        redirect controller: 'studyEditDesign', action: 'index', params: [ id: study.id ]
    }

    def deleteOldDesignAndLinkSamples() {
        session.migrateDesign['step'] = 3

        def study = Study.read( params.id )
        def sql = new groovy.sql.Sql(dataSource)

        def migrateDesign = session.migrateDesign

        migrateDesign['oldEventIdList'].each() { oldEventId ->
            def event = Event.read( oldEventId )

            if ( event ) {
                sql.execute("DELETE FROM event_group_event WHERE event_id = ${oldEventId}")

                study.removeFromEvents( event )
                study.save( flush: true )

                event.delete( flush: true, failOnError: true )
            }
        }

        migrateDesign['oldSamplingEventIdList'].each() { oldSamplingEventId ->
            def samplingEvent = SamplingEvent.read( oldSamplingEventId )

            if ( samplingEvent ) {
                sql.execute("DELETE FROM event_group_sampling_event WHERE sampling_event_id = ${oldSamplingEventId}")

                study.removeFromSamplingEvents( samplingEvent )
                study.save( flush: true )
                samplingEvent.delete( flush: true )
            }
        }

        migrateDesign['sampleMigration'].each() { sampleId, remap ->
            sql.execute("UPDATE sample SET parent_event_group_id = null WHERE id = ${sampleId as Long}")
            sql.execute("UPDATE sample SET parent_subject_event_group_id = ${remap[0]} WHERE id = ${sampleId as Long}")
            sql.execute("UPDATE sample SET parent_event_id = ${remap[1]} WHERE id = ${sampleId as Long}")
        }

        migrateDesign['oldEventGroupIdList'].each() { oldEventGroupId ->
            def eventGroup = EventGroup.read( oldEventGroupId )

            if ( eventGroup ) {
                sql.execute("DELETE FROM event_group_subject WHERE event_group_subjects_id = ${oldEventGroupId}")
                sql.execute("DELETE FROM event_group_sampling_event WHERE event_group_sampling_events_id = ${oldEventGroupId}")

                study.removeFromEventGroups( eventGroup )
                study.save( flush: true )
                eventGroup.delete( flush: true )
            }
        }

        session.removeAttribute('migrateDesign')

        redirect controller: 'studyEditDesign', id: params.id
    }

    def deleteNewDesign() {
        def migrateDesign = session.migrateDesign

        def study = Study.read( params.id )

        migrateDesign['newEventIdList'].each() { newEventId ->
            def event = Event.read( newEventId )

            ( [] + event.eventGroupInstances ).each { eventGroupInstance ->
                eventGroupInstance.eventGroup.removeFromEventInstances( eventGroupInstance )
                eventGroupInstance.event.removeFromEventGroupInstances( eventGroupInstance )

                eventGroupInstance.delete( flush: true )
            }

            study.removeFromEvents( event )
            study.save( flush: true )

            event.delete( flush: true )
        }
        migrateDesign.remove('newEventIdList')

        migrateDesign['newSamplingEventIdList'].each() { newSamplingEventId ->
            def samplingEvent = SamplingEvent.read( newSamplingEventId )

            ( [] + samplingEvent.eventGroupInstances ).each { eventGroupInstance ->
                eventGroupInstance.eventGroup.removeFromSamplingEventInstances( eventGroupInstance )
                eventGroupInstance.event.removeFromEventGroupInstances( eventGroupInstance )

                eventGroupInstance.delete( flush: true )
            }

            study.removeFromSamplingEvents( samplingEvent )
            study.save( flush: true )

            samplingEvent.delete( flush: true )
        }
        migrateDesign.remove('newSamplingEventIdList')

        if( migrateDesign['newSubjectGroupIdList'].size() > 0 ) {
            study.deleteSubjectGroup( SubjectGroup.read( migrateDesign['newSubjectGroupIdList'][0]) )
            study.save( flush: true )

            migrateDesign['newSubjectGroupIdList'].remove( 0 )
            redirect( action: 'deleteNewDesign', params: [ id:  study.id ] )
            return
        }

        if( migrateDesign['newEventGroupIdList'].size() > 0 ) {
            study.deleteEventGroup( EventGroup.read( migrateDesign['newEventGroupIdList'][0]) )
            study.save( flush: true )

            migrateDesign['newEventGroupIdList'].remove( 0 )
            redirect( action: 'deleteNewDesign', params: [ id: study.id ] )
            return
        }

        session.removeAttribute('migrateDesign')

        redirect controller: 'studyEditDesign', id: study.id
    }

    def quit() {

        session.removeAttribute('migrateDesign')

        redirect controller: 'studyEditDesign', action: 'index', params: [ id: params.id ]
    }
}