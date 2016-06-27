package cn.caratech.SIP;

public interface SIPEvent
{
    public void onRegisterSuccess();
    public void onRegisterFailure(final int code, final String reason);

    public void onInviteIncoming(final int callId, final String caller, final String sdp);
    public void onInviteTrying(final int callId);
    public void onInviteRinging(final int callId);
    public void onInviteClosed(final int callId);
    public void onInviteAnswered(final int callId, final String sdp);
    public void onInviteConnected(final int callId);
    public void onInviteFailure(final int callId, final int code, final String reason);

}