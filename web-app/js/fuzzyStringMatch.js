/**
 * FuzzyStringMatcher JavaScript class
 * Copyright (C) 2010 Jeroen Wesbeek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Usage:
 * ------
 * <input type="text" fuzzymatching="ajaxUrl" ... />
 *
 * in combination with gdt you should set the fuzzyStringMatchable map,
 * see dbnp.studycapturing.Study for details
 *
 * @author		Jeroen Wesbeek
 * @since		20110428
 * @package		org.dbnp.gdt
 * @requires	jquery, jquery-ui
 *
 * Revision information:
 * $Rev: 1344 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-07 00:10:00 +0100 (Fri, 07 Jan 2011) $
 */
function FuzzyStringMatcher() {
}
FuzzyStringMatcher.prototype = {
	/**
	 * initialize object
	 */
	init: function(options) {
		var that = this;

		// find all ontology elements
		$("input[fuzzymatching^='/']").each(function() {
			that.initAutocomplete(this);
		});
	},

	initAutocomplete: function(element) {
		var that = this;
		var inputElement = $(element);

		inputElement.autocomplete({
			source: function(request, response) {
				$.ajax({
					url: inputElement.attr('fuzzymatching') + "&value=" + inputElement.val(),
					dataType: "jsonp",
					success: function(data) {
						response(data);
					}
				});
			},
			minLength: 2,
			select: function(event, ui) {
			},
			open: function() {
				$(this).removeClass("ui-corner-all").addClass("ui-corner-top");
			},
			close: function() {
				$(this).removeClass("ui-corner-top").addClass("ui-corner-all");
			}
		});
	}
}