package org.dbxp.sam

import org.dbnp.gdt.AssayModule

class SAMHomeController {
    def moduleService

    def index = {
        if (moduleService.validateModule( params.module )) {
            [module: params?.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
	}

}
