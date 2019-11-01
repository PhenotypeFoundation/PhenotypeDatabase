package org.dbxp.moduleBase

class DataTablesTagLib {
    static namespace = 'dt';

    def dataTable = {attrs, body ->

        // TODO: add comment

        // id is required
        if(attrs.id == null)
            throwTagError("Tag [datatablesForm] is missing required attribute [id]");

        out << "<form name='"+attrs.id+"_form' id='"+attrs.id+"_form'>";

        Map mapInputs = attrs.inputs;
        mapInputs.each { item ->
            out << "<input type='hidden' id='"+item.key +"' value='"+ item.value +"' />";
        }
        out << "</form>";

        String strClass="";
        if(!(attrs.class == null))
            strClass = " " + attrs.class;

        out << "<table id='"+attrs.id+"_table' class='datatables"+strClass+"' rel='" + attrs.rel + "'>";
        out << body{};
        out << "</table>";
    }

    def buttonsShowEditDelete = {attrs ->
        // This tag generates 3 default buttons for each row (show, edit, delete)
        //
        // Usage:
        // <g:buttonsViewEditDelete controller="measurement" id="${measurementInstance.id}"/>
        // <g:buttonsViewEditDelete controller="measurement" id="${measurementInstance.id}" mapEnabled="[blnShow: true, blnEdit: false, blnDelete: true]" />

        // id is required
        if(attrs.id == null)
            throwTagError("Tag [buttonsShowEditDelete] is missing required attribute [id]");

        // controller is required
        if(attrs.controller == null)
            throwTagError("Tag [buttonsShowEditDelete] is missing required attribute [controller]");

        // mapEnabled is optional
        if(attrs.mapEnabled == null) {
            // By default all buttons are enabled
            attrs.mapEnabled = [blnShow: true, blnEdit: true, blnDelete: true];
        }

        out << this.buttonShow(id: attrs.id, controller: attrs.controller, blnEnabled: attrs.mapEnabled.blnShow);
        out << this.buttonEdit(id: attrs.id, controller: attrs.controller, blnEnabled: attrs.mapEnabled.blnEdit);
        out << this.buttonDelete(id: attrs.id, controller: attrs.controller, blnEnabled: attrs.mapEnabled.blnDelete);

	}

    def buttonShow = {attrs ->
        // This tag generates the default button Show
        //
        // Usage:
        // <g:buttonView controller="measurement" id="${measurementInstance.id}"/>
        // <g:buttonView controller="measurement" id="${measurementInstance.id}" blnEnabled="true" />

        // id is required
        if(attrs.id == null)
            throwTagError("Tag [buttonView] is missing required attribute [id]");

        // controller is required
        if(attrs.controller == null)
            throwTagError("Tag [buttonView] is missing required attribute [controller]");

        // blnShow is optional
        if(attrs.blnEnabled == null) {
            // By default all buttons are enabled
            attrs.blnEnabled = true;
        }

        // create show button
        out << "<td class='buttonColumn'>";
        if(attrs.blnEnabled) {
            if (attrs?.params) {
                out << g.link(action:"show", class:"show", controller:attrs.controller, params: attrs.params, id:attrs.id, "<img src=\"${fam.icon( name: 'magnifier')}\" alt=\"show\"/>");
            }
            else {
                out << g.link(action:"show", class:"show", controller:attrs.controller, id:attrs.id, "<img src=\"${fam.icon( name: 'magnifier')}\" alt=\"show\"/>");
            }

        } else {
            out << "<img class='disabled' src=\"${fam.icon( name: 'magnifier')}\" alt=\"show\"/>";
        }
        out << "</td>";
    }

