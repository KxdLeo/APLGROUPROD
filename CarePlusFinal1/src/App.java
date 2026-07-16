import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import careplus.client.CarePlusClient;
import careplus.client.ui.CarePlusClientFrame;
import careplus.server.CarePlusServer;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        String mode = args.length == 0 ? "patient" : args[0].toLowerCase();

        try (ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("applicationContext.xml")) {
            if ("server".equals(mode)) {
                context.getBean("carePlusServer", CarePlusServer.class).start();
                return;
            }
            if ("employee".equals(mode)) {
                startClient(context.getBean("carePlusClient", CarePlusClient.class), "employee");
                return;
            }
            startClient(context.getBean("carePlusClient", CarePlusClient.class), "patient");
        } catch (Exception ex) {
            logger.fatal("CarePlus application failed to start.", ex);
            System.out.println("CarePlus application failed to start. Check logs/careplus-app.log.");
        }
    }

    private static void startClient(final CarePlusClient client, final String mode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CarePlusClientFrame(client, mode).setVisible(true);
            }
        });
    }
}
