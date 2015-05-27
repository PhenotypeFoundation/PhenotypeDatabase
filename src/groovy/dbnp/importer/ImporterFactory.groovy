package dbnp.importer

import dbnp.authentication.SecUser

/**
 * Defines the interface for an exporter
 */
public class ImporterFactory {
    @Lazy
    protected instances = {
       [ 
           // This list enumerates all available importers. This could be made more 
           // flexible by allowing plugins to define importers as well. However, for now
           // this works fine.
       ].collectEntries { [ (it.identifier): it ] }
    }()
    
    /**
     * Returns a specific instance
     */
    public Importer getImporter( String identifier ) { 
        instances[identifier] 
    }
    
    /**
     * Returns a list of importers that support the given type
     */
    public List getImportersForType( String type ) { 
        instances.values().findAll { it.supportsType(type) } 
    }
    
}