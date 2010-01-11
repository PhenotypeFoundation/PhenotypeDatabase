/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ademcan
 */
class LoadController {

    String path;
    String delim;
    static Map att_list = new HashMap<String, ArrayList>();

    def index = {}

    def get_string(ArrayList tmp_list) {
        String value = tmp_list[1];
        for (a in 2..tmp_list.size()-1){
            value = value + " , " +tmp_list[a];
        }
        return value;
    }

    def load = {

        //ArrayList attributes = new ArrayList();

        render("Loading ...<br>");

        InputStream inputStream = request.getFile("uploadfile").inputStream;
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream));

        def investigationDesign = new dbnp.transcriptomics.magetab.idf.InvestigationDesign();
        def person = new dbnp.transcriptomics.magetab.idf.Person();
        def protocol = new dbnp.transcriptomics.magetab.idf.MAGEProtocol();l
        def publication = new dbnp.transcriptomics.magetab.idf.Publication();
        def normalization = new dbnp.transcriptomics.magetab.sdrf.Normalization();
        //def termSource = new dbnp.transcriptomics.magetab.adf.Termsource();
        def comment = new dbnp.transcriptomics.magetab.idf.Comment();
        def experimentalInfo = new dbnp.transcriptomics.magetab.idf.ExperimentalInfo();
        def factor = new dbnp.transcriptomics.magetab.idf.Factor();

        //ArrayList rows = new ArrayList();

        for (i in fileReader.readLines()){
            //render(i);
            String line = i.toString() ;
            ArrayList tmp_list = new ArrayList();
            def parsedTab = line.split("\t");

            for (j in parsedTab) {
                tmp_list.add(j);
            }   
            
            if (tmp_list.size() > 1){
                if (tmp_list[0]=="Investigation Title"){
                    investigationDesign.title = tmp_list[1];
                }
                else if (tmp_list[0]=="Comment[AEMIAMESCORE]") {
                    comment.aemiameScore = tmp_list[1];
                }
                else if (tmp_list[0]=="Comment[SecondaryAccession]") {
                    comment.secondaryAccession = tmp_list[1];
                }
                else if (tmp_list[0]=="Comment[ArrayExpressReleaseDate]") {
                    comment.arrayExpressReleaseDate = tmp_list[1];
                }
                else if (tmp_list[0]=="Comment[ArrayExpressAccession]") {
                    comment.arrayExpressAccession = tmp_list[1];
                }
                else if (tmp_list[0]=="Comment[MAGETAB TimeStamp_Version]") {
                    comment.timestamp_version = tmp_list[1];
                }
                else if (tmp_list[0]=="Experimental Design") {
                    String design = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        design = design + " , " +tmp_list[a];
                    }
                    experimentalInfo.design = design;
                }
                else if (tmp_list[0]=="Experimental Design Term Source REF") {
                    String value = get_string(tmp_list);
                    experimentalInfo.design_term_source_ref = value;
                    //render("value : " + value );
                    println("value : "+value);
                }
                else if (tmp_list[0]=="Experimental Factor Name") {
                    String factor_name = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        factor_name = factor_name + " , " +tmp_list[a];
                    }
                    factor.name = factor_name;
                }
                //else if (tmp_list[0]=="Experimental Factor Type") {
