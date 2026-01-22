package All;
import dorkbox.systemTray.Menu;
import dorkbox.systemTray.SystemTray;
import dorkbox.systemTray.MenuItem;
import java.net.URL;

public class TrayClass {
    static URL icon = TrayClass.class.getResource("/A.png");
    public static void main(String[] args) {
        TrayClass trayClass = new TrayClass();

        SystemTray traySystem = SystemTray.get();
        if (traySystem == null){
            System.out.println("System tray not supported");
            return;
        }
        traySystem.setImage(icon);
        traySystem.setTooltip("Opa");
        Menu menu = traySystem.getMenu();

        MenuItem open = new MenuItem("Open management");
        MenuItem cameraASCII = new MenuItem("Camera ASCII");
        MenuItem dance = new MenuItem("Just dance");
        MenuItem exit = new MenuItem("Exit");

        menu.add(open);
        menu.add(cameraASCII);
        menu.add(dance);
        menu.add(exit);

        open.setCallback(e -> {ClassWindow.metodCenter();});
        cameraASCII.setCallback(e -> {new Thread(CameraASCII::metodSuprem).start();});
        dance.setCallback(e -> {JustDance.metodJustDance();});
        exit.setCallback(e -> {traySystem.shutdown();
            System.exit(0);
        });

    }
}
