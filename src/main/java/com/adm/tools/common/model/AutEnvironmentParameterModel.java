package com.adm.tools.common.model;

public class AutEnvironmentParameterModel {

        private final String name;
        private final String value;
        private final AutEnvironmentParameterType paramType;
        private final boolean shouldGetOnlyFirstValueFromJson;

        private String resolvedValue;

        public String getResolvedValue() {
            return resolvedValue;
        }

        public void setResolvedValue(String resolvedValue) {
            this.resolvedValue = resolvedValue;
        }

        public AutEnvironmentParameterModel(String name, String value, AutEnvironmentParameterType paramType, boolean shouldGetOnlyFirstValueFromJson) {
            this.name = name;
            this.value = value;
            this.paramType = paramType;
            this.shouldGetOnlyFirstValueFromJson = shouldGetOnlyFirstValueFromJson;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public AutEnvironmentParameterType getParamType() {
            return paramType;
        }

        public boolean isShouldGetOnlyFirstValueFromJson() {
            return shouldGetOnlyFirstValueFromJson;
        }
}
