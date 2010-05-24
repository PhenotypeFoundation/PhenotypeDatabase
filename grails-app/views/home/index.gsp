<html>
    <head>
      <title>Generic Study Capture Framework</title>
      <meta name="layout" content="main" />
    </head>
    <body>
		Welcome to the first prototype of GSCF, version <b>${meta(name: 'app.version')}</b>. At this moment, there are ${studyCount} studies in the database.
	<p>
        This application will facilitate systems biological research and collaboration between researchers at various locations. This application is a combined effort of NuGO (Nutritional Phenotype Database; van Ommen et al, 2010, Genes and Nutrition) and NMC (Data Support Platform), Eurreca, TNO and NBIC. This study capturing module can be easily linked to assay specific modules and therefore can be reused for new technologies.
		This application is built for the easy input/storage and retrieval of studies. Studies can be stored with high detail and the type for information being stored can be field (e.g. human, mouse, plant) specific. Complex designs like studies with multiple doses, sampling time points and challenge tests, can be stored in this system.
		Studies will only be accessible for people that are specified by the study owner.
    </p>
    <p><n:isNotLoggedIn>
	    To be able to create, view or search studies, please log on or register at the right top corner of this page.
        NB: For this (test) version it is not required to login, but if you login as administrator ( admin / admiN123! ) or user ( user / useR123! ) you can test user functionality.
      </n:isNotLoggedIn></p>
    Choose from the upper bar whether you would like to create, view or search studies

  </body>
</html>