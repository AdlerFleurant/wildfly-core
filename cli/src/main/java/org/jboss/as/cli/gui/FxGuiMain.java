package org.jboss.as.cli.gui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.gui.component.CLIOutput;
import org.jboss.as.cli.gui.component.FxScriptMenu;
import org.jboss.as.cli.gui.component.FxTabsMenu;
import org.jboss.as.cli.gui.metacommand.FxDeployDialog;
import org.jboss.as.cli.gui.metacommand.FxUndeployCommandDialog;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.wildfly.common.annotation.NotNull;

public class FxGuiMain implements Initializable {

    @FXML
    private CheckBox verboseCheckBox;
    @FXML
    private TextArea cmdTextField;
    @FXML
    private TabPane tabs;

    private FxDoOperationActionListener opListener = new FxDoOperationActionListener();

    public FxGuiMain() {
        this.resourceControllerPair = FxUtil.getInstance().loadResourceControllerPair(getClass(), "FxDeployDialog.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verboseCheckBox.selectedProperty().bindBidirectional(FxCliGuiContext.getInstance().isVerbose());
        StringProperty cmdText = FxCliGuiContext.getInstance().getCmdText();
        cmdTextField.textProperty().bindBidirectional(cmdText);
        cmdText.addListener((observable, oldValue, newValue) -> {
            if (!cmdTextField.isFocused()) {
                cmdTextField.requestFocus();
            }
        });

        cmdTextField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                event.consume();
                execute();
            }
        });
        FxManagementModelNode fxManagementModelNode = new FxManagementModelNode();
        fxManagementModelNode.explore();
        FxManagementModelNode loggingSubsys = fxManagementModelNode.findNode("/subsystem=logging/");
        FxCliGuiContext cliGuiCtx = FxCliGuiContext.getInstance();
        if (loggingSubsys != null && cliGuiCtx.isStandalone() && this.isLogDownloadAvailable()) {
            VBox serverLogTabContent = FxUtil.getInstance().loadResource(getClass(), "FxServerLogs.fxml");
            Tab serverLogTab = new Tab("Server Logs", serverLogTabContent);
            tabs.getTabs().add(1, serverLogTab);
        }

        FxCliGuiContext.getInstance().setTabs(tabs);
    }

    @FXML
    private void execute() {
        opListener.run();
    }

    public void deploy() {
        //FXMLLoader loader = FxUtil.loadResource(getClass(), "FxDeployDialog.fxml");
        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setDialogPane(resourceControllerPair.getKey());
        resourceControllerPair.getValue().setWindow(resourceControllerPair.getKey().getScene().getWindow());

        dialog.showAndWait()
                .filter(bt -> bt == ButtonType.OK)
                .ifPresent(response -> {
                            FxDeployDialog dialogController = resourceControllerPair.getValue();
                            if (dialogController.getPath().isEmpty()) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Empty File Path");
                                alert.setContentText("A file must be selected.");
                                alert.showAndWait();
                                deploy();
                                return;
                            }

                            StringBuilder builder = new StringBuilder("deploy");
                            builder.append("  ").append(dialogController.getPath());

                            String name = dialogController.getName();
                            if (!name.isEmpty()) builder.append("  --name=").append(name);

                            String runtimeName = dialogController.getRuntimeName();
                            if (!runtimeName.isEmpty()) builder.append("  --runtime-name=").append(runtimeName);

                            if (dialogController.isForced()) builder.append("  --force");
                            if (dialogController.isDisabled()) builder.append("  --disabled");

//                               if (!FxCliGuiContext.getInstance().isStandalone()) {
//                                   if (allServerGroups.isSelected() && allServerGroups.isEnabled()) {
//                                       builder.append("  --all-server-groups");
//                                   } else if (serverGroupChooser.isEnabled()) {
//                                       builder.append(serverGroupChooser.getCmdLineArg());
//                                   }
//                               }

                            FxCliGuiContext.getInstance().getCmdText().set(builder.toString());
                        }
                );
    }

    private boolean isLogDownloadAvailable() {
        try {
            ModelNode readOps = FxCliGuiContext.getInstance().getExecutor().doCommand("/subsystem=logging:read-children-types");
            if (!readOps.get("result").isDefined()) return false;
            for (ModelNode op : readOps.get("result").asList()) {
                if ("log-file".equals(op.asString())) return true;
            }
        } catch (CommandFormatException | IOException e) {
            return false;
        }
        return false;
    }

    public void browseHelp() {
        FxUtil.getInstance().open("https://community.jboss.org/wiki/AGUIForTheCommandLineInterface");
    }

    private static CommandContext CMD_CTX;

    private static Supplier<ModelControllerClient> CLIENT;

    static {
        InputStream iconStream = FxGuiMain.class.getResourceAsStream("/icon/wildfly.png");
        //TODO: ToolTipManager.sharedInstance().setDismissDelay(15000); migrate after java 9
    }

    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(GuiMain.class);

    private static String LOOK_AND_FEEL_KEY = "cli-gui-laf";

    //    @Override
    public void start(Stage primaryStage) {
        prepareScene(primaryStage, makeGuiContext(CMD_CTX, CLIENT));
    }

    public static void start(CommandContext cmdCtx) {
        CMD_CTX = cmdCtx;
//        launch();
    }

    public static void start(Stage stage, CommandContext cmdCtx) {
        CMD_CTX = cmdCtx;
        prepareScene(stage, makeGuiContext(cmdCtx, null));
    }

    public static FxCliGuiContext startEmbedded(CommandContext cmdCtx, Supplier<ModelControllerClient> client) {
        return makeGuiContext(cmdCtx, client);
    }

    private static FxCliGuiContext makeGuiContext(CommandContext cmdCtx, Supplier<ModelControllerClient> client) {
        FxCliGuiContext cliGuiCtx = FxCliGuiContext.getInstance();

        cliGuiCtx.setCommandContext(cmdCtx);

        FxCommandExecutor executor = new FxCommandExecutor(client);
        cliGuiCtx.setExecutor(executor);

        CLIOutput output = new CLIOutput();
        cliGuiCtx.setOutput(output);

        Pane outputDisplay = makeOutputDisplay(output);


//        FxCommandLine cmdLine = new FxCommandLine(opListener);
//        cliGuiCtx.setCommandLine(cmdLine);

//        output.addMouseListener(new FxSelectPreviousOpMouseAdapter(cliGuiCtx, opListener));

        return cliGuiCtx;
    }

    private static synchronized void prepareScene(@NotNull Stage stage, FxCliGuiContext cliGuiCtx) {
        VBox mainBox = new VBox();

        mainBox.setPrefSize(800, 600);

        Scene scene = new Scene(mainBox, 800, 600);

//        scene.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        scene.addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosed(WindowEvent e) {
//                System.exit(0);
//            }
//        });

        mainBox.getChildren().add(makeMenuBar(cliGuiCtx));

        BorderPane contentPane = new BorderPane();
        contentPane.setCenter(cliGuiCtx.getMainPanel());

        mainBox.getChildren().add(contentPane);

//      TODO:  setUpLookAndFeel(cliGuiCtx.getMainWindow());
        stage.setScene(scene);
        stage.show();
    }

    public static MenuBar makeMenuBar(FxCliGuiContext cliGuiCtx) {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(makeDeploymentsMenu(cliGuiCtx));
        menuBar.getMenus().add(new FxTabsMenu(cliGuiCtx));
        menuBar.getMenus().add(new FxScriptMenu(cliGuiCtx));
//      TODO:  Menu lfMenu = makeLookAndFeelMenu(cliGuiCtx);
//        if (lfMenu != null) menuBar.add(lfMenu);
//        JMenu helpMenu = makeHelpMenu();
//        if (helpMenu != null) menuBar.add(helpMenu);


        return menuBar;
    }

