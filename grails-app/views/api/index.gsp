<html>
<head>
    <meta name="layout" content="main"/>
</head>
<body>
<h1>API specification</h1>
<h1>authenticate</h1>
<p>
    Authenticate a client using <a href="http://en.wikipedia.org/wiki/Basic_access_authentication" target="_new">HTTP BASIC authentication</a>.
    After successful authentication, a session token is returned which should be used in all subsequent calls to authorize the API calls.
    This call should also be performed whenever a client/server sessions becomes out of sync (e.g. the client's sequence count
    differs from the server's sequence count) as the server's sequence count will be returned. For security reasons this api method is
    designed to be called only once (or when sessions are out of sync) as HTTP BASIC authentication is not really secure (if someone
    is able to sniff your traffic, the authentication md5 hash is easily stolen).<br/>
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
</p>

<h1>getAssaysForStudy</h1>
<p>
    bla
</p>
</body>
</html>
