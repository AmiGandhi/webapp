package neu.csye6225.spring2020.cloud.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@Scope(value = "singleton")
public class CommonUtil {

    public String getFileNameFromPath(String path) {
        String[] pathArr = path.split("/");
        return pathArr[pathArr.length - 1];
    }

    public String computeMD5Hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        byte[] digest = messageDigest.digest(data);

        StringBuffer sb = new StringBuffer();
        for (byte b : digest) {
            sb.append(Integer.toHexString((int) (b & 0xff)));
        }
        return sb.toString();
    }

    public String stackTraceString(Exception e) {
        StringBuffer sb = new StringBuffer();

        // Main stacktrace
        StackTraceElement[] stack = e.getStackTrace();
        stackTraceStringBuffer(sb, e.toString(), stack, 0);

        // The cause(s)
        Throwable cause = e.getCause();
        while (cause != null) {
            // Cause start first line
            sb.append("Caused by: ");

            // Cause stacktrace
            StackTraceElement[] parentStack = stack;
            stack = cause.getStackTrace();
            if (parentStack == null || parentStack.length == 0)
                stackTraceStringBuffer(sb, cause.toString(), stack, 0);
            else {
                int equal = 0; // Count how many of the last stack frames are equal
                int frame = stack.length - 1;
                int parentFrame = parentStack.length - 1;
                while (frame > 0 && parentFrame > 0) {
                    if (stack[frame].equals(parentStack[parentFrame])) {
                        equal++;
                        frame--;
                        parentFrame--;
                    } else
                        break;
                }
                stackTraceStringBuffer(sb, cause.toString(), stack, equal);
            }
            cause = cause.getCause();
        }

        return sb.toString();

    }

        public static void stackTraceStringBuffer (StringBuffer sb, String name, StackTraceElement[]stack,int equal){
            String nl = System.getProperty("line.separator");
            // (finish) first line
            sb.append(name);
            sb.append(nl);

            // The stacktrace
            if (stack == null || stack.length == 0) {
                sb.append("   <<No stacktrace available>>");
                sb.append(nl);
            } else {
                for (int i = 0; i < stack.length - equal; i++) {
                    sb.append("   at ");
                    sb.append(stack[i] == null ? "<<Unknown>>" : stack[i].toString());
                    sb.append(nl);
                }
                if (equal > 0) {
                    sb.append("   ...");
                    sb.append(equal);
                    sb.append(" more");
                    sb.append(nl);
                }
            }
        }
    }
