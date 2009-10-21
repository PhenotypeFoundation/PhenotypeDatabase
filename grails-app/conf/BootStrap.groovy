import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil

/**
 * Application Bootstrapper
 * @Author  Jeroen Wesbeek
 * @Since   20091021
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class BootStrap {
     def init = { servletContext ->
	 // check if we're in development
	 if (GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) {
	     printf("development bootstrapping....\n\n");

	     // add roles
	     def AuthRole1 = new AuthRole(name:'Administrator', description:'Super user').save();
	     def AuthRole2 = new AuthRole(name:'Group Administrator', description:'Group Super user').save();
	     def AuthRole3 = new AuthRole(name:'Study Owner', description:'The creator of a study').save();
	     
	     // add actions
	     def AuthAction1 = new AuthAction(controller:'test', action:'index').save();
	     def AuthAction2 = new AuthAction(controller:'test', action:'sayHello').save();
	     def AuthAction3 = new AuthAction(controller:'test', action:'sayWeather').save();
	     
	     // authorize super user for everything
	     AuthRole1.addToActions(AuthAction1).save();
	     AuthRole1.addToActions(AuthAction2).save();
	     AuthRole1.addToActions(AuthAction3).save();

	     // authorize group admin only for index and hello
	     AuthRole2.addToActions(AuthAction1).save();
	     AuthRole2.addToActions(AuthAction2).save();

	     // authorize study owner only for index
	     AuthRole3.addToActions(AuthAction1).save();

	     // add users
	     def User1 = new AuthUser(username:'admin', password:'admin', firstName:'super', lastName:'User', email:'info@osx.eu').save();
	     def User2 = new AuthUser(username:'duh', password:'duh', firstname:'Jeroen', lastname:'Wesbeek', email:'j.a.m.wesbeek@umail.leidenuniv.nl').save();

	     // add group structure
	     def AuthGroup1 = new AuthGroup(name:'root', description:'the root of everything').save();
	     def AuthGroup2 = new AuthGroup(name:'TNO', description:'TNO - nation wide company').save();
	     def AuthGroup3 = new AuthGroup(name:'KVL', description:'TNO - quality of life').save();
	     def AuthGroup4 = new AuthGroup(name:'BSC', description:'BioSciences').save();

	     // create group tree 4 -> 3 -> 2 -> 1
	     AuthGroup4.addToGroups(AuthGroup3).save();
	     AuthGroup3.addToGroups(AuthGroup2).save();
	     AuthGroup2.addToGroups(AuthGroup1).save();

	     // add users to groups
	     //User1.addToGroups(AuthGroup1).save();
	     //User2.addToGroups(AuthGroup4).save();

	     
	 }
     }
     def destroy = {
     }
} 