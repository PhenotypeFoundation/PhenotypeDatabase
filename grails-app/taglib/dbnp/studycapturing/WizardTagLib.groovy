package dbnp.studycapturing

import dbnp.authentication.SecUser
import org.dbnp.gdt.*

/**
 * Wizard tag library
 *
 * @author Jeroen Wesbeek
 * @since 20100113
 * @package wizard
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class WizardTagLib extends GdtTagLib {
	def AuthenticationService
        
	/**
	 * Study form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def studyElement = { attrs, body ->
		// render study element
		baseElement.call(
			'studySelect',
			attrs,
			body
		)
	}

	/**
	 * render a study select element
	 * @param Map attrs
	 */
	def studySelect = { attrs ->
		// Find all studies the user has access to (max 100)
		attrs.from = Study.giveWritableStudies(AuthenticationService.getLoggedInUser(), 100);

		// got a name?
		if (!attrs.name) {
			attrs.name = "study"
		}

		// got result?
		if (attrs.from.size() > 0) {
			out << select(attrs)
		} else {
			// no, return false to make sure this element
			// is not rendered in the template
			return false
		}
	}

	/**
	 * Protocol form element
	 * @param Map attributes
	 * @param Closure help content
	 *
	def protocolElement = { attrs, body ->
		// render protocol element
		baseElement.call(
			'protocolSelect',
			attrs,
			body
		)
	}

	/**
	 * render a protocol select element
	 * @param Map attrs
	 *
	def protocolSelect = { attrs ->
		// fetch all protocold
		attrs.from = Protocol.findAll()	// for now, all protocols

		// got a name?
		if (!attrs.name) {
			attrs.name = 'protocol'
		}

		out << select(attrs)
	}

	def show = { attrs ->
		// is object parameter set?
		def o = attrs.object

		println o.getProperties();
		o.getProperties().each {
			println it
		}

		out << "!! test version of 'show' tag !!"
	}
	 */

	def PublicationSelectElement = { attrs, body ->
		attrs.description = 'Publications';
		// render list with publications currently available
		baseElement.call(
			'_publicationList',
			attrs,
			body
		)

		attrs.description = '';

		// render 'Add publication button'
		baseElement.call(
			'_publicationAddButton',
			attrs,
			body
		)
	}

	/**
	 * Renders a input box for publications
	 */
	def publicationSelect = { attrs, body ->
		if (attrs.get('value') == null) {
			attrs.value = [];
		}
		if (attrs.get('description') == null) {
			attrs.description = '';
		}
		out << '<form id="' + attrs.name + '_form" onSubmit="return false;">';
		out << textField(
			name: attrs.get("name"),
			value: '',
			rel: 'publication-pubmed',
			style: 'width: 400px;'
		);
		out << '</form>';
		out << '<script type="text/javascript">';
		out << '  var onSelect = function( chooserObject, inputElement, event, ui ) { selectPubMedAdd( chooserObject, inputElement, event, ui ); enableButton( ".' + attrs.name + '_publication_dialog", "Add", true ); };'
		out << '  iField = $( "#' + attrs.get('name') + '" );';
		out << '  new PublicationChooser().initAutocomplete( iField, { "select" : onSelect } );';
		out << '</script>';
	}

	def _publicationList = { attrs, body ->
		def display_none = 'none';
		if (!attrs.get('value') || attrs.get('value').size() == 0) {
			display_none = 'inline';
		}

		// Add a unordered list
		out << '<ul class="publication_list" id="' + attrs.name + '_list">';

		out << '<li>';
		out << '<span class="publication_none" id="' + attrs.name + '_none" style="display: ' + display_none + ';">';
		out << 'No publications selected';
		out << '</span>';
		out << '</li>';

		out << '</ul>';

		// Add the publications using javascript
		out << '<script type="text/javascript">'
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			def i = 0;
			attrs.get('value').each {
				out << 'showPublication( ';
				out << '  "' + attrs.name + '",';
				out << '  ' + it.id + ',';
				out << '  "' + it.title + '",';
				out << '  "' + it.authorsList + '",';
				out << '  ' + i++;
				out << ');';
			}
		}
		out << '</script>';

		def ids;
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			ids = attrs.get('value').id.join(',')
		} else {
			ids = '';
		}
		out << '<input type="hidden" name="' + attrs.name + '_ids" value="' + ids + '" id="' + attrs.name + '_ids">';
	}

	def _publicationAddButton = { attrs, body ->
		
		if( attrs.get( 'noForm', false ) ) {
			// Only show the add button. The dialog that is created with this method otherwise,
			// should be created somewhere outside the form.
		} else {
			out << publicationDialog( attrs, body );
		}
	
		out << '<input type="button" onClick="openPublicationDialog(\'' + attrs.name + '\' );" value="Add Publication">';
	}
	
	// Show the add publications dialog
	def publicationDialog = { attrs, body ->
		// Output the dialog for the publications
		out << '<div id="' + attrs.name + '_dialog">';
		out << '<p>Search for a publication on pubmed. You can search on a part of the title, authors or pubmed ID. </p>';
		out << publicationSelect(attrs, body);
		out << '</div>';
		out << '<script type="text/javascript">';
		out << '  createPublicationDialog( "' + attrs.name + '" );'
		out << '</script>';
	}
	

	def ContactSelectElement = { attrs, body ->

		attrs.description = 'Contacts';
		// render list with publications currently available
		baseElement.call(
			'_contactList',
			attrs,
			body
		)

		attrs.description = '';

		// render 'publications list'
		out << '<div id="' + attrs.name + '_dialog" class="contacts_dialog" style="display: none;">'
		baseElement.call(
			'_personSelect',
			attrs,
			body
		)
		baseElement.call(
			'_roleSelect',
			attrs,
			body
		)
		baseElement.call(
			'_contactAddButtonAddition',
			attrs,
			body
		)
		out << '</div>';

		// render 'Add contact button'
		baseElement.call(
			'_contactAddDialogButton',
			attrs,
			body
		)
	}

	def _contactList = { attrs, body ->
		def display_none = 'none';
		if (!attrs.get('value') || attrs.get('value').size() == 0) {
			display_none = 'inline';
		}

		// Add a unordered list
		out << '<ul class="contact_list" id="' + attrs.name + '_list">';

		out << '<li>';
		out << '<span class="contacts_none" id="' + attrs.name + '_none" style="display: ' + display_none + ';">';
		out << 'No contacts selected';
		out << '</span>';
		out << '</li>';

		out << '</ul>';

		// Add the contacts using javascript
		out << '<script type="text/javascript">'
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			def i = 0;
			attrs.get('value').each {
				out << 'showContact( ';
				out << '  "' + attrs.name + '",';
				out << '  "' + it.person.id + '-' + it.role.id + '",';
				out << '  "' + it.person.lastName + ', ' + it.person.firstName + (it.person.prefix ? ' ' + it.person.prefix : '') + '",';
				out << '  "' + it.role.name + '",';
				out << '  ' + i++;
				out << ');';
			}
		}
		out << '</script>';

		def ids = '';
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			ids = attrs.get('value').collect { it.person.id + '-' + it.role.id }
			ids = ids.join(',');
		}
		out << '<input type="hidden" name="' + attrs.name + '_ids" value="' + ids + '" id="' + attrs.name + '_ids">';
	}

	def _contactAddSelect = { attrs, body ->
		out << _personSelect(attrs) + _roleSelect(attrs);
	}

	def _contactAddButtonAddition = { attrs, body ->
		out << '<input type="button" onClick="if( addContact ( \'' + attrs.name + '\' ) ) { $(\'#' + attrs.name + '_dialog\').hide(); $( \'#' + attrs.name + '_dialogButton\' ).show(); }" value="Add">';
		out << '<input type="button" onClick="$(\'#' + attrs.name + '_dialog\').hide(); $( \'#' + attrs.name + '_dialogButton\' ).show();" value="Close">';
	}

	def _contactAddDialogButton = { attrs, body ->
		out << '<input type="button" onClick="$( \'#' + attrs.name + '_dialog\' ).show(); $(this).hide();" id="' + attrs.name + '_dialogButton" value="Add Contact">';
	}
	/**
	 * Person select element
	 * @param Map attributes
	 */
	def _personSelect = { attrs ->
		def selectAttrs = new LinkedHashMap();

		// define 'from'
		def persons = Person.findAll().sort({ a, b -> a.lastName == b.lastName ? (a.firstName <=> b.firstName) : (a.lastName <=> b.lastName) } as Comparator);
		selectAttrs.from = persons.collect { it.lastName + ', ' + it.firstName + (it.prefix ? ' ' + it.prefix : '') }
		selectAttrs.keys = persons.id;

		// add 'rel' attribute
		selectAttrs.rel = 'person'
		selectAttrs.name = attrs.name + '_person';

		// add a dummy field
		selectAttrs.from.add(0,'')
		selectAttrs.keys.add(0,'')

		out << "Person: " + select(selectAttrs)
	}

	/**
	 * Role select element
	 * @param Map attributes
	 */
	def _roleSelect = { attrs ->
		def selectAttrs = new LinkedHashMap();

		// define 'from'
		def roles = PersonRole.findAll();
		selectAttrs.from = roles.collect { it.name };
		selectAttrs.keys = roles.id;

		// add 'rel' attribute
		selectAttrs.rel = 'role'
		selectAttrs.name = attrs.name + '_role';

		// add a dummy field
		selectAttrs.from.add(0,'')
		selectAttrs.keys.add(0,'')

		out << "Role: " + select(selectAttrs)
	}


	def UserSelectElement = { attrs, body ->
		// render list with publications currently available
		baseElement.call(
			'_userList',
			attrs,
			body
		)

		attrs.description = '';

		// render 'Add user button'
		baseElement.call(
			'_userAddButton',
			attrs,
			body
		)
	}

	/**
	 * Renders an input box for publications
	 */
	def userSelect = { attrs, body ->
		if (attrs.get('value') == null) {
			attrs.value = [];
		}
		if (attrs.get('description') == null) {
			attrs.description = '';
		}
                
		out << '<form id="' + attrs.name + '_form" onSubmit="return false;">';
		out << select(
			name: attrs.get("name"),
			value: '',
                        from: SecUser.list(),
                        optionValue: 'username',
                        optionKey: 'id',
			style: 'width: 400px;'
		);
		out << '</form>';
	}

	def _userList = { attrs, body ->
		def display_none = 'none';
		if (!attrs.get('value') || attrs.get('value').size() == 0) {
			display_none = 'inline';
		}

		// Add a unordered list
		out << '<ul class="user_list" id="' + attrs.name + '_list">';

		out << '<li>';
		out << '<span class="user_none" id="' + attrs.name + '_none" style="display: ' + display_none + ';">';
		out << '-';
		out << '</span>';
		out << '</li>';

		out << '</ul>';

		// Add the publications using javascript
		out << '<script type="text/javascript">'
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			def i = 0;
			attrs.get('value').each {
				out << 'showUser( ';
				out << '  "' + attrs.name + '",';
				out << '  ' + it.id + ',';
				out << '  "' + it.username + '",';
				out << '  ' + i++;
				out << ');';
			}
		}
		out << '</script>';

		def ids;
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			ids = attrs.get('value').id.join(',')
		} else {
			ids = '';
		}
		out << '<input type="hidden" name="' + attrs.name + '_ids" value="' + ids + '" id="' + attrs.name + '_ids">';
	}

	def _userAddButton = { attrs, body ->
		if( attrs.get( 'noForm', false ) ) {
			// Only show the add button. The dialog that is created with this method otherwise,
			// should be created somewhere outside the form.
		} else {
			out << userDialog( attrs, body );
		}

		out << '<input type="button" onClick="openUserDialog(\'' + attrs.name + '\' );" value="Add User">';
	}
	
	def userDialog = { attrs, body ->
		// Output the dialog for the publications
		out << '<div id="' + attrs.name + '_dialog">';
		out << '<p>Select a user from the database.</p>';
		out << userSelect(attrs, body);
		out << '</div>';
		out << '<script type="text/javascript">';
		out << '  createUserDialog( "' + attrs.name + '" );'
		out << '</script>';
	}

}