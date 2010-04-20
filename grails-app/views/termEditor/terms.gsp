<%@ page contentType="text/html;charset=UTF-8" %>
<html>
 <head>
  <meta name="layout" content="dialog"/>
  <title>my bla</title>
 </head>
 <body>
 hoi, dit is <b>http://localhost:8080/gscf/termEditor</b> en ik heb deze ontology NCBO id's meegekregen:

 <ul>
 <g:each in="${ontologies}">
     <li>${it}</li>
 </g:each>
 </ul>

 Deze wizard moet dus:
 <ul>
 <li>de mogelijkheid bieden om ontologies toe te voegen (@see Ontology domain class)</li>
 <li>de mogelijkheid bieden terms toe te voegen uit bepaalde ontologies</li>
 <li>na sluiten van de popup (zie wizard subject page):
 	<li>óf de select dynamisch te updaten met toegevoegde velden</li>
 	<li>óf de pagina te 'refreshen' --> overleg met Jeroen</li>
 </li>
 </ul>

 Overige handige info:
 <ul>
	 <li><a href="/gscf/js/SelectAddMore.js">SelectAddMore.js</a></li>
	 <li><a href="/gscf/js/wizard.js">wizard.js</a></li>
	 <li><a href="/gscf/js/ontology-chooser.js">ontology-chooser.js</a></li>
	 <li><a href="http://www.grails.org/WebFlow">Grails WebFlow</a></li>
	 <li><a href="http://bioportal.bioontology.org/ontologies/">ontologies</a></li>
 </ul>

<i>NB: deze iFrame gebruikt een aantal HTML5 opties (seamless en sanbox)! Even goed naar kijken en op letten!</i><br/>
<i>NB2: wbt de study create wizard. Momenteel kan je in de 'subjects' pagina bij 'species' deze pagina lanceren in een iFrame
door 'add more...' te kiezen. Die is dynamisch toegevoegd door de volgende regel in wizard.js: <b>new SelectAddMore().init('term','/gscf/termEditor','ontology',function(scope) { refreshWebFlow(); });</b>
welke SelectAddMore(..) instantieert. Deze regel voegt aan alle select elementen met een <b>rel='term'</b> een 'add more..' option toe. Als die wordt aangeklikt
wordt een jQuery-ui dialog uitgevoerd die (in dit geval) '/gscf/termEditor'. Het select element heeft een 'ontology="..."'
parameter welke als GET parameter aan het iFrame wordt meegegeven. Zo 'weet' de term/ontology editor ook welke ontologien hij
precies moet aanbieden in de iframe. Zodra het iframe wordt gesloten wordt de javascript uitgevoerd. In dit geval dus 'refreshWebFlow();'.
Deze functie zorgt er voor dat de wizard webflow refresht waardoor de wijzigingen die je hebt gemaakt in de iframe ook zichtbaar
worden in de wizard....
</i>

<!--
Skype 'Chat with me' button
http://www.skype.com/go/skypebuttons
-->
<script type="text/javascript" src="http://download.skype.com/share/skypebuttons/js/skypeCheck.js"></script>
<a href="skype:duhcati?chat"><img src="http://download.skype.com/share/skypebuttons/buttons/chat_blue_transparent_97x23.png" style="border: none;" width="97" height="23" alt="Chat with me" /></a>
 

 </body>
</html>