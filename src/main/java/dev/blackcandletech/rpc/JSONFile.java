package dev.blackcandletech.rpc;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class JSONFile {

    private final boolean copyFromResources;
    private final File file;
    private final JSONObject jsonObject;

    public JSONFile(String fileName, boolean copyFromResources) throws ParseException {
        file = new File(fileName);
        this.copyFromResources = copyFromResources;
        if(!file.exists()) createFile();
        String jsonContent = getFileAsString();
        jsonObject = (JSONObject) new JSONParser().parse(jsonContent);
    }

    private void createFile () {
        // Create a blank config file and create an output stream
        try {
            file.createNewFile();
            if(copyFromResources) {
                FileOutputStream fileStream = new FileOutputStream(file);
                // If the resource can be found in the resources, then write the template data to blank config file
                InputStream resource = this.getClass().getClassLoader().getResourceAsStream(file.getName());
                if(resource != null) resource.transferTo(fileStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileAsString() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String newLine = System.getProperty("line.separator");

            String line;
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(newLine);
            }

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private JSONObject getJSON() {
        return jsonObject;
    }

    public Object getValue(String key) {
        return jsonObject.get(key);
    }

    public String getString(String key) {
        return (String) getValue(key);
    }

    public Double getDouble(String key) {
        return (Double) getValue(key);
    }

    public Float getFloat(String key) {
        return (Float) getValue(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) getValue(key);
    }

}
