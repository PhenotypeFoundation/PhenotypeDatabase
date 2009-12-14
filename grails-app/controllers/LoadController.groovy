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

    def load = {

        static ArrayList attributs = new ArrayList();

        render("Loading ...\n");

        File file = new File("/home/ademcan/Desktop/magetab/examples/E-GEOD-2354.idf.txt");

        def person = new magetab.idf.Person();
        def protocol = new magetab.idf.Protocol();
        def publication = new magetab.idf.Publication();

        ArrayList rows = new ArrayList();

        for (i in file.readLines()){
            String line = i.toString() ;
            ArrayList tmp_list = new ArrayList();
            List parsedTab = line.split("\t");

            for (j in parsedTab) {
                tmp_list.add(j);
            }   
            
            if (tmp_list.size() > 1){
                if (tmp_list[0]=="Investigation Title"){
                    magetab.idf.InvestigationDesign investigationDesign = new magetab.idf.InvestigationDesign();
                    investigationDesign.title=tmp_list[1];
                }
                else if (tmp_list[0]=="Experimental Design") {

                }
                else if (tmp_list[0]=="Experimental Term Source REF") {

                }
                else if (tmp_list[0]=="Experimental Factor Name") {

                }
                else if (tmp_list[0]=="Experimental Factor Type") {

                }
                else if (tmp_list[0]=="Experimental Factor Term Source REF") {

                }
                else if (tmp_list[0]=="Person Last Name") {
                    person.lastName = tmp_list[1];
                }
                else if (tmp_list[0]=="Person First Name") {
                    person.firstName = tmp_list[1];
                }
                else if (tmp_list[0]=="Person Mid Initials") {
                    person.midInitials = tmp_list[1];
                }
                else if (tmp_list[0]=="Person Email") {
                    person.email = tmp_list[1];
                }
                else if (tmp_list[0]=="Person Phone") {
                    person.phone = tmp_list[1];
                }
                else if (tmp_list[0]=="Person Fax") {
                    person.fax = tmp_list[1];
                }
                else if (tmp_list[0]=="Person Address") {
                    person.address = tmp_list[1];
                }
                else if (tmp_list[0]=="Person Affiliation") {
                    person.affiliation = tmp_list[1];
                }
                else if (tmp_list[0]=="Person Roles") {
                    person.roles = tmp_list[1];
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

                }
                else if (tmp_list[0]=="Normalization Term Source REF") {

                }
                else if (tmp_list[0]=="Date Of Experiment") {

                }
                else if (tmp_list[0]=="Public Release Date") {

                }
                else if (tmp_list[0]=="PubMed ID") {
                    publication.pubMedID = tmp_list[1];
                }
                else if (tmp_list[0]=="Publication DOI") {
                    publication.DOI = tmp_list[1];
                }
                else if (tmp_list[0]=="Publication Author List") {

                }
                else if (tmp_list[0]=="Publication Title") {
                    publication.title = tmp_list[1];
                }
                else if (tmp_list[0]=="Publication Status") {

                }
                else if (tmp_list[0]=="Publication Status Term Source REF") {

                }
                else if (tmp_list[0]=="Experiment Description") {

                }
                else if (tmp_list[0]=="Protocol Name") {
                    protocol.name = tmp_list[1];
                }
                else if (tmp_list[0]=="Protocol Type") {
                    
                }
                else if (tmp_list[0]=="Protocol Description") {
                    protocol.description = tmp_list[1];
                }
                else if (tmp_list[0]=="Protocol Parameters") {
                    protocol.parameters = tmp_list[1];
                }
                else if (tmp_list[0]=="Protocol Hardware") {
                    protocol.hardware = tmp_list[1];
                }
                else if (tmp_list[0]=="Protocol Software") {
                    protocol.software = tmp_list[1];
                }
                else if (tmp_list[0]=="Protocol Contact") {
                    protocol.contact = tmp_list[1];
                }
                else if (tmp_list[0]=="Protocol Term Source REF") {

                }
                else if (tmp_list[0]=="SDRF File") {

                }
                else if (tmp_list[0]=="Term Source Name") {

                }
                else if (tmp_list[0]=="Term Source File") {

                }
                else if (tmp_list[0]=="Term Source Version") {

                }

            }

            person.save();
            protocol.save();
            publication.save();

        }

    }

}

