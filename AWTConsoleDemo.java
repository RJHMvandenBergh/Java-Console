/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Robert
 */
public class AWTConsoleDemo {
    
    public static void main(String[] arg)
    {
        AWTConsole awtConsole=  new AWTConsole();
        awtConsole.init();
        // Note need to refactor the AWTConsole class
        System.out.println("Sending Hello to the AWTConsole on System.out");
        System.err.println("Simply printing this on the System.err");
        /* some problems with arrival time/order on the AWTConsole*/
    }
    
}
