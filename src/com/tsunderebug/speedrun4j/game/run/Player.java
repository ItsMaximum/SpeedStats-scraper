package com.tsunderebug.speedrun4j.game.run;

import com.tsunderebug.speedrun4j.user.User;

import java.awt.*;
import java.io.IOException;

public class Player {

	private String rel;
	private String id;
	private String name;
	private String uri;

	public String getName() throws IOException {
		if(name == null) {
			return User.fromID(id).getNames().get("international");
		} else {
			return name;
		}
	}
	
	public String getRel() {
		return rel;
	}

	

	public String getUri() {
		return uri;
	}

	public String getID() throws IOException {
			return id;
		
	}

	public Color getColor() throws IOException {
		if(id != null) {
			return User.fromID(id).getNameStyle().getColor();
		} else {
			return Color.white;
		}
	}
	
	
	@Override
	public boolean equals(Object o) {
		 
        // If the object is compared with itself then return true 
        if (o == this) {
            return true;
        }
 
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Player)) {
            return false;
        }
         
        // typecast o to Complex so that we can compare data members
        Player p = (Player) o;
         
        
        return this.getUri().equals(p.getUri());
    }

}
