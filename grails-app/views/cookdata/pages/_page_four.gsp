<af:page>
    <h1>Please indicate which features you are interested in</h1>
    <g:each in="${mapFeaturesPerAssay}" var="item">
       <h2>Assay ${assays[Integer.valueOf(item.key)]}</h2>
       <g:each in="${item.value}" var="feature">
           ${feature} <br>
       </g:each> 
    </g:each>
</af:page>