//    private static Menu makeLookAndFeelMenu(FxCliGuiContext cliGuiCtx) {
//        final UIManager.LookAndFeelInfo[] all = UIManager.getInstalledLookAndFeels();
//        if (all == null) return null;
//
//        final JMenu lfMenu = new JMenu("Look & Feel");
//        lfMenu.setMnemonic(KeyEvent.VK_L);
//
//        for (final UIManager.LookAndFeelInfo lookAndFeelInfo : all) {
//            JMenuItem item = new JMenuItem(new ChangeLookAndFeelAction(cliGuiCtx, lookAndFeelInfo));
//            lfMenu.add(item);
//        }
//
//        return lfMenu;
//    }

//    private static class ChangeLookAndFeelAction extends AbstractAction {
//        private static final String errorTitle = "Look & Feel Not Set";
//        private FxCliGuiContext cliGuiCtx;
//        private UIManager.LookAndFeelInfo lookAndFeelInfo;
//
//        ChangeLookAndFeelAction(FxCliGuiContext cliGuiCtx, UIManager.LookAndFeelInfo lookAndFeelInfo) {
//            super(lookAndFeelInfo.getName());
//            this.cliGuiCtx = cliGuiCtx;
//            this.lookAndFeelInfo = lookAndFeelInfo;
//        }
//
//        @Override
//        public void actionPerformed(final ActionEvent e) {
//            Window mainWindow = cliGuiCtx.getMainWindow();
//            try {
//                UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
//                SwingUtilities.updateComponentTreeUI(mainWindow);
//                PREFERENCES.put(LOOK_AND_FEEL_KEY, lookAndFeelInfo.getClassName());
//            } catch (Exception ex) {
//                showErrorDialog(mainWindow, errorTitle, ex);
//            }
//        }
//    }

    private static Menu makeDeploymentsMenu(FxCliGuiContext cliGuiCtx) {
        Menu metaCmdMenu = new Menu("_Deployments");

        MenuItem deploy = new MenuItem("_Deploy");
        deploy.setOnAction(ae -> {
            FxDeployDialog dialog = new FxDeployDialog();
//TODO:            dialog.setLocationRelativeTo(cliGuiCtx.getMainWindow());
//            dialog.setVisible(true);
//            dialog.showAndWait();
        });
        metaCmdMenu.getItems().add(deploy);

        MenuItem unDeploy = new MenuItem("_Undeploy");
        unDeploy.setOnAction(ae -> {
            FxUndeployCommandDialog dialog = new FxUndeployCommandDialog(cliGuiCtx);
//          TODO:  dialog.setLocationRelativeTo(cliGuiCtx.getMainWindow());
            dialog.showAndWait();
        });
        metaCmdMenu.getItems().add(unDeploy);

        return metaCmdMenu;
    }

