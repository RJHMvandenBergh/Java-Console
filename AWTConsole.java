
// A simple Java Console for your application (Swing version)
// Requires Java 1.1.5 or higher
//
// Disclaimer the use of this source is at your own risk.
// Permision to use and distribute into your own applications
//
// RJHM van den Bergh , rvdb@comweb.nl or comwebnl@proton.me
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class AWTConsole extends WindowAdapter implements WindowListener, ActionListener, Runnable {

    private Frame frame;
    private Button button;
    private TextArea textArea;
    private final Thread reader;    // used to read from System.out
    private final Thread reader2;   // used to read from System.err
    private boolean quit = false;    // signals the Threads that they should exit

    private final PipedInputStream pin = new PipedInputStream();  // used to read from System.out
    private final PipedInputStream pin2 = new PipedInputStream(); // used to read from System.err 

    private Thread errorThrower; // just for testing (Throws an Exception at this Console

    public AWTConsole() {
        // create all components and add them
        frame = new Frame("Java Console");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = new Dimension((int) (2 * screenSize.width / 3), (int) (screenSize.height / 2));
        int x = (int) (frameSize.width / 4);
        int y = (int) (frameSize.height / 2);
        frame.setBounds(x, y, frameSize.width, frameSize.height);

        textArea = new TextArea();
        String intro = "";
        intro += " **************************************************** \n";
        intro += " * AWTConsole                                       * \n";
        intro += " * System.err and System.out are printed here       * \n";
        intro += " * (Free to use at own risk)                        * \n";
        intro += " * https://github.com/RJHMvandenBergh/Java-Console  * \n";
        intro += " *                                                  * \n";
        intro += " * The main method of AWTConsole stands an example. * \n";
        intro += " ****************************************************\n\n";
        textArea.setText(intro);
        textArea.setEditable(false);
        button = new Button("clear");

        Panel panel = new Panel();
        panel.setLayout(new BorderLayout());
        panel.add(textArea, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        frame.add(panel);

        try {
            PipedOutputStream pout = new PipedOutputStream(this.pin);
            System.setOut(new PrintStream(pout, true));
        } catch (java.io.IOException io) {
            textArea.append("Couldn't redirect STDOUT to this console\n" + io.getMessage());
        } catch (SecurityException se) {
            textArea.append("Couldn't redirect STDOUT to this console\n" + se.getMessage());
        }

        try {
            PipedOutputStream pout2 = new PipedOutputStream(this.pin2);
            System.setErr(new PrintStream(pout2, true));
        } catch (java.io.IOException | SecurityException io) {
            textArea.append("Couldn't redirect STDERR to this console\n" + io.getMessage());
        }

        // Starting two seperate threads to read from the PipedInputStreams				
        reader = new Thread(this);
        reader.setDaemon(true);
        reader2 = new Thread(this);
        reader2.setDaemon(true);
    }

    public void init() {
        // adding listeners
        frame.addWindowListener(this);
        button.addActionListener(this);
        // starting the threads
        reader.start();
        reader2.start();
    }
    public void setVisible(boolean visible)
    {
        frame.setVisible(visible);
        frame.revalidate();
        frame.repaint();
    }

    @Override
    public synchronized void windowClosed(WindowEvent evt) {
        quit = true;
        this.notifyAll(); // stop all threads
        try {
            reader.join(1000);
            pin.close();
        } catch (Exception e) {
        }
        try {
            reader2.join(1000);
            pin2.close();
        } catch (Exception e) {
        }
        System.exit(0);
    }

    @Override
    public synchronized void windowClosing(WindowEvent evt) {
        frame.setVisible(false); // default behaviour of JFrame	
        frame.dispose();
    }

    @Override
    public synchronized void actionPerformed(ActionEvent evt) {
        textArea.setText("");
    }

    @Override
    public synchronized void run() {
        try {
            while (Thread.currentThread() == reader) {
                try {
                    this.wait(100);
                } catch (InterruptedException ie) {
                }
                if (pin.available() != 0) {
                    String input = this.readLine(pin);
                    textArea.append(input);
                }
                if (quit) {
                    return;
                }
            }

            while (Thread.currentThread() == reader2) {
                try {
                    this.wait(100);
                } catch (InterruptedException ie) {
                }
                if (pin2.available() != 0) {
                    String input = this.readLine(pin2);
                    textArea.append(input);
                }
                if (quit) {
                    return;
                }
            }
        } catch (Exception e) {
            textArea.append("\nConsole reports an Internal error.");
            textArea.append("The error is: " + e);
        }

        // just for testing (Throw a Nullpointer after 1 second)
        if (Thread.currentThread() == errorThrower) {
            try {
                this.wait(1000);
            } catch (InterruptedException ie) {
            }
            throw new NullPointerException("Application test: throwing an NullPointerException It should arrive at the console");
        }

    }

    public synchronized String readLine(PipedInputStream in) throws IOException {
        String input = "";
        do {
            int available = in.available();
            if (available == 0) {
                break;
            }
            byte b[] = new byte[available];
            in.read(b);
            input = input + new String(b, 0, b.length);
        } while (!input.endsWith("\n") && !input.endsWith("\r\n") && !quit);
        return input;
    }

    public static void main(String[] arg) {
        AWTConsole awtConsole = new AWTConsole(); // create console with no reference
        awtConsole.init();
        awtConsole.setVisible(true);

        System.out.println("\nLets throw an error on this console");
        awtConsole.errorThrower = new Thread(awtConsole);
        awtConsole.errorThrower.setDaemon(true);
        awtConsole.errorThrower.start();

    }
}
