package org.dbxp.moduleBase

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import grails.test.GrailsUnitTestCase
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Class to mock a webservice
 *
 * see: http://cantina.co/2010/11/05/putting-rest-in-your-tests-for-grails/
 */
class MockWeb {
    private HttpServer server

    String response
    String contentType = 'text/html'
    Integer status = 200

    MockWeb(Integer port, String context) {
        server = HttpServer.create(new InetSocketAddress(port), 0)
        server.createContext(context,
                { HttpExchange exchange ->
                    exchange.responseHeaders.'Content-Type' = contentType
                    exchange.sendResponseHeaders(status, response.bytes.length)
                    exchange.responseBody.write(response.bytes)
                    exchange.responseBody.close()
                } as HttpHandler)

        server.start()
    }

    void stop() { server?.stop(0) }
}

class GscfServiceTests extends GrailsUnitTestCase {

    def service
    def port = 9999
    def baseURL = "http://localhost:$port"
    def serverURL = 'http://server'
    def consumerId = 'consumerABC'
    def registerSearchPath = 'searchPath'
    def addStudyPath = 'addStudyPath'
    def studyToken = 'someStudyToken'
    def sessionToken = 'sessionABC'


    MockWeb mockWeb

    protected void setUp() {
        super.setUp()
        service = new GscfService()
        service.config = [
                gscf:   [   baseURL: baseURL,
                            registerSearchPath: registerSearchPath,
                            addStudyPath: addStudyPath],
                grails: [serverURL: serverURL],
                module: [consumerId: consumerId]
        ]

        // mock the encodeAsURL method on String because it's not available when running unit tests
        String.metaClass['encodeAsURL'] = {delegate}

        def request = new MockHttpServletRequest('GET','')
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))

        service.metaClass.log = [info:{println it}, error:{msg, e -> println msg}, warning: {println()}]
    }

    protected void tearDown() {
        super.tearDown()
        mockWeb?.stop()
    }

    void testUrlAuthRemoteWithoutParams() {

        def token = 'tokenABC'
        def params = null

        assert service.urlAuthRemote(params, token) == 'http://localhost:9999/login/auth_remote?moduleURL=http://server&consumer=consumerABC&token=tokenABC&returnUrl=http://server'

    }

    void testUrlAuthRemoteWithParams() {

        def token = 'tokenABC'
        def params = [a: 1, b: ['abc',3], controller: 'someController', id: '1']

        assert service.urlAuthRemote(params, token) == 'http://localhost:9999/login/auth_remote?moduleURL=http://server&consumer=consumerABC&token=tokenABC&returnUrl=http://server/someController?synchronizeAuthorization=true&a=1&b=abc&b=3&id=1'

        params.action = 'someAction'

        assert service.urlAuthRemote(params, token) == 'http://localhost:9999/login/auth_remote?moduleURL=http://server&consumer=consumerABC&token=tokenABC&returnUrl=http://server/someController/someAction/1?synchronizeAuthorization=true&a=1&b=abc&b=3'

    }

    void testIsUserLoggedIn() {

        mockWeb = new MockWeb(port, '/rest/isUser')

        mockWeb.response = '{}'

        assert !service.isUserLoggedIn(sessionToken)

        mockWeb.response = '{authenticated: true}'

        assert service.isUserLoggedIn(sessionToken)

        mockWeb.response = '{authenticated: false}'

        assert !service.isUserLoggedIn(sessionToken)

    }

    void testGetUser() {

        mockWeb = new MockWeb(port, '/rest/getUser')

        mockWeb.response = '{username: someUserName, id: 13}'

        assert service.getUser(sessionToken) as HashSet == [username: 'someUserName', id: 13, isAdministrator: false] as HashSet

        mockWeb.response = '{username: someUserName, id: 13, isAdministrator: true}'

        assert service.getUser(sessionToken) as HashSet == [username: 'someUserName', id: 13, isAdministrator: true] as HashSet

    }

    void testGetStudies() {

        mockWeb = new MockWeb(port, '/rest/getStudies')

        mockWeb.response = '[{"title":"NuGO PPS3 mouse study leptin module","studyToken":"PPS3_leptin_module",\
"startDate":"2008-01-01T23:00:00Z","published":false,"Description":"C57Bl/6 mice were fed a high fat (45 en%)\
or low fat (10 en%) diet after a four week run-in on low fat diet.","Objectives":null,"Consortium":null,\
"Cohort name":null,"Lab id":null,"Institute":null,"Study protocol":null},\
{"title":"NuGO PPS human study","studyToken":"PPSH","startDate":"2008-01-13T23:00:00Z","published":false,\
"Description":"Human study performed at RRI; centres involved: RRI, IFR, TUM, Maastricht U.","Objectives":null,\
"Consortium":null,"Cohort name":null,"Lab id":null,"Institute":null,"Study protocol":null}]'


        def a = service.getStudies(sessionToken)
        def b = [[startDate:'2008-01-01T23:00:00Z', Description:'C57Bl/6 mice were fed a high fat (45 en%)or low fat (10 en%) diet after a four week run-in on low fat diet.', title:'NuGO PPS3 mouse study leptin module', Objectives:null, 'Lab id':null, studyToken:'PPS3_leptin_module', Consortium:null, 'Study protocol':null, published:false, Institute:null, 'Cohort name':null], [startDate:'2008-01-13T23:00:00Z', Description:'Human study performed at RRI; centres involved: RRI, IFR, TUM, Maastricht U.', title:'NuGO PPS human study', Objectives:null, 'Lab id':null, studyToken:'PPSH', Consortium:null, 'Study protocol':null, published:false, Institute:null, 'Cohort name':null]]

        assert a == b

        a = service.getStudies(sessionToken, ['PPS3_leptin_module', 'PPSH'])

        assert a.toString() == b.toString()
    }

    void testGetStudy() {

        mockWeb = new MockWeb(port, '/rest/getStudies')

        mockWeb.response = '{"title":"NuGO PPS3 mouse study leptin module","studyToken":"PPS3_leptin_module",\
"startDate":"2008-01-01T23:00:00Z","published":false,"Description":"C57Bl/6 mice were fed a high fat (45 en%)\
or low fat (10 en%) diet after a four week run-in on low fat diet.","Objectives":null,"Consortium":null,\
"Cohort name":null,"Lab id":null,"Institute":null,"Study protocol":null}'

        def a = service.getStudy(sessionToken, 'PPS3_leptin_module')
        def b = [startDate:'2008-01-01T23:00:00Z', Description:'C57Bl/6 mice were fed a high fat (45 en%)or low fat (10 en%) diet after a four week run-in on low fat diet.', title:'NuGO PPS3 mouse study leptin module', Objectives:null, 'Lab id':null, studyToken:'PPS3_leptin_module', Consortium:null, 'Study protocol':null, published:false, Institute:null, 'Cohort name':null]

        assert a==b
    }

    void testUrlViewStudy() {
        assert service.urlViewStudy(studyToken) == baseURL + '/study/showByToken/' + studyToken
    }


    void testUrlAddStudy() {
        assert service.urlAddStudy() == baseURL + addStudyPath
    }

    void testUrlRegisterSearch() {
        assert service.urlRegisterSearch() == baseURL + registerSearchPath
    }

    void testModuleURL() {
        assert service.moduleURL() == serverURL
    }

    void testConsumerId() {
        assert service.consumerId() == consumerId
    }

    void testRestURL() {
        assert service.restURL() == baseURL + '/rest'
    }
}