//    private static Menu makeHelpMenu() {
//        if (!Desktop.isDesktopSupported()) return null;
//        final Desktop desktop = Desktop.getDesktop();
//        if (!desktop.isSupported(Desktop.Action.BROWSE)) return null;
//
//        JMenu help = new JMenu("Help");
//        help.setMnemonic(KeyEvent.VK_H);
//        JMenuItem onlineHelp = new JMenuItem(new OnlineHelpAction());
//        help.add(onlineHelp);
//
//        return help;
//    }

    private static Pane makeOutputDisplay(CLIOutput output) {
        BorderPane outputDisplay = new BorderPane();
        outputDisplay.setPrefSize(400, 5000);
//        outputDisplay.setLayout(new BorderLayout(5,5));
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(output);
        outputDisplay.setCenter(swingNode);
        return outputDisplay;
    }

//    public static void setUpLookAndFeel(Window mainWindow) {
//        try {
//            final String laf = PREFERENCES.get(LOOK_AND_FEEL_KEY, UIManager.getSystemLookAndFeelClassName());
//            UIManager.setLookAndFeel(laf);
//            SwingUtilities.updateComponentTreeUI(mainWindow);
//        } catch (Throwable e) {
//            // Just ignore if the L&F has any errors
//        }
//    }

    private static void showErrorDialog(Window window, final String title, final Throwable t) {
        Alert alert = new Alert(Alert.AlertType.ERROR, t.getLocalizedMessage());
        alert.setTitle(title);
        alert.initOwner(window);
        alert.show();
    }

    private Pair<DialogPane, FxDeployDialog> resourceControllerPair;
}
