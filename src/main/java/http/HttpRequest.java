package http;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    Map<String, String> requestMap = new HashMap<>();

    public HttpRequest(InputStream in){
        try {
            BufferedReader br = null;
            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();

            if(line == null) return;
            log.debug("request line : {}", line);
            String[] tokens = line.split(" ");

            requestMap.put("method", tokens[0]);
            String params = "";
            String path = tokens[1];
            if(tokens[1].contains("?")){
                int index = path.indexOf("?");
                path = path.substring(0, index);
                params = tokens[1].substring(index+1);
            }
            requestMap.put("path", path);

            int contentLength = 0;
            while(!"".equals(line = br.readLine()) ){
                log.debug("request line : {}", line);
                tokens = line.split(" ");
                requestMap.put(tokens[0].replace(":", ""), tokens[1]);

                if(line.startsWith("Content-Length:")){
                    contentLength = Integer.parseInt(tokens[1]);
                }
            }

            if("GET".equals(getMethod())){
                Map<String, String> param = HttpRequestUtils.parseQueryString(params);
                Iterator<String> keys = param.keySet().iterator();
                while (keys.hasNext()){
                    String key = keys.next();
                    requestMap.put(key, param.get(key));
                }
            }else if("POST".equals(getMethod())){
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> param = HttpRequestUtils.parseQueryString(body);
                Iterator<String> keys = param.keySet().iterator();
                while (keys.hasNext()){
                    String key = keys.next();
                    requestMap.put(key, param.get(key));
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    public String getMethod(){
        return requestMap.get("method");
    }

    public String getPath(){
        return requestMap.get("path");
    }

    public String getHeader(String headerName){
        return requestMap.get(headerName);
    }

    public String getParameter(String parameterName){
        return requestMap.get(parameterName);
    }
}
