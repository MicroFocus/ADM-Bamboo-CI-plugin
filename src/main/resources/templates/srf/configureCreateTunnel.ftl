<div class="control">
 [@ww.textfield labelKey="SRF Tunnel Client Path" name="SRF Tunnel Client Path" required='true'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('clientPathToolTip');">?</div>
<div id ="clientPathToolTip" class="toolTip">
 [@ww.text name='Path to a SRF tunnel client executable.'/]
</div>

<div class="control">
  [@ww.textfield labelKey="SRF Tunnel Config File" name="SRF Tunnel Config File" required='true'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('configFileToolTip');">?</div>
<div id ="configFileToolTip" class="toolTip">
  [@ww.text name='Path to the SRF tunneling config.json file.'/]
</div>

<script  type="text/javascript">
    var customWidth = "500px";
    document.getElementById('SRF Tunnel Client Path').style.maxWidth=customWidth;
    document.getElementById('SRF Tunnel Config File').style.maxWidth=customWidth;

    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if(e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
    }
</script>

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
    .control,.helpIcon, .toolTip {
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
    .control{
        width:500px;
        clear: both;
    }
</style>