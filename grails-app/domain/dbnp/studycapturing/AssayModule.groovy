package dbnp.studycapturing

/**
 * This entity describes actual dbNP submodule instances: what type of data they store, and how to reach them  
 */
class AssayModule {
    String name
    AssayType type
    String platform
    String url

    static constraints = {
    }
}
