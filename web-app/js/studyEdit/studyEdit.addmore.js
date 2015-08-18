if( typeof( StudyEdit ) === "undefined" ) { 
	StudyEdit = {};
}

StudyEdit.addMore = {
	initialize: function( selector ) {
		this.templates.initialize(selector);
		this.ontologies.initialize(selector);
	},
	
	templates: {
		initialize: function(selector) {
			var that = this;
			
			new SelectAddMore().init({
				rel	 : 'template',
				selector: selector,
				url	 : baseUrl + '/templateEditor',
				vars	: 'entity,ontologies',
				label   : 'add / modify..',
				style   : 'modify',
				onClose : function(scope) {
					that.refresh( selector );
				}
			});
		},
		
		refresh: function(selector) {
			var that = this;
			
			$( selector + " [rel=template]").each(function(idx,el) {
				var select = $(el);
				var entity = select.data("entity");
				
				$.get( baseUrl + "/template/getAllForEntity", { entity: entity }, function(data) {
					// Empty select 
					select.empty();
					select.off("change");
					
					// Add a dummy
					select.append( $( "<option>" ).attr( "value", "" ) );
					
					// Add new templates
					$.each(data, function(templateId, templateName) {
						select.append( $("<option>").attr( "value", templateName ).text( templateName ) );
					});
					
					that.initialize(selector)
				});
			});					
		}
	},
	ontologies: {
		initialize: function(selector) {
			var that = this;
			new SelectAddMore().init({
				rel	 : 'term',
				selector: selector,
				url	 : baseUrl + '/termEditor',
				vars	: 'ontologies',
				label   : 'add more...',
				style   : 'addMore',
				onClose : function(scope) {
					that.refresh( selector );
				}
			});
		},
		refresh: function(selector) {
			var that = this;
			
			$( selector + " [rel=term]").each(function(idx,el) {
				var select = $(el);
				var ontologies = select.attr("ontologies");
				
				var ajaxParams = {
				  url: baseUrl + "/termEditor/getAllTermsForOntologies",
				  data: { ontologies: ontologies.split(",") },
				  traditional: true
				}
					
				$.ajax(ajaxParams).done(function(data) {
					// Empty select 
					select.empty();
					select.off("change");
					
					// Make sure to add a dummy entry
					if( select.attr( "required" ) ) {
						select.append( $( "<option>" ).attr( "value", "" ) );
					}
					
					// Add new terms
					$.each(data, function(termIdx, termName) {
						select.append( $("<option>").attr( "value", termName ).text( termName ) );
					});
					
					that.initialize(selector)
				});
			});						
		}
		
	},
}		