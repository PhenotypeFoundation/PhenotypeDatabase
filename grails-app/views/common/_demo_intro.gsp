<%
/**
 * Demo instance introduction partial
 *
 * @author Jeroen Wesbeek
 * @since 20101111
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
	<p>
	Welcome to GSCF demo version <b>${meta(name: 'app.version')}</b>. At this moment, there are ${studyCount} studies and ${userCount} users in the database.
	</p>

	<p>
	This application will facilitate systems biological research and collaboration between researchers at various locations. This application is a combined effort of the <a href="http://www.nugo.org/everyone">Nutrigenomics Organization (NuGO)</a> the <a href="http://www.metabolomicscentre.nl">Netherlands Metabolomics Centre (NMC)</a>, <a href="http://www.eurreca.org">European Micronutrient recommandations Aligned (Eurreca)</a>, the <a href="http://www.tno.nl">Netherlands Organization for Applied Scientific Research (TNO)</a> and the <a href="http://www.nbic.nl/">Netherlands Bioinformatics Centre (NBIC)</a>. The GSCF is part of the Nutritional Phenotype Database described by <a href="http://www.springerlink.com/content/1555-8932">van Ommen et al, 2010: The Nutritional Phenotype Database. Genes and Nutrition. DOI: 10.1007/s12263-010-0167-9</a>.

	This study capturing module can be easily linked to assay specific modules and therefore can be reused for new technologies. This application is built for the easy input/storage and retrieval of studies. Studies can be stored with high detail and the type for information being stored can be field (e.g. human, mouse, plant) specific. Complex designs like studies with multiple doses, sampling time points and challenge tests, can be stored in this system.
	Studies will only be accessible for people that are specified by the study owner.
	</p>

	<p>
	<i>
	To be able to create, view or search studies, please log on (demo user: user / useR123! ) or register at the right top corner of this page.
	</i>
	</p>