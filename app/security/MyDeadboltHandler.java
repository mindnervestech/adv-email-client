package security;

import java.security.Key;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import models.User;
import models.UserPermission;
import models.UserRole;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;
import sun.misc.BASE64Decoder;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import views.html.*;

public class MyDeadboltHandler extends AbstractDeadboltHandler {

	public Result beforeAuthCheck(Http.Context context) {
        // returning null means that everything is OK.  Return a real result if you want a redirect to a login page or
        // somewhere else
        return null;
    }

    public Subject getSubject(Http.Context context) {
    	if(context.args.get("currentUser")!=null) {
    		return (Subject)context.args.get("currentUser");
    	}
    	String key = context.session().get("key");
        String username = null;
    	String password = null;
    	try {
            Map m = new HashMap();
            Key dkey = generateKey();
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, dkey);
            byte[] decordedValue = new BASE64Decoder().decodeBuffer(key);
            byte[] decValue = c.doFinal(decordedValue);
            String decryptedValue = new String(decValue);
            String[] st = decryptedValue.split("-");
            username = st[0];
            password = st[1];
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	Connection conn = null;
    	try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = null;
			conn = DriverManager.getConnection("jdbc:mysql://enter-agora.com:3307/agora_scrape","agora_jagbir", "gane3Ecehema6a");
			Statement smt = conn.createStatement();
			ResultSet userRS =  smt.executeQuery("select username,comp_id,user_id from users where username='"+username+"' and password='"+password+"'");
			List<UserRole>  userRole = new ArrayList<UserRole>();
			List<UserPermission> userPermission = new ArrayList<UserPermission>();
			Set<String> roleSet = new HashSet<String>();
			if(userRS.next()) {
				String user = userRS.getString("username");
				String subsc = userRS.getString("comp_id");
				Long id = userRS.getLong("user_id");
				User u = new User(user, "");
				u.setId(id);
				ResultSet roleRS = smt.executeQuery("select r.role_id as rid,r.name as rname,ac.action_id as aid,ac.action_name as aname,ac.action_url as aurl "
				+"from roles r,subscriber_role sr,authorities au,role_action ra,actionable ac where "
				+"(au.user_id="+id+" and au.role_id = r.role_id and r.role_id=ra.role_id and ra.action_id=ac.action_id) "+
				"or (sr.comp_id = "+subsc+" and sr.role_id = r.role_id and r.role_id=ra.role_id and ra.action_id=ac.action_id) group by ac.action_id");
				while(roleRS.next()) {
					if(roleSet.add(roleRS.getString("rname"))) {
						UserRole role = new UserRole();
						role.setId(roleRS.getLong("rid"));
						role.setName(roleRS.getString("rname"));
						userRole.add(role);
					}
					UserPermission permission = new UserPermission();
					permission.setId(roleRS.getLong("aid"));
					permission.setName(roleRS.getString("aname"));
					permission.setUrl(roleRS.getString("aurl"));
					userPermission.add(permission);
				}
				u.setUserPermissions(userPermission);
				u.setUserRoles(userRole);
				System.out.println("userRoles : "+userRole.size());
				System.out.println("userPermission : "+userPermission.size());
				context.args.put("currentUser", u);
				return u;
			} else {
				return null;
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
    	finally {
    		if(conn!=null) {
    			try {
					conn.close();
				} catch (SQLException e) {
				}
    		}
    	}
    	return null;//new User("abtutake@gmail.com","password");
    }

    public DynamicResourceHandler getDynamicResourceHandler(Http.Context context) {
        return null;//new MyDynamicResourceHandler();
    }

    @Override
    public Result onAuthFailure(Http.Context context,
                                                 String content) {
        // you can return any result from here - forbidden, etc
        return ok(accessFailed.render());
    }
    private static Key generateKey() throws Exception {
		Key key = new SecretKeySpec("The1234Secret987".getBytes(), "AES");
		return key;
	}
	

}
