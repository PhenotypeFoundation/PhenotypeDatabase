modules = {
    sam2 {
        dependsOn 'jquery'
        dependsOn 'datatables'  // from dbxpModuleBase
        dependsOn 'infoboxes'
        
        resource url: [dir: 'js', file: 'selectAddMore.js', plugin: 'dbxp-sam']
        resource url: [dir: 'js', file: 'removeWebFlowExecutionKey.js', plugin: 'dbxp-sam']
        resource url: [dir: 'css', file: 'sam.css', plugin: 'dbxp-sam']
        resource url: [dir: 'css', file: 'tooltip.css', plugin: 'dbxp-sam']
        resource url: [dir: 'images', file: 'subjectlayout.png', plugin: 'dbxp-sam'], attrs:[alt:''], disposition:'inline'
        resource url: [dir: 'images', file: 'samplelayout.png', plugin: 'dbxp-sam'], attrs:[alt:''], disposition:'inline'
        resource url: [dir: 'images', file: 'spinner.gif', plugin: 'dbxp-sam'], attrs:[alt:''], disposition:'inline'
        resource url: [ dir:'js', file: 'jquery-migrate-1.2.1.js'], linkOverride: '//code.jquery.com/jquery-migrate-1.2.1.js'
    }
    
    samdialog {
        dependsOn 'jquery'
        
        resource url: [dir: 'js', file: 'SelectAddMore.js', plugin: 'gdt']
        resource url: [dir: 'css', file: 'templateEditor.css', plugin: 'gdt']
        resource url: [dir: 'css', file: 'dialog.css']
    }

    tableEditor {
        dependsOn 'templateFieldsMisc'
        resource url:[plugin: 'gdt', dir:'js', file: 'table-editor.js']
        resource url:[plugin: 'gdt', dir:'css', file: 'table-editor.css', disposition: 'head']
    }

    importer {
        resource url: [dir: 'js', file: 'samimporter.js', plugin: 'dbxp-sam']
        resource url: [dir: 'css', file: 'samimporter.css', plugin: 'dbxp-sam']
    }

    templateFieldsMisc {
        dependsOn 'samtooltips'
        dependsOn 'studywizard-files'
        resource url: [dir: 'js', file: 'templateFields.js', plugin: 'dbxp-sam']
    }

    samtooltips {
        resource url:[plugin: 'gdt', dir:'js', file: 'tooltips.js']
        resource url:[plugin: 'gdt', dir:'js', file: 'jquery.qtip-1.0.0-rc3.js']
    }
}
