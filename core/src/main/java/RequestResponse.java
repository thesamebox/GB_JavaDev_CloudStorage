public class RequestResponse extends Requests {
    private String serverMessage;

    public RequestResponse(String serverMessage) {
        this.serverMessage = serverMessage;
    }

    public String getServerMessage() {
        return serverMessage;
    }
}
