package org.dbxp.sam



import org.junit.*
import grails.test.mixin.*

@TestFor(PlatformController)
@Mock(Platform)
class PlatformControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/platform/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.platformInstanceList.size() == 0
        assert model.platformInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.platformInstance != null
    }

    void testSave() {
        controller.save()

        assert model.platformInstance != null
        assert view == '/platform/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/platform/show/1'
        assert controller.flash.message != null
        assert Platform.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/platform/list'

        populateValidParams(params)
        def platform = new Platform(params)

        assert platform.save() != null

        params.id = platform.id

        def model = controller.show()

        assert model.platformInstance == platform
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/platform/list'

        populateValidParams(params)
        def platform = new Platform(params)

        assert platform.save() != null

        params.id = platform.id

        def model = controller.edit()

        assert model.platformInstance == platform
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/platform/list'

        response.reset()

        populateValidParams(params)
        def platform = new Platform(params)

        assert platform.save() != null

        // test invalid parameters in update
        params.id = platform.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/platform/edit"
        assert model.platformInstance != null

        platform.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/platform/show/$platform.id/"
        assert flash.message != null

        //test outdated version number
        response.reset()
        platform.clearErrors()

        populateValidParams(params)
        params.id = platform.id
        params.version = -1
        controller.update()

        assert view == "/platform/edit"
        assert model.platformInstance != null
        assert model.platformInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/platform/list'

        response.reset()

        populateValidParams(params)
        def platform = new Platform(params)

        assert platform.save() != null
        assert Platform.count() == 1

        params.id = platform.id

        controller.delete()

        assert Platform.count() == 0
        assert Platform.get(platform.id) == null
        assert response.redirectedUrl == '/platform/list'
    }
}
