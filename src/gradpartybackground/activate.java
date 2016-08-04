/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gradpartybackground;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;

/**
 *
 * @author Saleh
 */
public class activate {

    public interface User32 extends W32APIOptions {

        User32 instance = (User32) Native.loadLibrary("user32", User32.class,
                DEFAULT_OPTIONS);


        boolean ShowWindow(HWND hWnd, int nCmdShow);

        boolean SetForegroundWindow(HWND hWnd);

        HWND FindWindow(String winClass, String title);

        int SW_SHOW = 1;

    }

    public static void setFocus(String name) {  
        User32 user32 = User32.instance;  
        HWND hWnd = user32.FindWindow(null, "Downloads"); // Sets focus to my opened 'Downloads' folder
        user32.ShowWindow(hWnd, User32.SW_SHOW);  
        user32.SetForegroundWindow(hWnd);  
    } 
}