package dbnp.studycapturing

class ParameterStringListItem {

    String name;

    static constraints = {
        name(nullable: false);
    }


    def String toString() { return name }

}
