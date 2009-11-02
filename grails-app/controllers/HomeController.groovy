class HomeController {

    def index = {
       [ studyCount: Study.count() ]
    }
}
