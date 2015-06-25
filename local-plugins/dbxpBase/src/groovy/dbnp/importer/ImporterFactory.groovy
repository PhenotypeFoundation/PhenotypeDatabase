package dbnp.importer

import dbnp.authentication.SecUser
import dbnp.importer.impl.*
import org.dbxp.sam.importer.*

/**
 * Defines the interface for an exporter
 */
public class ImporterFactory {
    protected static final ImporterFactory instance = new ImporterFactory()
    
    public static ImporterFactory getInstance() {
        instance
    }
    
    protected instances = [:]

    /**
     * Register a new importer with the factory
     */
    public void register(Importer importer) {
        instances[importer.identifier] = importer
    }
    
    /**
     * Returns a specific instance
     */
    public Importer getImporter( String identifier, SecUser user = null ) {
        def importer = instances[identifier]
        
        if( importer && user )
            importer.user = user
            
        importer
    }
    
    /**
     * Returns a list of importers that support the given type
     */
    public List getImportersForType( String type ) { 
        instances.values().findAll { it.supportsType(type) } 
    }
    
    /**
     * Returns a list of all importers
     */
    public List getAllImporters() {
        instances.values().toList()
    }
    
}