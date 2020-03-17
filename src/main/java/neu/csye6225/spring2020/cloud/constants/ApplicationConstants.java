package neu.csye6225.spring2020.cloud.constants;

public class ApplicationConstants {
    // Endpoints for this service
    // =========================================================================================================================
    public static final String LOGIN = "/v1/user/self";
    public static final String REGISTER = "/v1/user";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BASIC = "Basic";
    public static final String CREATE_BILL = "/v1/bill";
    public static final String GET_ALL_BILLS = "/v3/bills";
//    public static final String GET_ALL_BILLS_v2 = "/v2/bills";
    public static final String GET_BILL = "/v1/bill/{bill_id}";
    public static final String UPDATE_BILL = "/v1/bill/{bill_id}";
    public static final String DELETE_BILL = "/v1/bill/{bill_id}";
    public static final String ATTACH_FILE = "/v1/bill/{bill_id}/file";
    public static final String GET_ATTACHMENT = "/v1/bill/{bill_id}/file/{file_id}";
    public static final String DELETE_ATTACHMENT = "/v1/bill/{bill_id}/file/{file_id}";


    // Username and Password Validation exceptions
    // =========================================================================================================================
    public static final String NULL_EMAIL = "Kindly enter the email id.";
    public static final String INVALID_EMAIL = "Kindly enter valid email id.";
    public static final String EXISTING_EMAIL = "Email Id already exists!";
    public static final String PASSWORD_INCORRECT = "Incorrect password entered. Password must contain an uppercase character, a lowercase character, a digit, a special character with min length of 8";
    public static final String MANDATORY_FIELDS_MISSING = "Please enter all mandatory fields!";
    public static final String INVALID_CREDENTIALS = "Invalid credentials!";
    public static final String EMAILID_PASSWORD_MISSING = "Email Id and password missing!";
    public static final String EMAILID_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    public static final String PASSWORD_REGEX = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*!]).{6,15})";


}
