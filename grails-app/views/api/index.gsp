<html>
<head>
    <meta name="layout" content="main"/>
    <style type="text/css">
        .api {
            margin-top: -40px;
        }

        .api .header {
            color: #ffda27;
            font-size: 24px;
            height: 40px;
        }

        .api h1 {
            background-color: #006DBA;
            padding-left: 10px;
            margin-top: 40px;
            height: 30px;
            padding-top: 10px;
            color: #fff;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.68);
        }

        .api h2 {
            font-size: 12px;
            background-color: #d7e6f1;
            padding-left: 10px;
            margin-top: 10px;
            height: 20px;
            padding-top: 5px;
            font-weight: bold;
            color: #006DBA;
            text-shadow: 0 1px 1px rgba(0, 0, 0, 0.28);
        }

        .api h3 {
            font-size: 12px;
            font-weight: bold;
            color: #ee7624;
            text-shadow: 0 1px 1px rgba(0, 0, 0, 0.28);
        }
        
        .api li {
            margin-left: 30px;
        }
    </style>
</head>
<body>
<div class="api">
<h1 class="header">API specification</h1>

The API allows third party software to interface with GSCF and connected modules.

<h2>prerequisites</h2>
    <li>a valid username / password with role ROLE_CLIENT (see <a href="#authenticate">authenticate</a>)</li>
    <li>an api key (used to calculate the validation md5 hash.
        <sec:ifLoggedIn>
            Get your api key <g:link controller="userRegistration" action="profile">here</g:link>
        </sec:ifLoggedIn>
        <sec:ifNotLoggedIn>
            Login to get your api key
        </sec:ifNotLoggedIn>
    )</li>
    <li>a deviceID / clientID (look <a href="https://github.com/4np/UIDevice-with-UniqueIdentifier-for-iOS-5" target="_new">here</a> for iOS)</li>

<h2>available API calls</h2>
    <li><a href="#authenticate">authenticate</a> - set up / synchronize client-server session</li>
    <li><a href="#getStudies">getStudies</a> - fetch all (readable) studies</li>
    <li><a href="#getSubjectsForStudy">getSubjectsForStudy</a> - fetch all subjects in a given study</li>
    <li><a href="#getAssaysForStudy">getAssaysForStudy</a> - fetch all assays in a given study</li>
    <li><a href="#getSamplesForAssay">getSamplesForAssay</a> - fetch all samples in a given assay</li>
    <li><a href="#getMeasurementDataForAssay">getMeasurementDataForAssay</a> - fetch all measurement data for a given assay</li>

<h2>SDK packages</h2>
    <li><a href="https://github.com/4np/gscf4php" target="_new">PHP</a> - Object Oriented SDK for interacting with GSCF</li>

<a name="authenticate"></a>
<h1>authenticate</h1>
<h3>url: <g:createLink controller="api" action="authenticate" absolute="true" /></h3>
<p>
    Authenticate a client using <a href="http://en.wikipedia.org/wiki/Basic_access_authentication" target="_new">HTTP BASIC authentication</a>.
    This API call is used to:
    <li>initially set up a client/server session</li>
    <li>re-synchronise client/server sessions that become out of sync (e.g. <i>sequence</i> differences)</li>
<p>

<p>
    After successful authentication, a session token is returned which should the client should store locally. This session token
    should be used in all subsequent calls to calculate the validation md5 hash.
