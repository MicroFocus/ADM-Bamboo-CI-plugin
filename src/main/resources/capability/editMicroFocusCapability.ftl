[@ww.checkbox labelKey='MFCapability.detectionLbl' name='uftDetection' toggle='true' description=capabilityType.getDetectionDescription() /]

[@ui.bambooSection dependsOn='uftDetection' showOn='true']
    [@ww.textfield labelKey="MFCapability.pathLbl" name="uftPath" description=capabilityType.getExecutableDescription() required='true' /]
[/@ui.bambooSection]