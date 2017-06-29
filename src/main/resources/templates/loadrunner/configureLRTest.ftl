[@ui.bambooSection title="LoadRunner Task"]
    [@ww.textarea labelKey="lr.param.label.tests" name="tests" required='true'/]
    [@ww.textfield labelKey="lr.param.label.timeout" name="timeout" required='false'/]
    [@ww.checkbox labelKey="lr.param.label.lrSettings" name="lrSettings" toggle="true" /]
    [@ui.bambooSection title="LoadRunner Settings" dependsOn="lrSettings" showOn="true" ]
        [@ww.textfield labelKey="lr.param.label.pollingInterval" name="pollingInterval" required="false"/]
        [@ww.textfield labelKey="lr.param.label.execTimeout" name="execTimeout" required="false"/]
        [@ww.textarea labelKey="lr.param.label.ignoreErrors" name="ignoreErrors" required="false"/]
    [/@ui.bambooSection]
[/@ui.bambooSection]

<script type="text/javascript">
    var customWidth = "500px";
    document.getElementById("tests").style.maxWidth = customWidth;
    document.getElementById("timeout").style.maxWidth = customWidth;
    document.getElementById("pollingInterval").style.maxWidth = customWidth;
    document.getElementById("execTimeout").style.maxWidth = customWidth;
    document.getElementById("ignoreErrors").style.maxWidth = customWidth;
</script>
