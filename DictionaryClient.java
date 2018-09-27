/* Yixiong Ding, 671499
 * <yixiongd@student.unimelb.edu.au>
 * COMP90015 Distributed System
 * September, 2018
 * The University of Melbourne */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

public class DictionaryClient extends Application {

    @Override
    public void start(Stage stage) {
        initUI(stage);
    }

    private void initUI(Stage stage) {
        // socket connection
        try {
            this._socket = new Socket(ip, port);
        } catch (UnknownHostException e) {
            System.err.println("unkownn host " + ip);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("socket I/O error, invalid port number");
            System.exit(1);
        }

        // setup ui
        VBox root = new VBox(5);
        root.setPadding(new Insets(10));

        // init ui components
        this._word = new TextField();
        this._meaning = new TextArea();
        this._status_bar = new Text();
        this._mode = Mode.SEARCH;
        this._message = new String();

        //this._meaning.setDisable(true);
        updateUI();

        root.getChildren().addAll(this._word, this._meaning, this._status_bar);

        Scene scene = new Scene(root);

        // event listener
        scene.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
            if (KEY_SEARCH.match(event)) {
                this._mode = Mode.SEARCH;
                this._meaning.setText("");
                //this._meaning.setDisable(true);
                this._message = "";
                updateUI();
            } else if (KEY_ADD.match(event)) {
                this._mode = Mode.ADD;
                this._meaning.setText("");
                //this._meaning.setDisable(false);
                this._message = "";
                updateUI();
            } else if (KEY_REMOVE.match(event)) {
                this._mode = Mode.REMOVE;
                this._meaning.setText("");
                //this._meaning.setDisable(true);
                this._message = "";
                updateUI();
            } else if (KEY_REQUEST.match(event)) {
                emit();
                updateUI();
            } else if (KEY_HELP.match(event)) {
                this._status_bar.setText(HELP_MSG);
            }
        });

        this._word.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                emit();
                updateUI();
            }
        });

        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("java -jar DictionaryClient <ADDRESS> <PORT>");
            System.exit(1);
        }
        try {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("\"" + args[1] + "\"" + " is not a valid integer");
            System.exit(1);
        }
        launch(args);
    }

    private String getStatusString() {
        return "[" + modeString() + "]    " + this._message;
    }

    private void updateUI() {
        this._meaning.setStyle("-fx-background-color: white");
        this._meaning.setStyle("-fx-text-fill: blue");
        this._status_bar.setText(getStatusString());
    }

    private void emit() {
        try {
            DataInputStream input = new DataInputStream(this._socket.getInputStream());
            DataOutputStream output = new DataOutputStream(this._socket.getOutputStream());

            if (this._word.getText().isEmpty()) {
                this._message = "empty word!";
                return;
            }

            Message msg = new Message();
            msg.put("operation", modeString());
            msg.put("word", this._word.getText());
            
            if (isAdd()) {
                String meaning = this._meaning.getText();
                if (meaning.isEmpty()) {
                    this._message = "empty meaning!";
                    return;
                } else {
                    msg.put("meaning", meaning);
                }
            }

            System.out.println(msg.toString());
            output.writeUTF(msg.toString());
            output.flush();

            String response = input.readUTF();
            HashMap<String, String> map = Message.toHashMap(response);
            this._message = map.get("message");

            if (isSearch()) {
                if (map.get("status").equals("SUCCESS")) {
                    this._meaning.setText(map.get("meaning"));
                } else {
                    this._meaning.setText("");
                }
            }
        } catch (IOException e) {
            this._message = "connection to the server is lost";
            System.out.println("connection to the server is lost");
        }
    }

    private String modeString() { return this._mode.name(); }
    private boolean isSearch() { return this._mode == Mode.SEARCH; }
    private boolean isAdd()    { return this._mode == Mode.ADD;    }
    private boolean isRemove() { return this._mode == Mode.REMOVE; }

    private static enum Mode { SEARCH, ADD, REMOVE }

    private TextField _word;
    private TextArea _meaning;
    private Text     _status_bar;
    private Mode     _mode;     // mode
    private String   _message;    // general message: connection status, response, ...
    private Socket   _socket;

    private static String ip;
    private static int port;

    private static final String TITLE = "Dictionary";
    private static final KeyCombination KEY_HELP = new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination KEY_SEARCH = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination KEY_ADD = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination KEY_REMOVE = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination KEY_REQUEST = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private final String HELP_MSG = "C-S search | C-D add | C-R remove | C-ENTER to request";
}