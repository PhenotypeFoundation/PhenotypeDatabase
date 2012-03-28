<html>
<head>
    <meta name="layout" content="main"/>
</head>
<body>
<h1>API specification</h1>

The API allows third party software to interface with GSCF and connected modules.

<h2>prerequisites</h2>
    <li>a valid username / password</li>
    <li>the username should be given the role ROLE_CLIENT</li>
    <li>a shared secret</li>
    <li>a deviceID / clientID (look <a href="https://github.com/4np/UIDevice-with-UniqueIdentifier-for-iOS-5" target="_new">here</a> for iOS)</li>

<h1>authenticate</h1>
<p>
    Authenticate a client using <a href="http://en.wikipedia.org/wiki/Basic_access_authentication" target="_new">HTTP BASIC authentication</a>.
    After successful authentication, a session token is returned which should be used in all subsequent calls to authorize the API calls.
    This call should also be performed whenever a client/server sessions becomes out of sync (e.g. the client's sequence count
    differs from the server's sequence count) as the server's sequence count will be returned. For security reasons this api method is
    designed to be called only once (or when sessions are out of sync) as HTTP BASIC authentication is not really secure (if someone
    is able to sniff your traffic, the authentication md5 hash is easily stolen).<br/>
    Every subsequent request the client does, needs to contain a validation MD5 hash, which is a MD5 sum of the concatenation of the device token,
    the request sequence and a shared secret (e.g. <i>md5sum( token + sequence + shared secret )</i>).
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
            <td>a unique ID of the client device / application performing the call</td>
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

<h1>getStudies</h1>
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

<h1>getSubjectsForStudy</h1>
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
</body>
</html>
