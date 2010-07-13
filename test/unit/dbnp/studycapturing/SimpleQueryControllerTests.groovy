package dbnp.studycapturing

import grails.test.ControllerUnitTestCase
import dbnp.query.SimpleQueryController

/**
 * Created by IntelliJ IDEA.
 * User: luddenv
 * Date: 12-jul-2010
 * Time: 11:29:18
 * To change this template use File | Settings | File Templates.
 */
class SimpleQueryControllerTests extends ControllerUnitTestCase {

  protected void setUp() {
      super.setUp()
  }

  protected void tearDown() {
      super.tearDown()
  }

  void testMergeResults () {
      def list1 = [1, 2, 3, 4]
      def list2 = [1, 3, 5]

      def list3 = SimpleQueryController.mergeLists(list1, list2)

      assert list3 == [1, 3]
  }

}
