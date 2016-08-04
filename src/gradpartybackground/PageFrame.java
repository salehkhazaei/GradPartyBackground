package gradpartybackground;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import com.sun.jna.platform.win32.*;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;

/**
 *
 * @author Saleh
 */
public class PageFrame extends JFrame {

    JPanel panel;
    double margin = 0;
    Toolkit toolkit = Toolkit.getDefaultToolkit();

    public PageFrame() {

        panel = new JPanel() {
            public JPanel run() {
                new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            if (panel != null) {
                                panel.repaint();
                            }
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }.start();
                return this;
            }

            double th = 0;

            @Override
            public void paint(Graphics g2d) {
                BufferedImage buf = new BufferedImage(toolkit.getScreenSize().width, toolkit.getScreenSize().height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = buf.createGraphics();

                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(1f, 1f, 1f));
                g.fillRect(0, 0, buf.getWidth(), buf.getHeight());

                g.translate(panel.getWidth() / 2, panel.getHeight() / 2);
                g.rotate(Math.toRadians(th));

                // x < 60 ? sqrt(x) : sqrt(-x + 120)
                double x = (th + 60) % 120;
                double speed = x < 60 ? Math.sqrt(x) : Math.sqrt(-x + 120);
                th += Math.max(Math.min(3.0, speed / 5.0), 0.01);

                try {
                    BufferedImage logo = ImageIO.read(new File("logo-v1.png"));
                    double r = Math.sqrt(logo.getWidth() * logo.getWidth() + logo.getHeight() * logo.getHeight());
                    double scale = (Math.min(panel.getWidth(), panel.getHeight()) - margin) / r;
                    g.drawImage(logo, -((int) (logo.getWidth() * scale)) / 2,
                            -((int) (logo.getHeight() * scale)) / 2,
                            (int) (logo.getWidth() * scale),
                            (int) (logo.getHeight() * scale), null);
                } catch (IOException ex) {
                    Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                g2d.drawImage(buf, 0, 0, getWidth(), getHeight(), null);
            }
        }.run();
        this.setSize(toolkit.getScreenSize());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocation(0, 0);
        this.setUndecorated(true);
        this.setVisible(true);
        this.setLayout(null);

        panel.setSize(getSize());
        panel.setLocation(0, 0);
        this.add(panel);

        initServer();
    }

    public class MyHandler implements HttpHandler {

        public HashMap<String, ArrayList<String>> getQueryParams(String query) {
            try {
                HashMap<String, ArrayList<String>> params = new HashMap<String, ArrayList<String>>();
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }

                    ArrayList<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        params.put(key, values);
                    }
                    values.add(value);
                }

