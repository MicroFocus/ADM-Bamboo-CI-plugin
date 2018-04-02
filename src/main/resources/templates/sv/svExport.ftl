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

[@ui.bambooSection title="Export Virtual Service Task"]

    <div class="control">
        [@ww.textfield labelKey="sv.param.label.url" name="url" required='true'/]
    </div>
    <div class="helpIcon" onclick="javascript: toggle_visibility('sv.param.tooltips.url');">?</div>
    <div id ="sv.param.tooltips.url" class="toolTip">
            [@ww.text name='sv.param.tooltips.url'/]
    </div>
    <hr>

    <div class="control">
        [@ww.textfield labelKey="sv.param.label.userName" name="userName" required='false'/]
    </div>
    <hr>

    <div class="control">
        [@ww.password labelKey="sv.param.label.password" name="password" showPassword="false"/]
    </div>
    <hr>

    <div>
        [@ww.radio labelKey='sv.param.label.serviceSelection' name='serviceSelection'
        listKey='key' listValue='value' toggle='true'
        list=SVServiceSelectionMap ]
        [/@ww.radio]

        [@ui.bambooSection dependsOn='serviceSelection' showOn='SERVICE']
            [@ww.textfield labelKey="sv.param.label.serviceNameOrId" name="serviceNameOrId" required='true'/]
        [/@ui.bambooSection]
        [@ui.bambooSection dependsOn='serviceSelection' showOn='PROJECT']
            [@ww.textfield labelKey="sv.param.label.projectPath" name="projectPath" required='true'/]
            [@ww.password labelKey="sv.param.label.projectPassword" name="projectPassword" showPassword="false"/]
        [/@ui.bambooSection]
    </div>
    <hr>

    <div class="control">
        [@ww.textfield labelKey="sv.param.label.targetDirectory" name="targetDirectory" required='true'/]
    </div>
    <div class="helpIcon" onclick="javascript: toggle_visibility('sv.param.tooltips.targetDirectory');">?</div>
    <div id ="sv.param.tooltips.targetDirectory" class="toolTip">
        [@ww.text name='sv.param.tooltips.targetDirectory'/]
    </div>
    <hr>

    <div class="control">
        [@ww.checkbox labelKey="sv.param.label.cleanTargetDirectory" name="cleanTargetDirectory" toggle="true" /]
    </div>
    <div class="helpIcon" onclick="javascript: toggle_visibility('sv.param.tooltips.cleanTargetDirectory');">?</div>
    <div id ="sv.param.tooltips.cleanTargetDirectory" class="toolTip">
        [@ww.text name="sv.param.tooltips.cleanTargetDirectory"/]
    </div>
    <hr>

    <div class="control">
        [@ww.checkbox labelKey="sv.param.label.switchServiceToStandBy" name="switchServiceToStandBy" toggle="true" /]
    </div>
    <div class="helpIcon" onclick="javascript: toggle_visibility('sv.param.tooltips.switchServiceToStandBy');">?</div>
    <div id ="sv.param.tooltips.switchServiceToStandBy" class="toolTip">
        [@ww.text name="sv.param.tooltips.switchServiceToStandBy"/]
    </div>
    <hr>

    <div class="control">
        [@ww.checkbox labelKey="sv.param.label.force" name="force" toggle="true" /]
    </div>
    <div class="helpIcon" onclick="javascript: toggle_visibility('sv.param.tooltips.force');">?</div>
    <div id ="sv.param.tooltips.force" class="toolTip">
        [@ww.text name='sv.param.tooltips.force'/]
    </div>
    <hr>

    <div class="control">
        [@ww.checkbox labelKey="sv.param.label.archive" name="archive" toggle="true" /]
    </div>
    <div class="helpIcon" onclick="javascript: toggle_visibility('sv.param.tooltips.archive');">?</div>
    <div id ="sv.param.tooltips.archive" class="toolTip">
        [@ww.text name='sv.param.tooltips.archive'/]
    </div>
[/@ui.bambooSection]

<script type="text/javascript">
    var customWidth = "500px";
    document.getElementById("url").style.maxWidth = customWidth;
    document.getElementById("userName").style.maxWidth = customWidth;
    document.getElementById("password").style.maxWidth = customWidth;
    document.getElementById("serviceNameOrId").style.maxWidth = customWidth;
    document.getElementById("projectPath").style.maxWidth = customWidth;
    document.getElementById("projectPassword").style.maxWidth = customWidth;
    document.getElementById("targetDirectory").style.maxWidth = customWidth;

    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if (e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
    }
</script>
