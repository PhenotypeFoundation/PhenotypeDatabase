package org.dbxp.sam

import org.dbnp.gdt.*
import org.apache.log4j.Logger

class Feature extends TemplateEntity {

    String name
    String unit

    static belongsTo = [platform:Platform]

    static constraints = {
        // The unit name constraint is case-sensitive.
        // Features that have the same name with a different case can still exist - it is up to the user to resolve this; a custom validator places too much of a performance impact.
        name(nullable: false, blank:false, unique: ['platform'])
        unit(nullable:true, blank:true)
    }

    public String toString() {
        return name
    }

    public String getCleanedName() {
        def name = name

        name = name.split('.v0')[0]
        name = name.split( " \\[")[0]
        name = name.replace('[','')
        name = name.replace(']','')
        name = name.replace('(','')
        name = name.replace(')','')
        name = name.replace('\\','_')
        name = name.replace('/','_')

        return name
    }

	/**
	 * Changes the template for this feature. If no template with the given name 
	 * exists, the template is set to null.
	 * @param templateName	Name of the new template
	 * @return	True if the change is successful, false otherwise
	 */
	public boolean changeTemplate( String templateName ) {
        def templateByEntityAndName
        Template.findAllByEntity(Feature).each {
            if(it.name == templateName) {
                templateByEntityAndName = it
            }
        }
        this.template = templateByEntityAndName
        return this.class == templateByEntityAndName.entity
	}
	
    /**
	 * return the domain fields for this domain class
	 * @return List
	 */
    @Override
    List<TemplateField> giveDomainFields() { return domainFields }

    static final List<TemplateField> domainFields = [
		new TemplateField(
			name: 'name',
			type: TemplateFieldType.STRING,
			preferredIdentifier: true,
			comment: 'The name of the feature',
			required: true),
		new TemplateField(
			name: 'unit',
			type: TemplateFieldType.STRING,
			comment: "The measurement unit of the feature",
			required: false)
	]


    def static delete(toDeleteList){
        def hasBeenDeletedList = []
        def return_map = [:]

        String strMessage = ""
        def lstFeatureStillReferenced = []
        boolean error = false;
        Logger log
        for( it in toDeleteList) {
            def name
            try{
                def featureInstance = Feature.get(it)
                name = featureInstance.toString()
                if(Measurement.findByFeature(featureInstance)){
                    if(error==false){
                        log = Logger.getLogger(Feature)
                        error = true
                    }
                    lstFeatureStillReferenced << name
                    continue;
                }
                featureInstance.delete(flush: true)
                hasBeenDeletedList.push(name);
            } catch(Exception e){
                if(error==false){
                    log = Logger.getLogger(Feature)
                    error = true
                } else {
                    strMessage += "<br>"
                }
                log.error e
                strMessage += "Something went wrong when trying to delete "
                if(name!=null && name!=""){
                    strMessage += name+". The probable cause is that the feature was already deleted."
                } else {
                    strMessage += "a feature. The probable cause is that the feature was already deleted."
                }
            }
        }
        if(!error){
            if(hasBeenDeletedList.size()==1){
                return_map["message"] = "The feature "+hasBeenDeletedList[0]+" has been deleted."
            } else {
                return_map["message"] = "The following features have been deleted: "+hasBeenDeletedList.toString()
            }
        } else {
            if(lstFeatureStillReferenced.size()!=0){
                strMessage += "<br>The following features cannot be deleted at this moment because they are still referenced by measurements: "+lstFeatureStillReferenced.toString()+"<br>"
            }
            if(hasBeenDeletedList.size()!=0){
                strMessage += "<br>The following features have been deleted: "+hasBeenDeletedList.toString()+"<br>"
            }
            return_map["message"] = strMessage
        }
        return_map["action"] = "list"
        return return_map
    }
}