</p>
<p>
    This call should also be performed whenever a client/server sessions becomes out of sync (e.g. the client's sequence count
    differs from the server's sequence count) as the server's sequence count will be returned after successfully authenticating.
    For security reasons this api method is designed to be called only once (or when sessions are out of sync) as HTTP BASIC authentication
    is not really secure (if someone is able to sniff your traffic, the authentication md5 hash is easily stolen). API calls are
    validated using the calculated md5 hash.
</p>
<p>
    Every subsequent request the client does, needs to contain the validation MD5 hash, which is a MD5 sum of the concatenation of the device token,
    the request sequence and the api key (e.g. <i>md5sum( token + sequence + api key )</i> ).<br/>
    <i>Note that in order to be able to successfully authenticate or use the API in general, the user should have the ROLE_CLIENT assigned!</i>

    <h2>Request parameters</h2>
    <table>
        <thead>
            <th>argument</th>
            <th>type</th>
            <th>length</th>
            <th>description</th>
            <th>example</th>
            <th>required</th>
        </thead>
        <tr>
            <td>deviceID</td>
            <td>string</td>
            <td>32</td>
            <td>a unique ID of the client device / application performing the call (<a href="https://github.com/4np/UIDevice-with-UniqueIdentifier-for-iOS-5" target="_new">iOS example</a>)</td>
            <td>9ae87836-d38d-4b86-be6a-eff93f2b049a</td>
            <td>yes</td>
        </tr>
    </table>

    <h2>Reply parameters</h2>
    <table>
        <thead>
            <th>argument</th>
            <th>type</th>
            <th>length</th>
            <th>description</th>
            <th>example</th>
        </thead>
        <tr>
            <td>token</td>
            <td>string</td>
            <td>36</td>
            <td>a unique token for setting up a client session</td>
            <td>9ae87836-d38d-4b86-be6a-eff93f2b049a</td>
        </tr>
        <tr>
            <td>sequence</td>
            <td>int</td>
            <td>-</td>
            <td>the api call count for this session</td>
            <td>231</td>
        </tr>
    </table>

    <h2>example reply</h2>
    <blockquote>
        {"token":"78b070a6-e68c-436e-a81b-2db08840e809","sequence":0}
    </blockquote>
</p>

<a name="getStudies"></a>
<h1>getStudies</h1>
<h3>url: <g:createLink controller="api" action="getStudies" absolute="true" /></h3>
<p>
    Returns the studies which are <i>readable</i> and/or <i>writable</i> for the client. If the client should get access to a particular
    study, the client's username (used to authenticate) should be added as a reader to the study.

    <h2>Request parameters</h2>
    <table>
        <thead>
            <th>argument</th>
            <th>type</th>
            <th>length</th>
            <th>description</th>
            <th>example</th>
            <th>required</th>
        </thead>
        <tr>
            <td>deviceID</td>
            <td>string</td>
            <td>36 (max)</td>
            <td>a unique ID of the client device / application performing the call</td>
            <td>9ae87836-d38d-4b86-be6a-eff93f2b049a</td>
            <td>yes</td>
        </tr>
        <tr>
            <td>validation</td>
            <td>string</td>
            <td>-</td>
            <td><a href="http://www.miraclesalad.com/webtools/md5.php" target="_new">md5sum</a>( token + sequence + api key )</td>
            <td>9ae87836d38d4b86be6aeff93f2b049a</td>
            <td>yes</td>
        </tr>
    </table>

    <h2>example reply</h2>
    <blockquote>
        {"count":2,"studies":[{"token":"f2e085fb-9138-4ebe-a59f-82f1bdc21d7e","title":"NuGO PPS human study","description":"Human study performed at RRI; centres involved: RRI, IFR, TUM, Maastricht U.","subjects":11,"species":["Homo sapiens"],"assays":["Mass Sequencing module","SAM module for clinical data","Metabolomics module"],"events":1,"uniqueEvents":["start: 3 days, 22 hours, duration: 8 hours"],"samplingEvents":2,"uniqueSamplingEvents":["start: 0 seconds","start: 4 days, 6 hours"],"eventGroups":1,"uniqueEventGroups":["Root group"],"samples":22},{"token":"6b7e85b3-b174-492c-ba27-fbfb71ab9b8b","title":"NuGO PPS3 mouse study leptin module","description":"C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet.","subjects":80,"species":["Mus musculus"],"assays":["SAM module for clinical data","Metabolomics module"],"events":8,"uniqueEvents":["start: 1 hour, duration: 1 week","start: 1 hour, duration: 4 weeks"],"samplingEvents":2,"uniqueSamplingEvents":["start: 1 week, 1 hour"],"eventGroups":8,"uniqueEventGroups":["10% fat + vehicle for 1 week","10% fat + leptin for 1 week","45% fat + vehicle for 1 week","45% fat + leptin for 1 week","10% fat + vehicle for 4 weeks","10% fat + leptin for 4 weeks","45% fat + vehicle for 4 weeks","45% fat + leptin for 4 weeks"],"samples":80}]}
    </blockquote>
</p>

<a name="getSubjectsForStudy"></a>
<h1>getSubjectsForStudy</h1>
<h3>url: <g:createLink controller="api" action="getSubjectsForStudy" absolute="true" /></h3>
<p>
    Returns the subjects for a particular study

    <h2>Request parameters</h2>
    <table>
        <thead>
            <th>argument</th>
            <th>type</th>
            <th>length</th>
            <th>description</th>
            <th>example</th>
            <th>required</th>
        </thead>
        <tr>
            <td>deviceID</td>
            <td>string</td>
            <td>36 (max)</td>
            <td>a unique ID of the client device / application performing the call</td>
            <td>9ae87836-d38d-4b86-be6a-eff93f2b049a</td>
            <td>yes</td>
        </tr>
        <tr>
            <td>validation</td>
            <td>string</td>
            <td>-</td>
            <td><a href="http://www.miraclesalad.com/webtools/md5.php" target="_new">md5sum</a>( token + sequence + api key )</td>
            <td>9ae87836d38d4b86be6aeff93f2b049a</td>
            <td>yes</td>
        </tr>
        <tr>
            <td>studyToken</td>
            <td>string</td>
            <td>255</td>
            <td>study token (see getStudies)</td>
            <td>b6e0c6f4-d8db-4a43-91fa-a157d2d492f0</td>
            <td>yes</td>
        </tr>
    </table>

    <h2>example reply</h2>
    <blockquote>
        {"count":11,"subjects":[{"id":81,"name":"1","species":"Homo sapiens","Gender":"Female","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":82,"name":"2","species":"Homo sapiens","Gender":"Male","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":83,"name":"3","species":"Homo sapiens","Gender":"Female","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":84,"name":"4","species":"Homo sapiens","Gender":"Male","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":85,"name":"5","species":"Homo sapiens","Gender":"Female","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":86,"name":"6","species":"Homo sapiens","Gender":"Male","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":87,"name":"7","species":"Homo sapiens","Gender":"Male","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":88,"name":"8","species":"Homo sapiens","Gender":"Male","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":89,"name":"9","species":"Homo sapiens","Gender":"Male","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":90,"name":"10","species":"Homo sapiens","Gender":"Male","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null},{"id":91,"name":"11","species":"Homo sapiens","Gender":"Female","Age":null,"DOB":null,"Height":null,"Weight":null,"BMI":null,"Race":null,"Waist circumference":null,"Hip circumference":null,"Systolic blood pressure":null,"Diastolic blood pressure":null,"Heart rate":null,"Run-in-food":null}]}
    </blockquote>
</p>

<a name="getAssaysForStudy"></a>
<h1>getAssaysForStudy</h1>
<h3>url: <g:createLink controller="api" action="getAssaysForStudy" absolute="true" /></h3>
<p>
    Returns the assays for a particular study

    <h2>Request parameters</h2>
    <table>
        <thead>
        <th>argument</th>
        <th>type</th>
        <th>length</th>
        <th>description</th>
        <th>example</th>
        <th>required</th>
        </thead>
        <tr>
            <td>deviceID</td>
            <td>string</td>
            <td>36 (max)</td>
            <td>a unique ID of the client device / application performing the call</td>
            <td>9ae87836-d38d-4b86-be6a-eff93f2b049a</td>
            <td>yes</td>
        </tr>
        <tr>
            <td>validation</td>
            <td>string</td>
            <td>-</td>
            <td><a href="http://www.miraclesalad.com/webtools/md5.php" target="_new">md5sum</a>( token + sequence + api key )</td>
            <td>9ae87836d38d4b86be6aeff93f2b049a</td>
            <td>yes</td>
        </tr>
        <tr>
            <td>studyToken</td>
            <td>string</td>
            <td>255</td>
            <td>study token (see getStudies)</td>
            <td>b6e0c6f4-d8db-4a43-91fa-a157d2d492f0</td>
            <td>yes</td>
        </tr>
    </table>

    <h2>example reply</h2>
    <blockquote>
        {"count":6,"assays":[{"token":"253ec24f-9bac-4f2b-b9cf-f84b86376a4e","name":"16S Sequencing assay","module":"Mass Sequencing module","Description":null},{"token":"4df2f49d-1d8c-48bd-8ebd-d267164948ec","name":"18S Sequencing assay","module":"Mass Sequencing module","Description":null},{"token":"828cf2d6-d797-484b-82f9-df9933d76d77","name":"Glucose assay after","module":"SAM module for clinical data","Description":null},{"token":"d68e8fed-41ca-4408-9d8e-f3598eca9183","name":"Glucose assay before","module":"SAM module for clinical data","Description":null},{"token":"32945764-6c5e-497c-8b1e-0d5e0dfa8221","name":"Lipidomics profile after","module":"Metabolomics module","Description":null,"Spectrometry technique":"GC/MS"},{"token":"92f42f77-1c13-4b25-aa57-b444e355fbf4","name":"Lipidomics profile before","module":"Metabolomics module","Description":null,"Spectrometry technique":"GC/MS"}]}
    </blockquote>
</p>

<a name="getSamplesForAssay"></a>
<h1>getSamplesForAssay</h1>
<h3>url: <g:createLink controller="api" action="getSamplesForAssay" absolute="true" /></h3>
<p>
    Returns the samples data for a particular assay

<h2>Request parameters</h2>
<table>
    <thead>
    <th>argument</th>
    <th>type</th>
    <th>length</th>
    <th>description</th>
    <th>example</th>
    <th>required</th>
    </thead>
    <tr>
        <td>deviceID</td>
        <td>string</td>
        <td>36 (max)</td>
        <td>a unique ID of the client device / application performing the call</td>
        <td>9ae87836-d38d-4b86-be6a-eff93f2b049a</td>
        <td>yes</td>
    </tr>
    <tr>
        <td>validation</td>
        <td>string</td>
        <td>-</td>
        <td><a href="http://www.miraclesalad.com/webtools/md5.php" target="_new">md5sum</a>( token + sequence + api key )</td>
        <td>9ae87836d38d4b86be6aeff93f2b049a</td>
        <td>yes</td>
    </tr>
    <tr>
        <td>assayToken</td>
        <td>string</td>
        <td>255</td>
        <td>assay token (see getAssays)</td>
        <td>b6e0c6f4-d8db-4a43-91fa-a157d2d492f0</td>
        <td>yes</td>
    </tr>
</table>

<h2>example reply</h2>
<blockquote>
    {
    "count":
    11,
    "samples":
    [
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T58.66620961739546",
    "material":
    "blood plasma",
    "name":
    "7_A",
    "token":
    "c705668a-81c4-4d80-83df-96bb477aeb0b"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T39.7483280873287",
    "material":
    "blood plasma",
    "name":
    "9_A",
    "token":
    "d81bdda8-4684-45b9-b254-1ec4756cfc71"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T43.20628871191769",
    "material":
    "blood plasma",
    "name":
    "2_A",
    "token":
    "2f501b55-ffdd-4bf2-a598-dcf24d3fac63"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T88.40760089710538",
    "material":
    "blood plasma",
    "name":
    "8_A",
    "token":
    "f908ae2a-3df7-4eb7-be2a-0b8859c20bfc"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T58.14619508995611",
    "material":
    "blood plasma",
    "name":
    "11_A",
    "token":
    "6763cff4-8113-4614-85b9-ef98fb34beba"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T71.86067212685215",
    "material":
    "blood plasma",
    "name":
    "6_A",
    "token":
    "5a339aaa-9bb6-4a0a-9ce7-4c42ceaf5771"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T2.395117860298579",
    "material":
    "blood plasma",
    "name":
    "3_A",
    "token":
    "a9e73abe-aed3-4c43-8fe7-a6b3dfe6e2ed"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T98.99437236833568",
    "material":
    "blood plasma",
    "name":
    "10_A",
    "token":
    "3e63a493-c69d-4cd4-ba23-eeafe962b17f"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T25.420102086098005",
    "material":
    "blood plasma",
    "name":
    "4_A",
    "token":
    "34d5611b-7407-489a-b25a-00ad2b0d8789"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T69.55369597806298",
    "material":
    "blood plasma",
    "name":
    "1_A",
    "token":
    "5c9dce07-ca4d-4bcb-8ac3-c8488bd7247a"
    },
    {
    "Remarks":
    null,
    "Sample measured volume":
    null,
    "Text on vial":
    "T50.41146383561054",
    "material":
    "blood plasma",
    "name":
    "5_A",
    "token":
    "21a07d33-6d95-46f9-a80d-cd58d7e140d0"
    }
    ]
    }
</blockquote>
</p>

<a name="getMeasurementDataForAssay"></a>
<h1>getMeasurementDataForAssay</h1>
<h3>url: <g:createLink controller="api" action="getMeasurementDataForAssay" absolute="true" /></h3>
<p>
    Returns the measurement data for a particular assay

<h2>Request parameters</h2>
<table>
    <thead>
    <th>argument</th>
    <th>type</th>
    <th>length</th>
    <th>description</th>
    <th>example</th>
    <th>required</th>
    </thead>
    <tr>
        <td>deviceID</td>
        <td>string</td>
        <td>36 (max)</td>
        <td>a unique ID of the client device / application performing the call</td>
        <td>9ae87836-d38d-4b86-be6a-eff93f2b049a</td>
        <td>yes</td>
    </tr>
    <tr>
        <td>validation</td>
        <td>string</td>
        <td>-</td>
        <td><a href="http://www.miraclesalad.com/webtools/md5.php" target="_new">md5sum</a>( token + sequence + api key )</td>
        <td>9ae87836d38d4b86be6aeff93f2b049a</td>
        <td>yes</td>
    </tr>
    <tr>
        <td>assayToken</td>
        <td>string</td>
        <td>255</td>
        <td>assay token (see getAssays)</td>
        <td>b6e0c6f4-d8db-4a43-91fa-a157d2d492f0</td>
        <td>yes</td>
    </tr>
</table>

<h2>example reply</h2>
<blockquote>
    {
    "measurements":
    {
    "07378e29-3233-4e3f-b4ea-007f9f9b1317":
    {
    "Fat Depot":
    310
    },
    "198183b1-d402-4f24-9c5a-396f79bb6a55":
    {
    "Fat Depot":
    1918
    },
    "2c719340-eb7f-4a70-8527-c64cc74dc542":
    {
    "Fat Depot":
    1039
    },
    "4861fc77-1320-4401-b18a-66b1cd67d2c8":
    {
    "Fat Depot":
    411
    },
    "4edff5ad-c3af-41da-8efe-87f5a018912a":
    {
    "Fat Depot":
    368
    },
    "6bfe2a0d-3af0-4ed8-8144-56840e934f6e":
    {
    "Fat Depot":
    456
    },
    "775666dd-05b1-4f35-ac9c-d36f8257eb1a":
    {
    "Fat Depot":
    2075
    },
    "7e9930bc-ec8c-4e74-bd2b-49d6c852eeda":
    {
    "Fat Depot":
    945
    },
    "a4d16db8-49f2-4dc2-81bb-910e574c804a":
    {
    "Fat Depot":
    311
    },
    "a556b145-dd37-4568-92f1-e3a251653276":
    {
    "Fat Depot":
    1150
    },
    "c180d727-8d6c-4673-a5fd-1c28f04b8aaa":
    {
    "Fat Depot":
    347
    },
    "e888b1b1-1dc8-45ed-8196-00338fa3b36f":
    {
    "Fat Depot":
    1387
    },
    "f7392df1-a754-4957-a1d0-c98bbb91080d":
    {
    "Fat Depot":
    318
    },
    "fc755220-b2b4-43ed-adf8-e4323fc62b55":
    {
    "Fat Depot":
    1308
    }
    }
    }
</blockquote>
</p>

</div>
</body>
</html>
