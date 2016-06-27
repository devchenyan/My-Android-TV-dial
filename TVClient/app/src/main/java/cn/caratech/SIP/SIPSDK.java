package cn.caratech.SIP;

public class SIPSDK
{
    static {
        System.loadLibrary("SIPCore");
    }

    public native int init();
    public native int done();

    public native int setSIPEventHandler(SIPEvent handler);

    public native int setUser(String username, String password, String domain, int port);
    public native int register();
    public native int unregister();

    public native int call(String callee, String sdp);
    public native int answer(int callId, String sdp);
    public native int reject(int callId);
    public native int hangup(int callId);
}