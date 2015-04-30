<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="sammain"/>
        <title>${module} Home</title>
        <script>
            $(function() {
                $( "#faq" ).accordion({ autoHeight: false, collapsible: true, active: false, header: 'h3' });
            });
        </script>
    </head>

    <body>
        <div id="data" style="position: relative;">
            <div style="width: 65%; ">
                <h1><b>Introduction ${module}</b></h1>
                <p><b>Here you can find the measurements for ${module}.</b></p>
                <h1>How do I start using this ${module} module?</h1>
                <div>
                    <p>
                    <ul style="list-style-type: decimal;">
                        <li>Browse platforms te see whether the platform of your choice already exists.</li>
                        <li>You can add features using the import function.</li>
                        <li>Then add measurements using the measurement importer.</li>
                    </ul>
                </p>
                </div>

                <h1>What is a ${module} feature?</h1>
                <div>
                    <p>A feature is a substance (or measurementtype / detector etc.) that is measured when a sample is taken.<br><i> For example, glucose, leptin, CD40, bodyweight, urea</i></p>
                </div>

                <h1>What is a ${module} platform?</h1>
                <div>
                    <p>A platform is a technique of a certain type of data.<br><i> For example, for Transcriptomics you will have platforms such as Affymetrix & Illumina.</i></p>
                </div>

                <h1>What is a measurement?</h1>
                <div>
                    <p>A measurement is a single value that belongs to a sample.<br><i> For example, glucose level is measured for a group of samples.</i></p>
                </div>

                <h1>What is an assay?</h1>
                <div>
                    <p>An assay is a group of samples that are analyzed.<br><i> For example to obtain, clinical chemistry -, metabolomics -, or transcriptomics data</i></p>
                </div>
            </div>
            <div id="faq" style="position: absolute; right: 0px; top: 0px; width: 30%;">
                <h1>Frequently Asked Questions</h1>
                <g:render template="faq" />
            </div>
        </div>
    </body>
</html>
