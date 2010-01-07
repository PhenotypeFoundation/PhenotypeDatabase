package dbnp.studycapturing

class Template {

    String name
    nimble.User owner
  
    static constraints = {
        name(unique:true)
    }

    def String toString() {
        return this.name;
    }
}
