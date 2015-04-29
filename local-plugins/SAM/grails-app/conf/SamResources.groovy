modules = {
    sam2 {
        dependsOn 'jquery'
        dependsOn 'datatables'  // from dbxpModuleBase

        resource url: 'js/selectAddMore.js'
        resource url: 'js/removeWebFlowExecutionKey.js'
        resource url: 'css/sam.css'
        resource url: 'css/tooltip.css'
        resource url:'/images/subjectlayout.png', attrs:[alt:''], disposition:'inline'
        resource url:'/images/samplelayout.png', attrs:[alt:''], disposition:'inline'
        resource url:'/images/spinner.gif', attrs:[alt:''], disposition:'inline'
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
        resource url: 'js/samimporter.js'
        resource url: 'css/samimporter.css'
    }

    templateFieldsMisc {
        dependsOn 'samtooltips'
        dependsOn 'studywizard-files'
        resource url: 'js/templateFields.js'
    }

    samtooltips {
        resource url:[plugin: 'gdt', dir:'js', file: 'tooltips.js']
        resource url:[plugin: 'gdt', dir:'js', file: 'jquery.qtip-1.0.0-rc3.js']
    }
}