//                    String factor_type = tmp_list[1];
//                    for (a in 2..tmp_list.size()-1){
//                        factor_type = factor_type + " , " +tmp_list[a];
//                    }
//                    factor.type = factor_type;
                //}
                else if (tmp_list[0]=="Experimental Factor Term Source REF") {
                    String exp_ref  = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        exp_ref = exp_ref + " , " +tmp_list[a];
                    }
                    experimentalInfo.factor_term_source_ref = exp_ref;
                }
                else if (tmp_list[0]=="Person Last Name") {
                    String last_name = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        last_name = last_name  + " , " +tmp_list[a];
                    }
                    person.lastName = last_name;
                }
                else if (tmp_list[0]=="Person First Name") {
                    String first_name = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        first_name = first_name  + " , " +tmp_list[a];
                    }
                    person.firstName = first_name;
                }
                else if (tmp_list[0]=="Person Mid Initials") {
                    String mid_initials = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        mid_initials = mid_initials  + " , " +tmp_list[a];
                    }
                    person.midInitials = mid_initials;
                }
                else if (tmp_list[0]=="Person Email") {
                    String pemail = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        pemail = pemail + " , " +tmp_list[a];
                    }
                    person.email = pemail;
                }
                else if (tmp_list[0]=="Person Phone") {
                    String pphone = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        pphone = pphone + " , " +tmp_list[a];
                    }
                    person.phone = pphone;
                }
                else if (tmp_list[0]=="Person Fax") {
                    String pfax = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        pfax = pfax + " , " +tmp_list[a];
                    }
                    person.fax = pfax;
                }
                else if (tmp_list[0]=="Person Address") {
                    String paddress = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        paddress = paddress + " , " +tmp_list[a];
                    }
                    person.address = paddress;
                }
                else if (tmp_list[0]=="Person Affiliation") {
                    String paffiliation = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        paffiliation = paffiliation + " , " +tmp_list[a];
                    }
                    person.affiliation = paffiliation;
                }
                else if (tmp_list[0]=="Person Roles") {
                    String proles = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        proles = proles + " , " +tmp_list[a];
                    }
                    person.roles = proles;
                }
                else if (tmp_list[0]=="Person Roles Term Source REF") {
                    person.roles_ref = tmp_list[1];
                }
                else if (tmp_list[0]=="Quality Control Type") {

                }
                else if (tmp_list[0]=="Quality Control Term Source REF") {

                }
                else if (tmp_list[0]=="Replicate Type") {

                }
                else if (tmp_list[0]=="Replicate Term Source REF") {

                }
                else if (tmp_list[0]=="Normalization Type") {
                    normalization.type = tmp_list[1];
                }
                else if (tmp_list[0]=="Normalization Term Source REF") {
                    normalization.term_source_ref = tmp_list[1];
                }
                else if (tmp_list[0]=="Date Of Experiment") {
                    investigationDesign.dateOfExperiment = tmp_list[1];
                }
                else if (tmp_list[0]=="Public Release Date") {
                    investigationDesign.publicReleaseDate = tmp_list[1];
                }
                else if (tmp_list[0]=="PubMed ID") {
                    publication.pubMedID = tmp_list[1];
                }
                else if (tmp_list[0]=="Publication DOI") {
                    String doi = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        doi = doi + " , " +tmp_list[a];
                    }
                    publication.DOI = doi;
                }
                else if (tmp_list[0]=="Publication Author List") {
                    publication.authors_list = tmp_list[1];
                }
                else if (tmp_list[0]=="Publication Title") {
                    String pubTitle = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        pubTitle = pubTitle + " , " +tmp_list[a];
                    }
                    publication.title = pubTitle;
                }
                else if (tmp_list[0]=="Publication Status") {
                    def status = new dbnp.transcriptomics.magetab.idf.OntologyTerm();
                    status.text = tmp_list[1];
                    status.save();
                    publication.status = status;
                }
                else if (tmp_list[0]=="Publication Status Term Source REF") {
                    publication.status_term_source_ref = tmp_list[1];
                }
                else if (tmp_list[0]=="Experiment Description") {
                    investigationDesign.experimentDescription = tmp_list[1];
                }
                else if (tmp_list[0]=="Protocol Name") {
                    String protocol_name = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        protocol_name = protocol_name + " , " +tmp_list[a];
                    }
                    protocol.name = protocol_name;
                }
                //todo list of type
                else if (tmp_list[0]=="Protocol Type") {
                    def type = new dbnp.transcriptomics.magetab.idf.OntologyTerm();
                    for (j in 1..tmp_list.size()){
                        type.text = tmp_list[j];
                        type.save();
                    }
                    protocol.type = type;
                }
                else if (tmp_list[0]=="Protocol Description") {
                    String protocol_description = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        protocol_description = protocol_description + " , " +tmp_list[a];
                    }
                    protocol.description = protocol_description;
                }
                else if (tmp_list[0]=="Protocol Parameters") {
                    String protocol_parameters = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        protocol_parameters = protocol_parameters + " , " +tmp_list[a];
                    }
                    protocol.parameters = protocol_parameters;
                }
                else if (tmp_list[0]=="Protocol Hardware") {
                    String phardware = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        phardware = phardware + " , " +tmp_list[a];
                    }
                    protocol.hardware = phardware;
                }
                else if (tmp_list[0]=="Protocol Software") {
                    String psoftware = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        psoftware = psoftware + " , " +tmp_list[a];
                    }
                    protocol.software = psoftware;
                }
                else if (tmp_list[0]=="Protocol Contact") {
                    String pcontact = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        pcontact = pcontact + " , " +tmp_list[a];
                    }
                    protocol.contact = pcontact;
                }
                else if (tmp_list[0]=="Protocol Term Source REF") {
                    protocol.term_source_ref = tmp_list[1];
                }
                else if (tmp_list[0]=="SDRF File") {
                    String sdrf = tmp_list[1];
                    for (a in 2..tmp_list.size()-1){
                        sdrf = sdrf + " , " +tmp_list[a];
                    }
                    investigationDesign.sdrf_file = sdrf;
                }
                else if (tmp_list[0]=="Term Source Name") {
                    //termSource.name = tmp_list[1];
                }
                else if (tmp_list[0]=="Term Source File") {
                    //termSource.file = tmp_list[1];
                }
                else if (tmp_list[0]=="Term Source Version") {
                    //termSource.version = tmp_list[1];
                }
            }

            person.save();
            protocol.save();
            publication.save();
            normalization.save();
            investigationDesign.save();
            comment.save();
            experimentalInfo.save();
            //factor.save();
            //termSource.save();
        }
    }
}