                return params;
            } catch (Exception ex) {
                Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println(t.getRequestURI());
            String response = "<html><head><meta charset=\"utf-8\">"
                    + "<style> "
                    + "html,body { background: #000; color: white } "
                    + "table { width: 100%; text-align: center } "
                    + "a { padding: 20px; background: #0a0; color: white; font-size: 200% } "
                    + ".red { background: #a00; } "
                    + "h1 { margin: 10px }"
                    + "</style></head><body><table cellspace=10px cellpadding=50px >";

            if (t.getRequestURI().getPath().startsWith("/files")) {
                System.out.println("FFF");
                File folder = new File("F:\\New folder (4)\\GradPartyBackground\\files\\");
                String foldername = "";
                if (t.getRequestURI().getQuery() != null) {
                    HashMap<String, ArrayList<String>> arr = getQueryParams(t.getRequestURI().getQuery());
                    String filename = arr.get("folder").get(0);
                    folder = new File("F:\\New folder (4)\\GradPartyBackground\\files\\" + filename);
                    foldername = filename + "\\";
                }
                File[] listOfFiles = folder.listFiles();
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        response += "<tr><td><a href=\"/download?file=" + URLEncoder.encode(foldername + listOfFiles[i].getName()) + "\">" + listOfFiles[i].getName() + "</a></td></tr>";
                    } else if (listOfFiles[i].isDirectory()) {
                        response += "<tr><td><a href=\"/files?folder=" + URLEncoder.encode(listOfFiles[i].getName()) + "\">" + listOfFiles[i].getName() + "</a></td></tr>";
                    }
                }
                response += "<tr><td><a class='red' href=\"/close\">Close</a></td></tr>";
                System.out.println("S");
            } else if (t.getRequestURI().getPath().startsWith("/download")) {
                HashMap<String, ArrayList<String>> arr = getQueryParams(t.getRequestURI().getQuery());
                String filename = arr.get("file").get(0);
                System.out.println(filename);
                File file = new File("F:\\New folder (4)\\GradPartyBackground\\files\\" + filename);
                System.out.println(file.length());
                t.sendResponseHeaders(200, file.length());
                // TODO set the Content-Type header to image/gif 
                OutputStream outputStream = t.getResponseBody();
                Files.copy(file.toPath(), outputStream);
                outputStream.close();
                return;
            } else if (t.getRequestURI().getPath().startsWith("/play")) {
                HashMap<String, ArrayList<String>> arr = getQueryParams(t.getRequestURI().getQuery());
                while (margin < Math.min(panel.getWidth(), panel.getHeight())) {
                    margin += 10;
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Desktop.getDesktop().open(new File("F:\\New folder (4)\\GradPartyBackground\\files\\" + URLDecoder.decode(arr.get("file").get(0))));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    activate.setFocus("KMPlayer");
                    Robot robot = new Robot();
                    robot.setAutoDelay(250);
                    //robot.keyPress(KeyEvent.VK_ALT);
                    //robot.keyPress(KeyEvent.VK_ENTER);
                    //robot.keyRelease(KeyEvent.VK_ENTER);
                    //robot.keyRelease(KeyEvent.VK_ALT);
                } catch (IOException ex) {
                    Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (AWTException ex) {
                    Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                response += "<script>window.location='/files';</script>";
            } else if (t.getRequestURI().getPath().startsWith("/close")) {
                Runtime.getRuntime().exec("taskkill /F /IM Video.UI.exe");
                Runtime.getRuntime().exec("taskkill /F /IM KMP*");
                try {
                    Thread.sleep(500);

                    Robot bot = new Robot();
//                    bot.mouseMove(toolkit.getScreenSize().width - 10, toolkit.getScreenSize().height / 2);
//                    bot.mousePress(InputEvent.BUTTON1_MASK);
//                    bot.mouseRelease(InputEvent.BUTTON1_MASK);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (AWTException ex) {
                    Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                while (margin > 0) {
                    margin -= 10;
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                response += "<script>window.location='/files';</script>";
            } else {
                response += "Invalid request";
            }
            response += "</table></body></html>";

            System.out.println(response.length());
            t.sendResponseHeaders(200, 0);
            System.out.println("Done");
            try {
                BufferedOutputStream os = new BufferedOutputStream(t.getResponseBody(),1024);
                System.out.println("Done");
                byte[] buf = new byte[50];
                byte[] res = response.getBytes();
                int j = 0 ;
                for ( int i = 0 ; i < res.length ; i ++ )
                {
                    buf[j] = res[i];
                    j ++ ;
                    if ( j >= buf.length )
                    {
                        os.write(buf);
                        j = 0;
                    }
                }
                if ( j != 0 )
                    os.write(buf);
                System.out.println("Done");
                os.close();
            } catch (Exception E) {
                E.printStackTrace();
            }
            System.out.println("Done");
        }
    }

    HttpServer server;

    public void initServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(9390), 0);
            server.createContext("/files", new MyHandler());
            server.createContext("/download", new MyHandler());
            server.createContext("/play", new MyHandler());
            server.createContext("/close", new MyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(PageFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
