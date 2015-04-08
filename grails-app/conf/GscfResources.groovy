import org.dbxp.moduleBase.DataTablesTagLib;
import org.springframework.context.annotation.DependsOn;

import dbnp.query.AdvancedQueryController;

modules = {
    'jquery-browser-plugin' {
        dependsOn 'jquery'
        resource url: [ dir: 'js', file: 'jquery.browser.min.js' ]
    }

    // Main GSCF module, included in the layout file
    gscfmain {
        dependsOn 'jquery'
        dependsOn 'jquery-ui'

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

        resource url:[ dir:'css', file: 'dialog.css']
        resource url:[ dir:'js', file: 'disableKeys.js']
        resource url:[ dir:'js', file: 'paginate.js']
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
        resource url:[ dir:'js', file: 'jquery.tipTip.minified.js']
        resource url:[ dir:'js', file: 'tipTip.initialize.js']
        resource url:[ dir:'css', file: 'tipTip.css']
    }

    // Helptexts in the wizard
    helptooltips {
        resource url: [dir: 'js', file: 'tooltips.js', plugin: 'gdt']
        resource url: [dir: 'js', file: 'jquery.qtip-1.0.0-rc3.js', plugin: 'gdt']
    }

    // Nice good looking buttons
    buttons {
        resource url:[ dir:'css', file: 'buttons.css']
    }

    // Scripts needed for study edit
    studyEdit {
        dependsOn 'jquery', 'jquery-ui', 'timeline', 'buttons', 'helptooltips'
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.meta.js'], disposition: 'head'
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.design.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.design.eventGroupDialog.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.design.subjectGroups.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.assaySamples.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.datatables.js']
        resource url:[ dir:'css', file: 'studyEdit.css']
        resource url:[ dir:'css', file: 'templates.css']

        resource url: [dir: 'js', file: 'ajaxupload.3.6.js']
        resource url: [dir: 'js', file: 'selectAddMore-1.0.js' ]
        resource url: [dir: 'js', file: 'publication-chooser.js' ], disposition: 'head'
        resource url: [dir: 'js', file: 'publication-chooser.pubmed.js' ], disposition: 'head'
    }

    // Scripts needed for study view pages
    studyView {
        dependsOn 'jquery', 'jquery-ui', 'timeline', 'buttons', 'helptooltips'
        resource url:[ dir:'js/studyView', file: 'studyView.js']
        resource url:[ dir:'js/studyView', file: 'studyView.meta.js'], disposition: 'head'
        resource url:[ dir:'js/studyView', file: 'studyView.design.js']
        resource url:[ dir:'js/studyView', file: 'studyView.design.subjectGroups.js']
        resource url:[ dir:'js/studyEdit', file: 'studyEdit.datatables.js']
        resource url:[ dir:'css', file: 'studyView.css']
        resource url:[ dir:'css', file: 'templates.css']
    }

    // Datatables to be used in several pages
    "gscf-datatables" {
        dependsOn 'jquery'
        resource url: [dir: 'js', file: 'jquery.dataTables.js' ] , linkOverride:'//ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.js'
        resource url: [dir: 'js', file: 'datatables.js', plugin: 'dbxpModuleBase']

        resource url: [ dir:'css', file: 'jquery.dataTables.css'], linkOverride: "//ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css"
        resource url: [ dir:'css', file: 'datatables-jui.css']
    }

    // Advanced Query functionality
    advancedQuery {
        dependsOn "gscf-datatables", "buttons"
        resource url: [dir: 'js', file: 'advancedQueryResults.js']
        resource url: [dir: 'css', file: 'advancedQuery.css']
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

    templateEditor {
        dependsOn 'jquery'
        dependsOn 'jquery-ui'
        
        resource url: [dir: 'js', file: 'templateEditor.js']
        resource url: [dir: 'css', file: 'templateEditor.css']
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
}