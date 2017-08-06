
<style type="text/css">
    .helpIcon{
        background-color: rgba(59, 115, 175, 1);
        color: white;
        width: 15px;
        border-radius:15px;
        font-weight: bold;
        padding-left:6px;
        cursor:pointer;
        margin:5px;
    }
    .control,.helpIcon, .toolTip, .MCcheckBox, .parameterWrapper, #paramTable {
        float:left;
    }
    #paramTable{
        width:100%;
    }
    .MCcheckBox{
        width:100%
    }
    .control,.helpIcon, .toolTip{
        float:left;
    }
    .toolTip{
        display: none;
        border: solid #bbb 1px;
        background-color: #f0f0f0;
        padding: 1em;
        margin-bottom: 1em;
        width: 97%;
    }
    hr{
        clear:both;
        border:none;
    }
    .control{
        width:500px;
    }
    form.aui .field-group input.text {
        max-width: 500px;
    }
    h3.title {
        margin: 0px;
    }
    #extraApps {
        min-height: 30px;
        border: 1px solid #ccc;
        border-radius: 1px;
    }
    .extra-app-info {
        padding: 8px 0px;
    }
    .extra-app-info .app-name {
        margin-right: 10px;
    }
</style>

[@ui.bambooSection title="LoadRunner Task"]

    <div class="control">
        [@ww.textarea labelKey="lr.param.label.tests" name="tests" required='true'/]
    </div>
    <div class="helpIcon" onclick="javascript: toggle_visibility('lr.param.tooltips.tests');">?</div>
    <div id ="lr.param.tooltips.tests" class="toolTip">
    [@ww.text name='List of tests to run. Each line should contain a single test.'/]
    </div>
    <hr>

    <div class="control">
        [@ww.textfield labelKey="lr.param.label.timeout" name="timeout" required='false'/]
    </div>
    <div class="helpIcon" onclick="javascript: toggle_visibility('lr.param.tooltips.timeout');">?</div>
    <div id ="lr.param.tooltips.timeout" class="toolTip">
    [@ww.text name='Timeout value in seconds. If left empty, there would be no timeout.'/]
    </div>
    <hr>

    [@ww.checkbox labelKey="lr.param.label.lrSettings" name="lrSettings" toggle="true" /]
    [@ui.bambooSection title="LoadRunner Settings" dependsOn="lrSettings" showOn="true" ]

        <div class="control">
            [@ww.textfield labelKey="lr.param.label.pollingInterval" name="pollingInterval" required="false"/]
        </div>
        <div class="helpIcon" onclick="javascript: toggle_visibility('lr.param.tooltips.pollingInterval');">?</div>
        <div id ="lr.param.tooltips.pollingInterval" class="toolTip">
        [@ww.text name='Polling interval for checking the scenario status, in seconds. The default is 30 seconds.'/]
        </div>
        <hr>
        <div class="control">
            [@ww.textfield labelKey="lr.param.label.execTimeout" name="execTimeout" required="false"/]
        </div>
        <div class="helpIcon" onclick="javascript: toggle_visibility('lr.param.tooltips.execTimeout');">?</div>
        <div id ="lr.param.tooltips.execTimeout" class="toolTip">
        [@ww.text name='The maximum time allotted for scenario execution, in minutes. The default is 10 minutes'/]
        </div>
        <hr>

        <div class="control">
            [@ww.textarea labelKey="lr.param.label.ignoreErrors" name="ignoreErrors" required="false"/]
        </div>
        <div class="helpIcon" onclick="javascript: toggle_visibility('lr.param.tooltips.ignoreErrors');">?</div>
        <div id ="lr.param.tooltips.ignoreErrors" class="toolTip">
        [@ww.text name='Ignore errors during the scenario run containing any of the strings listed below. For example: "Error: CPU usage for this load generator has exceeded 80%"'/]
        </div>
        <hr>
    [/@ui.bambooSection]
[/@ui.bambooSection]

<script type="text/javascript">
    var customWidth = "500px";
    document.getElementById("tests").style.maxWidth = customWidth;
    document.getElementById("timeout").style.maxWidth = customWidth;
    document.getElementById("pollingInterval").style.maxWidth = customWidth;
    document.getElementById("execTimeout").style.maxWidth = customWidth;
    document.getElementById("ignoreErrors").style.maxWidth = customWidth;

    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if (e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
    }
</script>
