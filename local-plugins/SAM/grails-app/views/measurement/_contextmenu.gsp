<li><g:link class="list" controller="measurement" params="${[module: module]}">List measurements</g:link></li>
<li><g:link class="create" controller="measurement" action="create" params="${[module: module]}">Create new measurement</g:link></li>
<li><g:link class="upload" controller="SAMImporter" action="upload" params="${[importer: "Measurements", module: module]}">Import measurements</g:link></li>
