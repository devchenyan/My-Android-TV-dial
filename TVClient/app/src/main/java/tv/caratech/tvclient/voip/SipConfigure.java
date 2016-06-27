package tv.caratech.tvclient.voip;

/**
 * Created by wurenhai on 2016/6/15.
 */
public class SipConfigure {

    public final static int INVALID_PORT = 0;

    public String user;
    public String pass;
    public String domain;
    public int port = INVALID_PORT;

    public boolean validate() {
        if (user == null || "".equals(user)) {
            return false;
        }
        if (pass == null || "".equals(pass)) {
            return false;
        }
        if (domain == null || "".equals(domain)) {
            return false;
        }
        if (port == INVALID_PORT) {
            return false;
        }
        return true;
    }

}
