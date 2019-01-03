<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="sammain" />
    <title>SAM Feature Mapper</title>

    <r:require modules="mapper,gscfimporter"/>
</head>
<body>
    <div class="basicTabLayout importer">
        <h1>
            <span class="truncated-title">
                Feature Mapper for ${module}
            </span>
        </h1>

        <span class="message info">
            %{--<span class="title">Choose platform</span>--}%
            This tool assists in looking up identifiers for feature names with the help of <a href="https://bioportal.bioontology.org" target="_blank">BioPortal</a>, <a href="https://www.ebi.ac.uk/ols/index" target="_blank">OLS</a> and all linked sources.<br/>
            The ontology selected is the default ontology for this ${module} module, only change this parameter when it is strictly necessary.
        </span>

        <g:if test="${apiKeyConfigured}">
            <g:form action="listFeatures" id="platformForm" name="platformForm">
                <g:hiddenField name="module" value="${module}"/>
                <fieldset>
                    <legend>Parameters</legend>
                    <div class="element">
                        <div class="description">
                            Select platform
                        </div>
                        <div class="input">
                            <g:select name="platformId" from="${platformList}" optionKey="id" optionValue="name"></g:select>
                        </div>
                    </div>
                    <div class="element">
                        <div class="description">
                            Select ontology
                        </div>
                        <div class="input">
                            <g:select name="ontology" from="${['AAO', 'ABA-AMB', 'ABD', 'ACGT-MO', 'ADAR', 'ADMIN', 'ADO', 'ADW', 'AEO', 'AERO', 'AGRO', 'AI-RHEUM', 'ALLERGYDETECTOR', 'AMINO-ACID', 'ANCESTRO', 'AO', 'AOPO', 'APACOMPUTER', 'APADISORDERS', 'APAEDUCLUSTER', 'APANEUROCLUSTER', 'APAOCUEMPLOY', 'APAONTO', 'APASTATISTICAL', 'APATANDT', 'APATREATMENT', 'APO', 'ARD', 'ARO', 'ASDPTO', 'ATC', 'ATO', 'ATOL', 'AURA', 'BAO', 'BAO-GPCR', 'BATHMATE', 'BCGO', 'BCO', 'BCTEO', 'BCTT', 'BCTTV1', 'BDO', 'BFO', 'BHN', 'BHO', 'BIM', 'BIOETHICS', 'BIOMO', 'BIOMODELS', 'BIRNLEX', 'BMT', 'BNO', 'BOF', 'BP', 'BP-METADATA', 'BRCT', 'BRIDG', 'BRO', 'BRO_ACRONYM', 'BSAO', 'BSPO', 'BT', 'BTO', 'CABRO', 'CANCO', 'CANONT', 'CAO', 'CARELEX', 'CARO', 'CARRE', 'CBO', 'CCO', 'CCON', 'CCONT', 'CDAO', 'CEDARPC', 'CEDARVS', 'CEPH', 'CHD', 'CHEBI', 'CHEMBIO', 'CHEMINF', 'CHMO', 'CIINTEADO', 'CIO', 'CISAVIADO', 'CL', 'CLO', 'CMO', 'CMPO', 'CN', 'CNO', 'CNO_ACRONYM', 'CO', 'CO-WHEAT', 'COGAT', 'COGPO', 'COMODI', 'COSTART', 'CO_320', 'CO_321', 'CO_322', 'CO_323', 'CO_324', 'CO_325', 'CO_327', 'CO_330', 'CO_331', 'CO_333', 'CO_334', 'CO_335', 'CO_336', 'CO_337', 'CO_338', 'CO_339', 'CO_340', 'CO_341', 'CO_343', 'CO_345', 'CO_346', 'CO_347', 'CO_348', 'CO_350', 'CO_356', 'CO_357', 'CO_360', 'CPRO', 'CPT', 'CPTAC', 'CRISP', 'CRO', 'CSEO', 'CSO', 'CSSO', 'CTCAE', 'CTENO', 'CTO', 'CTONT', 'CTX', 'CU-VO', 'CVDO', 'CYTO', 'DCM', 'DCO', 'DCO-DEBUGIT', 'DDANAT', 'DDI', 'DDO', 'DDPHENO', 'DERMLEX', 'DERMO', 'DIAB', 'DIAGONT', 'DIDEO', 'DIKB', 'DINTO', 'DLORO', 'DOCCC', 'DOID', 'DRON', 'DUO', 'ECG', 'ECO', 'ECOCORE', 'ECP', 'EDAM', 'EDAMTO', 'EDDA', 'EFO', 'EGO', 'EHDA', 'EHDAA', 'EHDAA2', 'ELIG', 'EMAP', 'EMAPA', 'EMO', 'ENM', 'ENTITY', 'ENTITYCANDIDATES', 'ENVO', 'EO', 'EOL', 'EP', 'EPILONT', 'EPO', 'ERO', 'ESSO', 'EUPATH', 'EXACT', 'EXO', 'FALL', 'FAO', 'FB-BT', 'FB-CV', 'FB-DV', 'FB-SP', 'FBBI', 'FBBT', 'FBCV', 'FBDV', 'FBbi', 'FHHO', 'FIRE', 'FIX', 'FLOPO', 'FLU', 'FMA', 'FO', 'FOODON', 'FTC', 'FYPO', 'GALEN', 'GANTENGJADI', 'GAZ', 'GBM', 'GCO', 'GENE-CDS', 'GENEPIO', 'GENO', 'GEO', 'GEOSPECIES', 'GEXO', 'GFO', 'GFO-BIO', 'GFVO', 'GLYCO', 'GLYCORDF', 'GMM', 'GMO', 'GO', 'GO-EXT', 'GO-PLUS', 'GPML', 'GRO', 'GRO-CPD', 'GRO-CPGA', 'GTO', 'GTO_TESTING', 'HAAURAADO', 'HAO', 'HAROREADO', 'HC', 'HCPCS', 'HEIO', 'HFO', 'HINO', 'HIV', 'HIVO004', 'HL7', 'HOM', 'HP', 'HPIO', 'HRDO', 'HSAPDV', 'HUGO', 'HUPSON', 'IAO', 'ICD10', 'ICD10CM', 'ICD10PCS', 'ICD11-BODYSYSTEM', 'ICD9CM', 'ICECI', 'ICF', 'ICNP', 'ICO', 'ICPC', 'ICPC2P', 'ICPS', 'IDMP', 'IDO', 'IDOBRU', 'IDODEN', 'IDOMAL', 'IDQA', 'IFAR', 'IGTO', 'IMGT-ONTOLOGY', 'IMMDIS', 'INM-NPI', 'INO', 'INSECTH', 'INVERSEROLES', 'ISO-15926-2_2003', 'ISO-ANNOTATIONS', 'IXNO', 'InterNano', 'JERM', 'KISAO', 'LBO', 'LDA', 'LEGALAPA', 'LEGALAPATEST2', 'LHN', 'LIPRO', 'LOINC', 'LPT', 'MA', 'MAMO', 'MAT', 'MATR', 'MATRCOMPOUND', 'MATRELEMENT', 'MATRROCK', 'MATRROCKIGNEOUS', 'MCBCC', 'MCCL', 'MCCV', 'MDDB', 'MEDDRA', 'MEDEON', 'MEDLINEPLUS', 'MEDO', 'MEGO', 'MEO', 'MERA', 'MESH', 'MF', 'MFMO', 'MFO', 'MFOEM', 'MFOMD', 'MHC', 'MI', 'MIAPA', 'MIM', 'MINERAL', 'MIRNAO', 'MIRO', 'MIXS', 'MIXSCV', 'MMO', 'MMUSDV', 'MNR', 'MO', 'MOC', 'MOD', 'MONDO', 'MOOCCIADO', 'MOOCCUADO', 'MOOCULADO', 'MOP', 'MP', 'MPATH', 'MPIO', 'MRO', 'MS', 'MSO', 'MSTDE', 'MSTDE-FRE', 'MSV', 'MWLA', 'NATPRO', 'NBO', 'NCBITAXON', 'NCCO', 'NCIT', 'NCRO', 'NDDF', 'NDFRT', 'NEMO', 'NEOMARK3', 'NEOMARK4', 'NEUMORE', 'NGSONTO', 'NHSQI2009', 'NIC', 'NIDM-RESULTS', 'NIFCELL', 'NIFDYS', 'NIFSTD', 'NIFSUBCELL', 'NIGO', 'NIHSS', 'NLMVS', 'NMOBR', 'NMOSP', 'NMR', 'NONRCTO', 'NORREG', 'NPI', 'NPO', 'NTDO', 'OAE', 'OARCS', 'OBA', 'OBATKEPUTIHAN', 'OBCS', 'OBI', 'OBIB', 'OBIWS', 'OBI_BCGO', 'OBOE-SBC', 'OBOREL', 'OCHV', 'OCRE', 'ODNAE', 'OF', 'OFSMR', 'OGDI', 'OGG', 'OGG-MM', 'OGI', 'OGMD', 'OGMS', 'OGR', 'OGSF', 'OHD', 'OHMI', 'OLATDV', 'OMIABIS', 'OMIM', 'OMIT', 'OMP', 'OMRSE', 'ONL-DP', 'ONL-MR-DA', 'ONL-MSA', 'ONLIRA', 'ONSTR', 'ONTOAD', 'ONTODM-CORE', 'ONTODM-KDD', 'ONTODT', 'ONTOKBCF', 'ONTOLURGENCES', 'ONTOMA', 'ONTONEO', 'ONTOPNEUMO', 'ONTOTOXNUC', 'OOEVV', 'OOSTT', 'OPB', 'OPE', 'OPL', 'ORDO', 'ORTH', 'OVAE', 'OntoVIP', 'PAE', 'PATHLEX', 'PATO', 'PAV', 'PCO', 'PDO', 'PDON', 'PDO_CAS', 'PDQ', 'PDRO', 'PDUMDV', 'PE', 'PEAO', 'PECO', 'PEDTERM', 'PEO', 'PHAGE', 'PHARE', 'PHARMGKB', 'PHENOMEBLAST', 'PHENX', 'PHFUMIADO', 'PHMAMMADO', 'PHYLOGENETICS', 'PHYLONT', 'PIERO', 'PLANA', 'PLIO', 'PLOSTHES', 'PMA', 'PMR', 'PO', 'PORO', 'PP', 'PPIO', 'PPO', 'PR', 'PRIDE', 'PROBONTO', 'PROCCHEMICAL', 'PROPREO', 'PROVO', 'PSDS', 'PSEUDO', 'PSIMOD', 'PTO', 'PTRANS', 'PTS', 'PVONTO', 'PW', 'PXO', 'QIBO', 'QUDT', 'RADLEX', 'RAO', 'RB', 'RCD', 'RCTONT', 'RCTV2', 'RDL', 'REPO', 'RETO', 'REX', 'REXO', 'RH-MESH', 'RNAO', 'RNPRIO', 'RNRMU', 'RO', 'ROLEO', 'ROO', 'RPO', 'RS', 'RSA', 'RXNO', 'RXNORM', 'SACHAN', 'SAO', 'SBO', 'SBOL', 'SCDO', 'SCHEMA', 'SD3', 'SDO', 'SEDI', 'SEP', 'SEPIO', 'SHR', 'SIBO', 'SIO', 'SITBAC', 'SMASH', 'SNMI', 'SNOMEDCT', 'SNOMED_CT', 'SNPO', 'SO', 'SOPHARM', 'SOY', 'SP', 'SPD', 'SPO', 'SPTO', 'SSE', 'SSO', 'STAFOR', 'STATO', 'STUFF', 'STY', 'SUICIDEO', 'SURGICAL', 'SWEET', 'SWO', 'SYMP', 'SYN', 'TADS', 'TAO', 'TAXRANK', 'TEDDY', 'TEO', 'TESTE', 'TEST_TEST', 'TGMA', 'TM-CONST', 'TM-MER', 'TM-OTHER-FACTORS', 'TM-SIGNS-AND-SYMPTS', 'TMA', 'TMO', 'TO', 'TOK', 'TOP-MENELAS', 'TRAK', 'TRANS', 'TRIAGE', 'TRON', 'TSTONTJV', 'TTO', 'TTT', 'TYPON', 'UBERON', 'UNIMOD', 'UNITSONT', 'UO', 'UPHENO', 'VANDF', 'VARIO', 'VHOG', 'VICO', 'VIVO', 'VIVO-ISF', 'VO', 'VSAO', 'VSO', 'VT', 'VTO', 'WB-BT', 'WB-LS', 'WB-PHENOTYPE', 'WBBT', 'WBLS', 'WBPHENOTYPE', 'WHO-ART', 'WIKIPATHWAYS', 'WSIO', 'XAO', 'XCO', 'XEO', 'XL', 'ZEA', 'ZECO', 'ZFA', 'ZFS']}" value="CHEBI"/>
                        </div>
                    </div>
                    <br clear="all" />
                </fieldset>

                <p class="options">
                    <a href="#" onClick="$('#platformForm').submit()" class="next">Next</a>
                </p>
            </g:form>
        </g:if>
        <g:else>
            <fieldset>
                    <legend>Error</legend>
                    API key not set, please contact an admin.
            </fieldset>
        </g:else>
    </div>
</body>
</html>
