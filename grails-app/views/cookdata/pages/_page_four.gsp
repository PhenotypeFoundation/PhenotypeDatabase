<af:page>
    <h1>Results</h1>
    <g:each in="${mapFeaturesPerAssay}" var="item">
       <h2>Assay ${assays[Integer.valueOf(item.key)]}</h2>
       <g:each in="${item.value}" var="feature">
           ${feature} <br>
       </g:each>
    </g:each>
</af:page>