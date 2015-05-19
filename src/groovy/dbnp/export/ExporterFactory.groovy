package dbnp.export

import dbnp.authentication.SecUser

/**
 * Defines the interface for an exporter
 */
public class ExporterFactory {
    @Lazy
    protected instances = {
       [ 
           // This list enumerates all available exporters. This could be made more 
           // flexible by allowing plugins to define exporters as well. However, for now
           // this works fine.
           new SimpleToxExporter(),
           new MultipleStudiesExporter(),
           new AssayDataExporter()
       ].collectEntries { [ (it.identifier): it ] }
    }()
    
    /**
     * Returns a specific instance
     */
    public Exporter getExporter( String identifier, SecUser user = null ) { 
        def exporter = instances[identifier] 
    
        if( exporter && user )
            exporter.user = user
            
        exporter    
    }
    
    /**
     * Returns a list of exporters that support the given type
     */
    public List getExportersForType( String type ) { 
        instances.values().findAll { it.type == type } 
    }
    
}