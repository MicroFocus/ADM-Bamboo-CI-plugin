package com.adm.tools.common.sdk.handler;

import com.adm.tools.common.SSEException;
import com.adm.tools.common.sdk.Logger;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import net.minidev.json.JSONArray;

public class JsonHandler {
    private Logger logger;

    public JsonHandler(Logger logger) {
        this.logger = logger;
    }

    public Object load(String path) {

        logger.log(String.format("Loading JSON file from: [%s]", path));
        Object parsedJson;
        try {
            InputStream is = new FileInputStream(path);
            String jsonTxt;
            jsonTxt = IOUtils.toString(is, "UTF-8");
            parsedJson =
                    Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST).jsonProvider().parse(
                            jsonTxt);
        } catch (Throwable e) {
            throw new SSEException(String.format("Failed to load JSON from: [%s]", path), e);
        }

        return parsedJson;
    }

    public String getValueFromJsonAsString(
            Object jsonObject,
            String pathToRead,
            boolean shouldGetSingleValueOnly) {

        String value = "";
        try {
            Object extractedObject = JsonPath.read(jsonObject, pathToRead);
            while (extractedObject instanceof JSONArray && shouldGetSingleValueOnly) {
                extractedObject = ((JSONArray) extractedObject).get(0);
            }
            value = extractedObject.toString();
        } catch (Throwable e) {
            logger.log(String.format(
                    "Failed to get the value of [%s] from the JSON file.\n\tError was: %s",
                    pathToRead,
                    e.getMessage()));
        }
        return value;

    }
}
