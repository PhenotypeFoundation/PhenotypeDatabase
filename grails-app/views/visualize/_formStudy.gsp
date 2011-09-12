<label for="study">Study</label>
<p class="info">Select a study from the list below.</p>
<p>
    <g:select from="${studies}" size="6" optionKey="id" optionValue="title" name="study" onChange="changeStudy();"/>
</p>