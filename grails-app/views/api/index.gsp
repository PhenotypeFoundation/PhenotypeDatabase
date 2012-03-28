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
    <li>a shared secret (used to calculate the validation md5 hash)</li>
    <li>a deviceID / clientID (look <a href="https://github.com/4np/UIDevice-with-UniqueIdentifier-for-iOS-5" target="_new">here</a> for iOS)</li>

<h2>available API calls</h2>
    <li><a href="#authenticate">authenticate</a> - set up / synchronize client-server session</li>
    <li><a href="#getStudies">getStudies</a> - fetch all (readable) studies</li>
    <li><a href="#getSubjectsForStudy">getSubjectsForStudy</a> - fetch all subjects in a given study</li>
    <li><a href="#getAssaysForStudy">getAssaysForStudy</a> - fetch all assays in a given study</li>

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
    the request sequence and a shared secret (e.g. <i>md5sum( token + sequence + shared secret )</i> ).<br/>
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
            <td><a href="http://www.miraclesalad.com/webtools/md5.php" target="_new">md5sum</a>( token + sequence + shared secret )</td>
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
            <td><a href="http://www.miraclesalad.com/webtools/md5.php" target="_new">md5sum</a>( token + sequence + shared secret )</td>
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
            <td><a href="http://www.miraclesalad.com/webtools/md5.php" target="_new">md5sum</a>( token + sequence + shared secret )</td>
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
</div>
</body>
</html>
