<af:pageContent>
        <g:form url="[action:'results',controller:'query',params: 'q']">
          <input type="hidden" name="targetUri" value="${targetUri}" />
          <label class="grey" for="q">Search for:</label>
          <input class="field" type="text" name="q" id="q" size="40" />
          <input type="submit" name="submit" value="Query"/>
        </g:form>
</af:pageContent>