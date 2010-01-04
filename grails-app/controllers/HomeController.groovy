import dbnp.studycapturing.Study

class HomeController {

    def index = {
       [ studyCount: dbnp.studycapturing.Study.count() ]
    }
}
