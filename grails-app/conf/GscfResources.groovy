modules = {
    /**********************************************************
     *
     * These modules are used in the several layouts, on every 
     * page in that layout
     *
     **********************************************************/

    // Main GSCF module, included in the layout file
    gscfmain {
        dependsOn 'jquery'
        dependsOn 'jquery-ui'
        dependsOn 'infoboxes'

        resource url:[ dir:'css', file: 'default.css']
        resource url:[ dir:'js', file: 'disableKeys.js']
        resource url:[ dir:'js', file: 'paginate.js']
        resource url:[ dir:'js', file: 'removeWebflowExecutionFromMenu.js']
        resource url:[ dir:'js', file: 'main.js']
        resource url:[ dir:'js', file: 'icheck.js']
        resource url: [ dir:'js', file: 'jquery-migrate-1.2.1.js'], linkOverride: '//code.jquery.com/jquery-migrate-1.2.1.js'
    }

    // GSCF dialog module, included in the dialog
    gscfdialog {
        dependsOn 'jquery'
        dependsOn 'jquery-ui'
        dependsOn 'infoboxes'
        
        resource url:[ dir:'css', file: 'dialog.css']
        resource url:[ dir:'js', file: 'disableKeys.js']
        resource url:[ dir:'js', file: 'paginate.js']
    }

    /**********************************************************
     *
     * These modules are used on specific pages with functionality
     * The name describes the place where they are used
     *
     **********************************************************/
    home {
        dependsOn 'jquery'
        resource url:[ dir:'css', file: 'home.css']
    }
    
    "home-stats" {
        dependsOn 'jquery-ui'
        resource url:[ dir:'js', file: 'highcharts.js']
        resource url:[ dir: 'js', file: 'jquery.ui.autocomplete.html.js', plugin: 'gdt']
        resource url:[ dir:'css', file: 'home.css']
    }


    // Defines the basic 'tab' layout, used in study edit, study view and importer    
    basicTabLayout {
        dependsOn 'jquery', 'jquery-ui', 'buttons', 'helptooltips'
        resource url:[ dir:'css', file: 'basicTabLayout.css']
        
    }
    
    // Scripts needed for study edit
    studyEdit {
        dependsOn 'basicTabLayout', 'timeline', 'fileupload', 'add-more', 'publication-chooser'
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.meta.js'], disposition: 'head'
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.design.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.design.eventGroupDialog.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.design.subjectGroups.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.assaySamples.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.datatables.js']
        resource url:[ dir:'css', file: 'studyEdit.css']
        resource url:[ dir:'css', file: 'templates.css']
    }

    // Scripts needed for study view pages
    studyView {
        dependsOn 'basicTabLayout', 'timeline'
        resource url:[ dir:'js/studyView', file: 'studyView.js']
        resource url:[ dir:'js/studyView', file: 'studyView.meta.js'], disposition: 'head'
        resource url:[ dir:'js/studyView', file: 'studyView.design.js']
        resource url:[ dir:'js/studyView', file: 'studyView.design.subjectGroups.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.datatables.js']
        resource url:[ dir:'css', file: 'studyView.css']
        resource url:[ dir:'css', file: 'templates.css']
    }
    
    gscfimporter { 
        dependsOn 'basicTabLayout',  'fileupload', 'add-more' 
        dependsOn 'gscf-datatables'
        
        resource url:[ dir:'js', file: 'importer.js']
        resource url:[ dir:'css', file: 'importer.css']
        resource url:[ dir:'css', file: 'templates.css']
    }
    
    // Advanced Query functionality
    advancedQuery {
        dependsOn "gscf-datatables", "buttons"
        resource url: [dir: 'js', file: 'advancedQuery.js']
        resource url: [dir: 'js', file: 'advancedQueryResults.js']
        resource url: [dir: 'css', file: 'advancedQuery.css']
    }
    
    // Cookdata functionality
    cookdata {
        dependsOn 'jquery'
        resource url:[ dir:'css', file: 'cookdata.css']
        resource url:[ dir:'js', file: 'cookdata_dataset_selection.js']
    }
    
    // Template editor functionality
    templateEditor {
        dependsOn 'jquery'
        dependsOn 'jquery-ui'
        
        resource url: [dir: 'js', file: 'templateEditor.js', plugin: 'gdt']
        resource url: [dir: 'css', file: 'templateEditor.css', plugin: 'gdt']
        resource url: [dir: 'js', file: 'ontology-chooser.js', plugin: 'gdt'], disposition: 'head'
    }
    
    // Term editor
    termEditor {
        dependsOn 'jquery'
        dependsOn 'jquery-ui'
        
        resource url: [dir: 'css', file: 'termEditor.css', plugin: 'gdt']
        resource url: [dir: 'js', file: 'jquery.ui.autocomplete.html.js', plugin: 'gdt']
        resource url: [dir: 'js', file: 'ontology-chooser.js', plugin: 'gdt']
    }
    
    // Template importer 
    templateImporter {
        resource url: [dir: 'js', file: 'templateImporter.js']
        resource url: [dir: 'css', file: 'templateImporter.css']
    }

    
    // Scripts needed for visualization
    visualization {
        dependsOn 'jquery-ui'
        dependsOn 'jquery-browser-plugin'
        dependsOn 'jqplot'

        resource url: [dir: 'js', file: 'visualization.js']
        resource url: [dir: 'css', file: 'visualization.css']
    }
    
    // Study compare functionality
    studycompare {
        resource url: [dir: 'css', file: 'ajaxflow.css']
        resource url: [dir: 'css', file: 'studyCompare.css']
    }
    
    // Setup wizard
    setup {
        dependsOn 'helptooltips'
        resource url: [dir: 'css', file: 'ajaxflow.css']
        resource url: [dir: 'css', file: 'setupwizard.css']
        resource url: [dir: 'css', file: 'templates.css']
        
        resource url: "http://alexgorbatchev.com/pub/sh/current/styles/shCore.css"
        resource url: "http://alexgorbatchev.com/pub/sh/current/styles/shThemeDefault.css"
        resource url: "http://alexgorbatchev.com/pub/sh/current/scripts/shCore.js"
        resource url: "http://alexgorbatchev.com/pub/sh/current/scripts/shAutoloader.js"
    }
    

    /**********************************************************
     * 
     * Modules below are libraries and plugins
     * 
     **********************************************************/

    'infoboxes' {
        resource url: [dir: 'css', file: 'infoboxes.css']
    }

    'fileupload' {
        resource url: [dir: 'js', file: 'fileupload.js']
        resource url: [dir: 'css', file: 'fileupload.css']
        resource url: [dir: 'js', file: 'jquery.fileupload.js']
        resource url: [dir: 'js', file: 'jquery.iframe-transport.js']
        resource url: [dir: 'js', file: 'jquery.ui.widget.js']
    }
    
    'studywizard-files' {
        dependsOn 'fileupload'
        dependsOn 'add-more'
        
        resource url: 'css/studywizard.css'
        
        resource url: [dir: 'js', file: 'studywizard.js']
        resource url:[dir:'js', file: 'fuzzyStringMatch.js', plugin: 'gdt']
    }
    
    'add-more' {
        resource url: [dir: 'js', file: 'selectAddMore-1.0.js' ]
    }
    
    // jquery browser plugin for compatibility
    'jquery-browser-plugin' {
        dependsOn 'jquery'
        resource url: [ dir: 'js', file: 'jquery.browser.min.js' ]
    }

    // Timeline scripts used within the study edit
    timeline {
        dependsOn 'jquery'
        resource url:[ dir:'js', file: 'reltime.js']
        resource url:[ dir:'js/timeline', file: 'timeline.js']
        resource url:[ dir:'js/timeline', file: 'study-timeline.js']
        resource url:[ dir:'css/timeline', file: 'timeline.css']
        resource url:[ dir:'css/timeline', file: 'timeline-theme.css']
    }

    // Tiptip scripts to shown nice tooltips
    tiptip {
        dependsOn 'jquery'
        resource url:[ dir:'js', file: 'jquery.tipTip.min.js']
        resource url:[ dir:'js', file: 'tipTip.initialize.js']
        resource url:[ dir:'css', file: 'tipTip.css']
    }

    // Helptexts in the wizard
    helptooltips {
        dependsOn 'jquery'
        resource url: [dir: 'js', file: 'tooltips.js', plugin: 'gdt']
        resource url: [dir: 'js', file: 'jquery.qtip-1.0.0-rc3.js', plugin: 'gdt']
        resource url: [dir: 'css', file: 'tooltips.css']
    }

    // Nice good looking buttons
    buttons {
        resource url:[ dir:'css', file: 'buttons.css']
    }

    'publication-chooser' {
        resource url: [dir: 'js', file: 'publication-chooser.js' ], disposition: 'head'
        resource url: [dir: 'js', file: 'publication-chooser.pubmed.js' ], disposition: 'head'
    }
    
    // Datatables to be used in several pages
    "gscf-datatables" {
        dependsOn 'jquery'
        resource url: [dir: 'js', file: 'jquery.dataTables.js' ] , linkOverride:'//ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.js'
        resource url: [dir: 'js', file: 'datatables.js']

        resource url: [dir:'css', file: 'jquery.dataTables.css'], linkOverride: "//ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css"
        resource url: [dir:'css', file: 'datatables-jui.css']
    }

    // jqplot charting library
    jqplot {
        resource url: [dir: 'js/jqplot', file: 'excanvas.min.js'], wrapper: { s -> "<!--[if lt IE 9]>$s<![endif]-->" }
        resource url: [dir: 'js/jqplot', file: 'jquery.jqplot.min.js']
        resource url: [dir: 'css', file: 'jquery.jqplot.min.css']

        // jqPlot plugins
        resource url: [dir: 'js/jqplot/plugins', file: 'jqplot.barRenderer.min.js']
        resource url: [dir: 'js/jqplot/plugins', file: 'jqplot.categoryAxisRenderer.min.js']
        resource url: [dir: 'js/jqplot/plugins', file: 'jqplot.pointLabels.min.js']
        resource url: [dir: 'js/jqplot/plugins', file: 'jqplot.canvasTextRenderer.min.js']
        resource url: [dir: 'js/jqplot/plugins', file: 'jqplot.canvasAxisLabelRenderer.min.js']
        resource url: [dir: 'js/jqplot/plugins', file: 'jqplot.canvasAxisTickRenderer.min.js']
        resource url: [dir: 'js/jqplot/plugins', file: 'jqplot.boxplotRenderer.js']
        resource url: [dir: 'js/jqplot/plugins', file: 'jqplot.highlighter.min.js']
    }
    
    multiselect {
        dependsOn 'jquery'
        resource url: [dir: 'js', file: 'jquery.multiselect.js' ]
        resource url: [dir: 'css', file: 'jquery.multi-select.css']
    }

}
