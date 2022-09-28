import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


public class hwScraper {
	
	public static void main(String[] args) throws IOException {
		
		 
        // reading all lines of file as List of strings
        byte[]  bytes = Files.readAllBytes(Paths.get("feat.xml"));
         
        // converting List<String> to palin string using java 8 api.
        String feat = new String(bytes, StandardCharsets.UTF_8);
         
        // printing the final string.
        
        //ArrayList<String> ids = new ArrayList<String>();
        String sql = "(";
        while(true) {
        	int idIndex = feat.indexOf("id=\"");
        	
        	if(idIndex == -1) {
        		break;
        	}
        	
        	feat = feat.substring(idIndex + 4);
        	sql = sql + "\"" + feat.substring(0,feat.indexOf("\"")) + "\",";
        	
        	feat = feat.substring(feat.indexOf("\""));
        }
        System.out.println(sql);
		
		
	}
}
