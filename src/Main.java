import com.leapmotion.leap.*;

import java.io.*;

public class Main {
    static boolean listen = true;
    static Controller controller = new Controller();
    static MyListener myListener;
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));



    public static void main(String[] args) throws IOException, InterruptedException {
        myListener = new MyListener();
        myListener.initialize("alph");
        System.out.println("click enter to start");
        startListening();
    }


    public static void startListening() throws IOException, InterruptedException {
        while (true) {
            String line = in.readLine();
            if (line.equalsIgnoreCase("q")) {
                break;
            } else if (line.length() == 0) {
                if (!listen) {
                    System.out.println("Listening= " + !controller.removeListener(myListener));
                    listen = !listen;

                } else {
                    System.out.println("Listening= " + controller.addListener(myListener));
                    listen = !listen;
                }
            } else {
                controller.removeListener(myListener);
                myListener.setClassFeature(line);
                System.out.print("ready in 3 seconds ");
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(1000);
                    System.out.println(".");
                }
                controller.addListener(myListener);
            }
        }

    }
}