    def buttonEdit = {attrs ->
        // This tag generates the default button Edit
        //
        // Usage:
        // <g:buttonEdit controller="measurement" id="${measurementInstance.id}"/>
        // <g:buttonEdit controller="measurement" id="${measurementInstance.id}" blnEnabled="true" />

        // id is required
        if(attrs.id == null)
            throwTagError("Tag [buttonEdit] is missing required attribute [id]");

        // controller is required
        if(attrs.controller == null)
            throwTagError("Tag [buttonEdit] is missing required attribute [controller]");

        // blnEnabled is optional
        if(attrs.blnEnabled == null) {
            // By default all buttons are enabled
            attrs.blnEnabled = true;
        }

        // create edit button
        out << "<td class='buttonColumn'>";
        if(attrs.blnEnabled) {
            if (attrs?.params) {
                out << g.link(action:"edit", class:"edit", controller:attrs.controller, params: attrs.params, id:attrs.id, "<img src=\"${fam.icon( name: 'pencil')}\" alt=\"edit\"/>");
            }
            else {
                out << g.link(action:"edit", class:"edit", controller:attrs.controller, id:attrs.id, "<img src=\"${fam.icon( name: 'pencil')}\" alt=\"edit\"/>");
            }
        } else {
            out << "<img class='disabled' src=\"${fam.icon( name: 'pencil')}\" alt=\"edit\"/>";
        }
        out << "</td>";
    }

    def buttonDelete = {attrs ->
        // This tag generates the default button Delete
        //
        // Usage:
        // <g:buttonDelete controller="measurement" id="${measurementInstance.id}"/>
        // <g:buttonDelete controller="measurement" id="${measurementInstance.id}" blnEnabled="true" />

        // id is required
        if(attrs.id == null)
            throwTagError("Tag [buttonDelete] is missing required attribute [id]");

        // controller is required
        if(attrs.controller == null)
            throwTagError("Tag [buttonDelete] is missing required attribute [controller]");

        // blnShow is optional
        if(attrs.blnEnabled == null) {
            // By default all buttons are enabled
            attrs.blnEnabled = true;
        }

        // create delete button
        out << "<td class='buttonColumn'>";
        if(attrs.blnEnabled) {
            if (attrs?.params) {
                out << "<form id='"+attrs.controller+"_"+attrs.id+"_deleteform' name='"+attrs.controller+"_"+attrs.id+"_deleteform' method='post' action='delete'>";
                out << g.link(action:"delete", class:"delete", onclick:"if(confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}')) {\$('#${attrs.controller}_${attrs.id}_deleteform').submit(); return false;} else {return false;} ;", controller:attrs.controller, "<img src=\"${fam.icon( name: 'delete')}\" alt=\"delete\"/>");
                out << "<input type='hidden' name='ids' value='"+attrs.id+"' />";
                params.each {
                    out << "<input type='hidden' name='"+it.key+"' value='"+it.value+"' />";
                }
                out << "</form>";
            }
            else {
                out << "<form id='"+attrs.controller+"_"+attrs.id+"_deleteform' name='"+attrs.controller+"_"+attrs.id+"_deleteform' method='post' action='delete'>";
                out << g.link(action:"delete", class:"delete", onclick:"if(confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}')) {\$('#${attrs.controller}_${attrs.id}_deleteform').submit(); return false;} else {return false;} ;", controller:attrs.controller, "<img src=\"${fam.icon( name: 'delete')}\" alt=\"delete\"/>");
                out << "<input type='hidden' name='ids' value='"+attrs.id+"' />";
                out << "</form>";
            }
        } else {
            out << "<img class='disabled' src=\"${fam.icon( name: 'delete')}\" alt=\"delete\"/>";
        }
        out << "</td>";
    }

    def buttonsHeader = {attrs ->
        // This tag generates a number of empty headers (default=3)
        //
        // Usage:
        // <g:buttonsHeader/>
        // <g:buttonsHeader numColumns="2"/>

        if(attrs.numColumns == null) {
            // Default = 3
            attrs.numColumns = 3;
        } else {
            attrs.numColumns = Integer.valueOf(attrs.numColumns);
        }

        for(int i=0; i<attrs.numColumns; i++) {
            // Create empty header
            out << '<th class="nonsortable buttonColumn"></th>';
        }

    }
}
