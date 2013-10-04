import org.dbxp.moduleBase.DataTablesTagLib;
import org.springframework.context.annotation.DependsOn;

import dbnp.query.AdvancedQueryController;

modules = {
    gscfmain {
        dependsOn 'jquery'
		dependsOn 'jquery-ui'
		
		resource url:[ dir:'css', file: 'default.css']
		resource url:[ dir:'css', file: 'login_panel.css']
		
		resource url:[ dir:'js', file: 'disableKeys.js']
		resource url:[ dir:'js', file: 'login_panel.js']
		resource url:[ dir:'js', file: 'topnav.js']
		resource url:[ dir:'js', file: 'paginate.js']
		resource url:[ dir:'js', file: 'removeWebflowExecutionFromMenu.min.js']
    }

	// Timeline scripts used within the study edit 
	timeline {
		dependsOn 'jquery'
		resource url:[ dir:'js', file: 'reltime.js']
		resource url:[ dir:'js/timeline', file: 'timeline.js']
		resource url:[ dir:'js/timeline', file: 'study-timeline.js']
		resource url:[ dir:'css', file: 'timeline.css']
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
	
	studyEdit {
		dependsOn 'jquery', 'jquery-ui', 'timeline', 'buttons', 'helptooltips'
		resource url:[ dir:'js', file: 'studyEdit.js']
		resource url:[ dir:'js', file: 'studyEdit.meta.js'], disposition: 'head'
		resource url:[ dir:'js', file: 'studyEdit.design.js']
		resource url:[ dir:'js', file: 'studyEdit.datatables.js']
		resource url:[ dir:'css', file: 'studyEdit.css']
		resource url:[ dir:'css', file: 'templates.css']
		
		resource url: [dir: 'js', file: 'ajaxupload.3.6.js']
		resource url: [dir: 'js', file: 'selectAddMore-1.0.js' ]
		resource url: [dir: 'js', file: 'publication-chooser.js' ], disposition: 'head'
		resource url: [dir: 'js', file: 'publication-chooser.pubmed.js' ], disposition: 'head'
	}
	
	"gscf-datatables" { 
		dependsOn 'jquery'
		resource url: [dir: 'js', file: 'jquery.dataTables.min.js' ] , linkOverride:'http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js'
		resource url: [dir: 'js', file: 'datatables.js', plugin: 'dbxpModuleBase']

		resource url: [ dir:'css', file: 'jquery.dataTables.css'], linkOverride: "http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css"
		resource url: [ dir:'css', file: 'datatables-jui.css']
	}
	
	advancedQuery {
		dependsOn "gscf-datatables", "buttons" 
		resource url: [dir: 'js', file: 'advancedQueryResults.js']
		resource url: [dir: 'css', file: 'advancedQuery.css']
	}
	
}