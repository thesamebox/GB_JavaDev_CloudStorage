public class CurrentLogin {
    private static String currentLogin;

    public static void setCurrentLogin(String login){
        currentLogin = login;
    }
    public static String getCurrentLogin(){
        return currentLogin;
    }
}

